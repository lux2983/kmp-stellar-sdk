package com.soneso.stellar.sdk.datalake

import com.soneso.stellar.sdk.util.ZstdDecompressor
import com.soneso.stellar.sdk.xdr.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json

/**
 * Client for accessing Stellar data lakes.
 *
 * Provides high-performance access to historical ledger data stored in AWS S3 (mainnet)
 * or Stellargate (testnet) following SEP-54 path format.
 *
 * The data lake stores ledger close meta batches as zstd-compressed XDR files.
 * This client handles:
 * - SEP-54 path calculation
 * - Concurrent download management with rate limiting
 * - Zstd decompression
 * - XDR parsing
 * - Transaction and event extraction with filtering
 * - Optional caching to avoid redundant downloads
 *
 * Basic Usage:
 * ```kotlin
 * // Create client with optional caching
 * val cache = InMemoryDataLakeCache(maxEntries = 50)
 * val client = DataLakeClient.mainnet(cache = cache)
 *
 * // Fetch a single ledger batch
 * val batch = client.fetchLedgerBatch(12345u)
 * println("Batch contains ${batch.ledgerCloseMetas.size} ledgers")
 *
 * // Query transactions
 * client.queryTransactions(
 *     ledgerRange = 12345u..12350u,
 *     filter = TransactionFilter.byContract("CCID...")
 * ).collect { tx ->
 *     println("Transaction ${tx.hash} at ledger ${tx.ledger}")
 * }
 *
 * // Close when done
 * client.close()
 * ```
 *
 * Performance:
 * - Concurrent downloads (configurable, default 10)
 * - Streaming API with Flow for memory efficiency
 * - Automatic retry with exponential backoff
 * - Optional caching to reduce network traffic
 *
 * @property config Configuration for data lake access
 * @property cache Optional cache for decompressed ledger batches
 * @property httpClient HTTP client for downloading files
 */
class DataLakeClient(
    private val config: DataLakeConfig,
    private val cache: DataLakeCache? = null,
    private val httpClient: HttpClient = createDefaultHttpClient()
) {
    private val decompressor = ZstdDecompressor()
    private val downloadSemaphore = Semaphore(config.maxConcurrentDownloads)
    private val schemaMutex = Mutex()
    private var cachedSchema: DataLakeSchema? = null

    /**
     * Fetch and cache the data lake schema configuration.
     *
     * The schema defines ledgersPerBatch and batchesPerPartition which are used
     * for SEP-54 path calculation.
     *
     * @return The data lake schema configuration
     * @throws DataLakeException if the schema cannot be fetched or parsed
     */
    suspend fun getSchema(): DataLakeSchema {
        schemaMutex.withLock {
            cachedSchema?.let { return it }

            val configUrl = "${config.baseUrl}.config.json"
            val schema = try {
                val response = httpClient.get(configUrl) {
                    timeout {
                        requestTimeoutMillis = config.requestTimeoutMs
                    }
                }

                when (response.status) {
                    HttpStatusCode.OK -> {
                        val body = response.body<String>()
                        defaultJson.decodeFromString<DataLakeSchema>(body)
                    }
                    HttpStatusCode.NotFound -> throw DataLakeException("Schema not found: $configUrl")
                    else -> throw DataLakeException("HTTP ${response.status}: $configUrl")
                }
            } catch (e: DataLakeException) {
                throw e
            } catch (e: Exception) {
                throw DataLakeException("Failed to fetch schema from $configUrl", e)
            }

            cachedSchema = schema
            return schema
        }
    }

    /**
     * Fetch a ledger batch from the data lake.
     *
     * @param ledgerSequence A ledger number within the batch to fetch (uint32)
     * @return The ledger close meta batch
     * @throws DataLakeException if the batch cannot be fetched
     */
    suspend fun fetchLedgerBatch(ledgerSequence: UInt): LedgerCloseMetaBatchXdr {
        val schema = getSchema()
        return fetchLedgerBatchInternal(ledgerSequence, schema)
    }

    private suspend fun fetchLedgerBatchInternal(
        ledgerSequence: UInt,
        schema: DataLakeSchema
    ): LedgerCloseMetaBatchXdr {
        // Normalize cache key to batch start sequence to avoid duplicate entries
        val batchStartSequence = DataLakePathCalculator.getBatchStartLedger(
            ledgerSequence,
            schema.ledgersPerBatch.toUInt()
        )

        // Check cache first
        cache?.get(batchStartSequence)?.let { cachedData ->
            try {
                val reader = XdrReader(cachedData)
                return LedgerCloseMetaBatchXdr.decode(reader)
            } catch (e: Exception) {
                // Cache data is corrupted, remove it and proceed to download
                cache.remove(batchStartSequence)
            }
        }

        val url = DataLakePathCalculator.getLedgerUrl(
            config.baseUrl,
            schema,
            ledgerSequence
        )

        val compressedData = downloadWithRetry(url)

        val decompressedData = try {
            decompressor.decompress(compressedData)
        } catch (e: Exception) {
            throw DataLakeException(
                "Decompression failed for ledger $ledgerSequence (URL: $url, compressed size: ${compressedData.size} bytes)",
                e
            )
        }

        // Store in cache using normalized batch start sequence
        cache?.put(batchStartSequence, decompressedData)

        return try {
            val reader = XdrReader(decompressedData)
            val batch = LedgerCloseMetaBatchXdr.decode(reader)

            // Validate batch contains expected ledger
            require(batch.startSequence.value <= ledgerSequence && batch.endSequence.value >= ledgerSequence) {
                "Batch range ${batch.startSequence.value}..${batch.endSequence.value} doesn't contain ledger $ledgerSequence"
            }

            batch
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("Unknown") == true) {
                throw DataLakeException(
                    "Unsupported XDR version for ledger $ledgerSequence (URL: $url). SDK update may be required.",
                    e
                )
            }
            throw DataLakeException(
                "XDR parsing failed for ledger $ledgerSequence (URL: $url, decompressed size: ${decompressedData.size} bytes)",
                e
            )
        } catch (e: Exception) {
            throw DataLakeException(
                "XDR parsing failed for ledger $ledgerSequence (URL: $url, decompressed size: ${decompressedData.size} bytes)",
                e
            )
        }
    }

    /**
     * Fetch a range of ledgers as a Flow.
     *
     * @param ledgerRange The range of ledger numbers to fetch (uint32)
     * @return Flow of ledger close meta batches
     */
    fun fetchLedgerRange(ledgerRange: UIntRange): Flow<LedgerCloseMetaBatchXdr> = flow {
        for (ledger in ledgerRange) {
            emit(fetchLedgerBatch(ledger))
        }
    }.buffer(config.maxConcurrentDownloads)

    /**
     * Query transactions matching a filter across a ledger range.
     *
     * @param ledgerRange The range of ledgers to search (uint32)
     * @param filter The transaction filter criteria
     * @return Flow of matching transactions
     */
    fun queryTransactions(
        ledgerRange: UIntRange,
        filter: TransactionFilter
    ): Flow<DataLakeTransaction> = flow {
        for (ledger in ledgerRange) {
            val batch = fetchLedgerBatch(ledger)
            for (meta in batch.ledgerCloseMetas) {
                val transactions = extractTransactions(meta, filter)
                transactions.forEach { emit(it) }
            }
        }
    }.buffer(config.maxConcurrentDownloads)

    /**
     * Query events matching a filter across a ledger range.
     *
     * @param ledgerRange The range of ledgers to search (uint32)
     * @param filter The event filter criteria
     * @return Flow of matching events
     */
    fun queryEvents(
        ledgerRange: UIntRange,
        filter: EventFilter
    ): Flow<DataLakeEvent> = flow {
        for (ledger in ledgerRange) {
            val batch = fetchLedgerBatch(ledger)
            for (meta in batch.ledgerCloseMetas) {
                val events = extractEvents(meta, filter)
                events.forEach { emit(it) }
            }
        }
    }.buffer(config.maxConcurrentDownloads)

    private suspend fun downloadWithRetry(url: String): ByteArray {
        var lastException: Exception? = null

        repeat(config.maxRetries + 1) { attempt ->
            try {
                return downloadSemaphore.withPermit {
                    val response = httpClient.get(url) {
                        timeout {
                            requestTimeoutMillis = config.requestTimeoutMs
                        }
                    }

                    when (response.status) {
                        HttpStatusCode.OK -> response.body<ByteArray>()
                        HttpStatusCode.NotFound -> throw DataLakeException("Ledger not found: $url")
                        else -> throw DataLakeException("HTTP ${response.status}: $url")
                    }
                }
            } catch (e: DataLakeException) {
                if (e.message?.contains("not found") == true) {
                    throw e // Don't retry on 404
                }
                lastException = e
                if (attempt < config.maxRetries) {
                    delay((attempt + 1) * 1000L) // Exponential backoff
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < config.maxRetries) {
                    delay((attempt + 1) * 1000L)
                }
            }
        }

        throw DataLakeException("Failed to download after ${config.maxRetries + 1} attempts", lastException)
    }

    private fun extractTransactions(
        meta: LedgerCloseMetaXdr,
        filter: TransactionFilter
    ): List<DataLakeTransaction> {
        return when (meta) {
            is LedgerCloseMetaXdr.V0 -> extractTransactionsV0(meta.value, filter)
            is LedgerCloseMetaXdr.V1 -> extractTransactionsV1(meta.value, filter)
            is LedgerCloseMetaXdr.V2 -> extractTransactionsV2(meta.value, filter)
        }
    }

    private fun extractEvents(
        meta: LedgerCloseMetaXdr,
        filter: EventFilter
    ): List<DataLakeEvent> {
        return when (meta) {
            is LedgerCloseMetaXdr.V0 -> emptyList() // No Soroban events in V0
            is LedgerCloseMetaXdr.V1 -> extractEventsV1(meta.value, filter)
            is LedgerCloseMetaXdr.V2 -> extractEventsV2(meta.value, filter)
        }
    }

    // Extraction helper methods
    private fun extractTransactionsV0(meta: LedgerCloseMetaV0Xdr, filter: TransactionFilter): List<DataLakeTransaction> {
        val ledgerSeq = meta.ledgerHeader.header.ledgerSeq.value
        val closeTime = meta.ledgerHeader.header.scpValue.closeTime.value.value
        val transactions = mutableListOf<DataLakeTransaction>()

        // In V0, transactions are in TransactionSet
        val envelopes = meta.txSet.txs
        val results = meta.txProcessing

        // Match envelopes with results (they should be in apply order)
        for (i in envelopes.indices) {
            if (i >= results.size) break

            val envelope = envelopes[i]
            val resultMeta = results[i]

            // Apply filters
            if (!matchesTransactionFilterV0(envelope, resultMeta, filter)) {
                continue
            }

            transactions.add(
                DataLakeTransaction(
                    hash = resultMeta.result.transactionHash.value.toHexString(),
                    ledger = ledgerSeq,
                    ledgerCloseTime = closeTime,
                    sourceAccount = envelope.getSourceAccountId(),
                    fee = resultMeta.result.result.feeCharged.value,
                    operationCount = envelope.getOperationCount(),
                    successful = resultMeta.result.result.result.isSuccess(),
                    envelopeXdr = envelope.toXdrByteArray(),
                    resultXdr = resultMeta.result.result.toXdrByteArray(),
                    resultMetaXdr = resultMeta.toXdrByteArray()
                )
            )
        }

        return transactions
    }

    private fun extractTransactionsV1(meta: LedgerCloseMetaV1Xdr, filter: TransactionFilter): List<DataLakeTransaction> {
        val ledgerSeq = meta.ledgerHeader.header.ledgerSeq.value
        val closeTime = meta.ledgerHeader.header.scpValue.closeTime.value.value
        val transactions = mutableListOf<DataLakeTransaction>()

        // In V1, transactions are in GeneralizedTransactionSet
        val envelopes = extractEnvelopesFromGeneralizedTxSet(meta.txSet)
        val results = meta.txProcessing

        // Match envelopes with results
        for (i in envelopes.indices) {
            if (i >= results.size) break

            val envelope = envelopes[i]
            val resultMeta = results[i]

            // Apply filters
            if (!matchesTransactionFilterV0(envelope, resultMeta, filter)) {
                continue
            }

            transactions.add(
                DataLakeTransaction(
                    hash = resultMeta.result.transactionHash.value.toHexString(),
                    ledger = ledgerSeq,
                    ledgerCloseTime = closeTime,
                    sourceAccount = envelope.getSourceAccountId(),
                    fee = resultMeta.result.result.feeCharged.value,
                    operationCount = envelope.getOperationCount(),
                    successful = resultMeta.result.result.result.isSuccess(),
                    envelopeXdr = envelope.toXdrByteArray(),
                    resultXdr = resultMeta.result.result.toXdrByteArray(),
                    resultMetaXdr = resultMeta.toXdrByteArray()
                )
            )
        }

        return transactions
    }

    private fun extractTransactionsV2(meta: LedgerCloseMetaV2Xdr, filter: TransactionFilter): List<DataLakeTransaction> {
        val ledgerSeq = meta.ledgerHeader.header.ledgerSeq.value
        val closeTime = meta.ledgerHeader.header.scpValue.closeTime.value.value
        val transactions = mutableListOf<DataLakeTransaction>()

        // In V2, transactions are in GeneralizedTransactionSet
        val envelopes = extractEnvelopesFromGeneralizedTxSet(meta.txSet)
        val results = meta.txProcessing

        // Match envelopes with results
        for (i in envelopes.indices) {
            if (i >= results.size) break

            val envelope = envelopes[i]
            val resultMetaV1 = results[i]

            // Apply filters
            if (!matchesTransactionFilterV1(envelope, resultMetaV1, filter)) {
                continue
            }

            transactions.add(
                DataLakeTransaction(
                    hash = resultMetaV1.result.transactionHash.value.toHexString(),
                    ledger = ledgerSeq,
                    ledgerCloseTime = closeTime,
                    sourceAccount = envelope.getSourceAccountId(),
                    fee = resultMetaV1.result.result.feeCharged.value,
                    operationCount = envelope.getOperationCount(),
                    successful = resultMetaV1.result.result.result.isSuccess(),
                    envelopeXdr = envelope.toXdrByteArray(),
                    resultXdr = resultMetaV1.result.result.toXdrByteArray(),
                    resultMetaXdr = resultMetaV1.toXdrByteArray()
                )
            )
        }

        return transactions
    }

    private fun extractEventsV1(meta: LedgerCloseMetaV1Xdr, filter: EventFilter): List<DataLakeEvent> {
        val ledgerSeq = meta.ledgerHeader.header.ledgerSeq.value
        val closeTime = meta.ledgerHeader.header.scpValue.closeTime.value.value
        val events = mutableListOf<DataLakeEvent>()

        // Extract events from each transaction's Soroban meta
        for (resultMeta in meta.txProcessing) {
            val txHash = resultMeta.result.transactionHash.value.toHexString()
            val sorobanMeta = resultMeta.txApplyProcessing.getSorobanMeta()

            sorobanMeta?.let { sorobanMetaValue ->
                // Extract contract events
                for (event in sorobanMetaValue.events) {
                    // Use empty string for system events with null contractId
                    val contractIdStr = event.contractId?.let {
                        it.value.value.toContractIdString()
                    } ?: ""

                    if (matchesEventFilter(event, contractIdStr, filter)) {
                        events.add(
                            DataLakeEvent(
                                ledger = ledgerSeq,
                                ledgerCloseTime = closeTime,
                                transactionHash = txHash,
                                contractId = contractIdStr,
                                type = event.type.toEventType(),
                                topics = event.body.getTopics(),
                                data = event.body.getData()
                            )
                        )
                    }
                }

                // Extract diagnostic events if requested
                if (filter.eventType == EventType.DIAGNOSTIC || filter.eventType == null) {
                    for (diagEvent in sorobanMetaValue.diagnosticEvents) {
                        // Use empty string for system events with null contractId
                        val contractIdStr = diagEvent.event.contractId?.let {
                            it.value.value.toContractIdString()
                        } ?: ""

                        if (matchesEventFilter(diagEvent.event, contractIdStr, filter)) {
                            events.add(
                                DataLakeEvent(
                                    ledger = ledgerSeq,
                                    ledgerCloseTime = closeTime,
                                    transactionHash = txHash,
                                    contractId = contractIdStr,
                                    type = EventType.DIAGNOSTIC,
                                    topics = diagEvent.event.body.getTopics(),
                                    data = diagEvent.event.body.getData()
                                )
                            )
                        }
                    }
                }
            }
        }

        return events
    }

    private fun extractEventsV2(meta: LedgerCloseMetaV2Xdr, filter: EventFilter): List<DataLakeEvent> {
        val ledgerSeq = meta.ledgerHeader.header.ledgerSeq.value
        val closeTime = meta.ledgerHeader.header.scpValue.closeTime.value.value
        val events = mutableListOf<DataLakeEvent>()

        // Extract events from each transaction's Soroban meta
        for (resultMetaV1 in meta.txProcessing) {
            val txHash = resultMetaV1.result.transactionHash.value.toHexString()
            val sorobanMeta = resultMetaV1.txApplyProcessing.getSorobanMeta()

            sorobanMeta?.let { sorobanMetaValue ->
                // Extract contract events
                for (event in sorobanMetaValue.events) {
                    // Use empty string for system events with null contractId
                    val contractIdStr = event.contractId?.let {
                        it.value.value.toContractIdString()
                    } ?: ""

                    if (matchesEventFilter(event, contractIdStr, filter)) {
                        events.add(
                            DataLakeEvent(
                                ledger = ledgerSeq,
                                ledgerCloseTime = closeTime,
                                transactionHash = txHash,
                                contractId = contractIdStr,
                                type = event.type.toEventType(),
                                topics = event.body.getTopics(),
                                data = event.body.getData()
                            )
                        )
                    }
                }

                // Extract diagnostic events if requested
                if (filter.eventType == EventType.DIAGNOSTIC || filter.eventType == null) {
                    for (diagEvent in sorobanMetaValue.diagnosticEvents) {
                        // Use empty string for system events with null contractId
                        val contractIdStr = diagEvent.event.contractId?.let {
                            it.value.value.toContractIdString()
                        } ?: ""

                        if (matchesEventFilter(diagEvent.event, contractIdStr, filter)) {
                            events.add(
                                DataLakeEvent(
                                    ledger = ledgerSeq,
                                    ledgerCloseTime = closeTime,
                                    transactionHash = txHash,
                                    contractId = contractIdStr,
                                    type = EventType.DIAGNOSTIC,
                                    topics = diagEvent.event.body.getTopics(),
                                    data = diagEvent.event.body.getData()
                                )
                            )
                        }
                    }
                }
            }
        }

        return events
    }

    // Helper methods for extraction

    private fun extractEnvelopesFromGeneralizedTxSet(txSet: GeneralizedTransactionSetXdr): List<TransactionEnvelopeXdr> {
        return when (txSet) {
            is GeneralizedTransactionSetXdr.V1TxSet -> {
                val envelopes = mutableListOf<TransactionEnvelopeXdr>()
                for (phase in txSet.value.phases) {
                    when (phase) {
                        is TransactionPhaseXdr.V0Components -> {
                            for (component in phase.value) {
                                when (component) {
                                    is TxSetComponentXdr.TxsMaybeDiscountedFee -> {
                                        envelopes.addAll(component.value.txs)
                                    }
                                }
                            }
                        }
                        is TransactionPhaseXdr.ParallelTxsComponent -> {
                            // Extract envelopes from parallel execution stages
                            // Each stage contains clusters, each cluster contains transaction envelopes
                            for (stage in phase.value.executionStages) {
                                for (cluster in stage.value) {
                                    envelopes.addAll(cluster.value)
                                }
                            }
                        }
                    }
                }
                envelopes
            }
        }
    }

    private fun matchesTransactionFilterV0(
        envelope: TransactionEnvelopeXdr,
        resultMeta: TransactionResultMetaXdr,
        filter: TransactionFilter
    ): Boolean {
        // Check if failed transactions should be included
        if (!filter.includeFailedTransactions && !resultMeta.result.result.result.isSuccess()) {
            return false
        }

        // Check source account filter
        filter.sourceAccount?.let { filterAccount ->
            if (envelope.getSourceAccountId() != filterAccount) {
                return false
            }
        }

        // Check contract ID filter (look for InvokeHostFunction operations)
        filter.contractId?.let { filterContractId ->
            if (!envelope.invokesContract(filterContractId)) {
                return false
            }
        }

        // Check operation type filter
        filter.operationType?.let { filterOpType ->
            if (!envelope.hasOperationType(filterOpType)) {
                return false
            }
        }

        return true
    }

    private fun matchesTransactionFilterV1(
        envelope: TransactionEnvelopeXdr,
        resultMetaV1: TransactionResultMetaV1Xdr,
        filter: TransactionFilter
    ): Boolean {
        // Check if failed transactions should be included
        if (!filter.includeFailedTransactions && !resultMetaV1.result.result.result.isSuccess()) {
            return false
        }

        // Check source account filter
        filter.sourceAccount?.let { filterAccount ->
            if (envelope.getSourceAccountId() != filterAccount) {
                return false
            }
        }

        // Check contract ID filter
        filter.contractId?.let { filterContractId ->
            if (!envelope.invokesContract(filterContractId)) {
                return false
            }
        }

        // Check operation type filter
        filter.operationType?.let { filterOpType ->
            if (!envelope.hasOperationType(filterOpType)) {
                return false
            }
        }

        return true
    }

    private fun matchesEventFilter(
        event: ContractEventXdr,
        contractId: String,
        filter: EventFilter
    ): Boolean {
        // Check contract ID filter
        filter.contractId?.let { filterContractId ->
            if (contractId != filterContractId) {
                return false
            }
        }

        // Check event type filter
        filter.eventType?.let { filterType ->
            if (event.type.toEventType() != filterType) {
                return false
            }
        }

        // Check topics filter
        filter.topics?.let { filterTopics ->
            val eventTopics = event.body.getTopics()
            // Match if any filter topic is in event topics
            val hasMatch = filterTopics.any { filterTopic ->
                eventTopics.any { it.toString() == filterTopic }
            }
            if (!hasMatch) {
                return false
            }
        }

        return true
    }

    /**
     * Release resources held by this client.
     * Call when the client is no longer needed.
     */
    fun close() {
        decompressor.close()
        httpClient.close()
    }

    companion object {
        /**
         * Default JSON configuration for serialization/deserialization.
         * Same configuration as HorizonServer for consistency.
         */
        val defaultJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            encodeDefaults = true
        }

        /**
         * Create a client for Mainnet via AWS Public Blockchain.
         *
         * @param cache Optional cache for decompressed ledger batches
         * @param httpClient HTTP client for downloading files
         */
        fun mainnet(
            cache: DataLakeCache? = null,
            httpClient: HttpClient = createDefaultHttpClient()
        ) = DataLakeClient(DataLakeConfig.mainnet(), cache, httpClient)

        /**
         * Create a client for Testnet via Stellargate.
         *
         * @param cache Optional cache for decompressed ledger batches
         * @param httpClient HTTP client for downloading files
         */
        fun testnet(
            cache: DataLakeCache? = null,
            httpClient: HttpClient = createDefaultHttpClient()
        ) = DataLakeClient(DataLakeConfig.testnet(), cache, httpClient)

        /**
         * Creates a default HTTP client with SDK identification headers.
         *
         * The client is configured with:
         * - JSON content negotiation with lenient parsing
         * - SDK identification headers (X-Client-Name, X-Client-Version)
         *
         * Client identification headers help Stellar server operators track SDK usage
         * and identify SDK-specific issues.
         */
        private fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                expectSuccess = false
                install(ContentNegotiation) {
                    json(defaultJson)
                }
                install(DefaultRequest) {
                    header("X-Client-Name", "kmp-stellar-sdk")
                    header("X-Client-Version", com.soneso.stellar.sdk.Util.getSdkVersion())
                }
            }
        }
    }
}
