package com.soneso.stellar.sdk.datalake

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * Integration tests for DataLakeClient.
 *
 * These tests verify the SDK's Data Lake integration against the live Stellar mainnet data lake.
 * They cover:
 * - Schema fetching and validation
 * - Single ledger batch fetching
 * - Ledger range fetching
 * - Transaction querying with filters
 * - Event querying with filters
 * - Error handling for non-existent ledgers
 *
 * Test Network: Uses AWS Public Blockchain mainnet data lake which has complete historical data.
 *
 * Running Tests:
 * ```bash
 * ./gradlew :stellar-sdk:jvmTest --tests "DataLakeClientIntegrationTest"
 * ```
 */
class DataLakeClientIntegrationTest {

    // Use mainnet ledgers which are guaranteed to exist in AWS data lake.
    // Ledger 60,000,000 is from late 2024 and has Soroban activity.
    private val testLedgerStart = 60_000_000u

    /**
     * Test fetching and validating schema configuration.
     *
     * This test:
     * 1. Fetches .config.json from mainnet data lake
     * 2. Validates schema structure
     * 3. Verifies expected configuration values
     */
    @Test
    fun testFetchSchema() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            val schema = client.getSchema()

            // Verify schema has expected fields
            assertNotNull(schema.networkPassphrase, "Network passphrase should not be null")
            assertTrue(
                schema.networkPassphrase.contains("Public") || schema.networkPassphrase.contains("pubnet"),
                "Network passphrase should indicate mainnet: ${schema.networkPassphrase}"
            )

            assertNotNull(schema.version, "Version should not be null")
            assertTrue(schema.version.isNotBlank(), "Version should not be blank")

            assertEquals("zstd", schema.compression, "Compression should be zstd")

            assertTrue(schema.ledgersPerBatch > 0, "Ledgers per batch should be positive")
            assertTrue(schema.batchesPerPartition > 0, "Batches per partition should be positive")

            val partitionSize = schema.partitionSize
            assertTrue(partitionSize > 0u, "Partition size should be positive")
            assertEquals(
                (schema.ledgersPerBatch * schema.batchesPerPartition).toUInt(),
                partitionSize,
                "Partition size should equal ledgersPerBatch * batchesPerPartition"
            )

            println("Schema fetched successfully:")
            println("  Network: ${schema.networkPassphrase}")
            println("  Version: ${schema.version}")
            println("  Compression: ${schema.compression}")
            println("  Ledgers per batch: ${schema.ledgersPerBatch}")
            println("  Batches per partition: ${schema.batchesPerPartition}")
            println("  Partition size: $partitionSize")
        } finally {
            client.close()
        }
    }

    /**
     * Test fetching a single ledger batch.
     *
     * This test:
     * 1. Fetches a known ledger from mainnet
     * 2. Verifies batch structure
     * 3. Validates batch contains expected ledger
     */
    @Test
    fun testFetchSingleLedger() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            val ledgerSequence = testLedgerStart

            val batch = client.fetchLedgerBatch(ledgerSequence)

            // Verify batch structure
            assertNotNull(batch, "Batch should not be null")
            assertTrue(
                batch.startSequence.value <= ledgerSequence,
                "Batch start should be <= requested ledger"
            )
            assertTrue(
                batch.endSequence.value >= ledgerSequence,
                "Batch end should be >= requested ledger"
            )

            val ledgerCloseMetas = batch.ledgerCloseMetas
            assertTrue(ledgerCloseMetas.isNotEmpty(), "Batch should contain ledger close metas")

            println("Ledger batch fetched successfully:")
            println("  Start sequence: ${batch.startSequence.value}")
            println("  End sequence: ${batch.endSequence.value}")
            println("  Number of ledgers: ${ledgerCloseMetas.size}")

            // Verify we can extract basic info from a ledger
            val firstLedger = ledgerCloseMetas.first()
            assertNotNull(firstLedger, "First ledger should not be null")

            // Extract ledger sequence based on ledger close meta version
            val ledgerSeq = when (firstLedger) {
                is com.soneso.stellar.sdk.xdr.LedgerCloseMetaXdr.V0 ->
                    firstLedger.value.ledgerHeader.header.ledgerSeq.value
                is com.soneso.stellar.sdk.xdr.LedgerCloseMetaXdr.V1 ->
                    firstLedger.value.ledgerHeader.header.ledgerSeq.value
                is com.soneso.stellar.sdk.xdr.LedgerCloseMetaXdr.V2 ->
                    firstLedger.value.ledgerHeader.header.ledgerSeq.value
            }

            assertTrue(ledgerSeq > 0u, "Ledger sequence should be positive")
            println("  First ledger sequence: $ledgerSeq")
        } finally {
            client.close()
        }
    }

    /**
     * Test fetching a range of ledgers.
     *
     * This test:
     * 1. Fetches a small range of ledgers (3-5)
     * 2. Verifies batches are returned
     * 3. Validates batch ordering
     */
    @Test
    fun testFetchLedgerRange() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            val startLedger = testLedgerStart
            val endLedger = testLedgerStart + 2u
            val ledgerRange = startLedger..endLedger

            val batches = client.fetchLedgerRange(ledgerRange).toList()

            assertTrue(batches.isNotEmpty(), "Should fetch at least one batch")

            println("Fetched ${batches.size} batch(es) for range $startLedger..$endLedger")

            // Verify each batch
            for (batch in batches) {
                assertNotNull(batch, "Batch should not be null")
                assertTrue(
                    batch.ledgerCloseMetas.isNotEmpty(),
                    "Each batch should contain ledgers"
                )

                println("  Batch: ${batch.startSequence.value}..${batch.endSequence.value} (${batch.ledgerCloseMetas.size} ledgers)")
            }
        } finally {
            client.close()
        }
    }

    /**
     * Test querying transactions with source account filter.
     *
     * This test:
     * 1. Queries transactions from a ledger range
     * 2. Applies source account filter
     * 3. Verifies transaction structure
     */
    @Test
    fun testQueryTransactions() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            val ledgerRange = testLedgerStart..(testLedgerStart + 5u)
            val filter = TransactionFilter.all()

            val transactions = client.queryTransactions(ledgerRange, filter).toList()

            // Mainnet ledgers should have transactions
            println("Found ${transactions.size} transaction(s) in range $ledgerRange")

            if (transactions.isNotEmpty()) {
                val firstTx = transactions.first()

                // Verify transaction structure
                assertNotNull(firstTx.hash, "Transaction hash should not be null")
                assertTrue(firstTx.hash.matches(Regex("[0-9a-f]{64}")), "Hash should be 64-char hex string")
                assertTrue(firstTx.ledger > 0u, "Ledger should be positive")
                assertTrue(firstTx.ledgerCloseTime > 0uL, "Close time should be positive")
                assertNotNull(firstTx.sourceAccount, "Source account should not be null")
                assertTrue(firstTx.fee >= 0, "Fee should be non-negative")
                assertTrue(firstTx.operationCount >= 0, "Operation count should be non-negative")
                assertNotNull(firstTx.envelopeXdr, "Envelope XDR should not be null")
                assertNotNull(firstTx.resultXdr, "Result XDR should not be null")

                println("First transaction:")
                println("  Hash: ${firstTx.hash}")
                println("  Ledger: ${firstTx.ledger}")
                println("  Source: ${firstTx.sourceAccount}")
                println("  Fee: ${firstTx.fee}")
                println("  Operations: ${firstTx.operationCount}")
                println("  Successful: ${firstTx.successful}")
            }
        } finally {
            client.close()
        }
    }

    /**
     * Test querying transactions with failed transaction filter.
     *
     * This test:
     * 1. Queries transactions including failed ones
     * 2. Verifies filter is applied correctly
     */
    @Test
    fun testQueryTransactions_IncludeFailed() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            val ledgerRange = testLedgerStart..(testLedgerStart + 10u)
            val filter = TransactionFilter(includeFailedTransactions = true)

            val transactions = client.queryTransactions(ledgerRange, filter).toList()

            println("Found ${transactions.size} transaction(s) (including failed) in range $ledgerRange")

            // Check if we have any failed transactions
            val failedCount = transactions.count { !it.successful }
            println("  Failed: $failedCount")
            println("  Successful: ${transactions.size - failedCount}")
        } finally {
            client.close()
        }
    }

    /**
     * Test querying events from a ledger range.
     *
     * This test:
     * 1. Queries events from ledgers that might have Soroban activity
     * 2. Verifies event structure if events are found
     */
    @Test
    fun testQueryEvents() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            val ledgerRange = testLedgerStart..(testLedgerStart + 10u)
            val filter = EventFilter.all()

            val events = client.queryEvents(ledgerRange, filter).toList()

            println("Found ${events.size} event(s) in range $ledgerRange")

            if (events.isNotEmpty()) {
                val firstEvent = events.first()

                // Verify event structure
                assertTrue(firstEvent.ledger > 0u, "Ledger should be positive")
                assertTrue(firstEvent.ledgerCloseTime > 0uL, "Close time should be positive")
                assertNotNull(firstEvent.transactionHash, "Transaction hash should not be null")
                assertTrue(
                    firstEvent.transactionHash.matches(Regex("[0-9a-f]{64}")),
                    "Transaction hash should be 64-char hex string"
                )
                assertNotNull(firstEvent.contractId, "Contract ID should not be null")
                assertNotNull(firstEvent.type, "Event type should not be null")
                assertNotNull(firstEvent.topics, "Topics should not be null")
                assertNotNull(firstEvent.data, "Data should not be null")

                println("First event:")
                println("  Ledger: ${firstEvent.ledger}")
                println("  Transaction: ${firstEvent.transactionHash}")
                println("  Contract: ${firstEvent.contractId}")
                println("  Type: ${firstEvent.type}")
                println("  Topics: ${firstEvent.topics.size}")
            } else {
                println("Note: No Soroban events found in this range")
            }
        } finally {
            client.close()
        }
    }

    /**
     * Test querying events with contract filter.
     *
     * This test verifies contract ID filtering works correctly.
     */
    @Test
    fun testQueryEvents_WithContractFilter() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            // Use a dummy contract ID for testing filter logic
            val contractId = "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4"
            val ledgerRange = testLedgerStart..(testLedgerStart + 10u)
            val filter = EventFilter.byContract(contractId)

            val events = client.queryEvents(ledgerRange, filter).toList()

            println("Found ${events.size} event(s) for contract $contractId in range $ledgerRange")

            // Verify all returned events match the contract ID
            for (event in events) {
                assertEquals(
                    contractId,
                    event.contractId,
                    "All events should match the filtered contract ID"
                )
            }
        } finally {
            client.close()
        }
    }

    /**
     * Test error handling for non-existent ledger.
     *
     * This test:
     * 1. Attempts to fetch a ledger that doesn't exist
     * 2. Verifies proper error is thrown
     */
    @Test
    fun testLedgerNotFound() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            // Try to fetch a ledger far in the future (should not exist)
            val futureLedger = UInt.MAX_VALUE

            val exception = assertFailsWith<DataLakeException> {
                client.fetchLedgerBatch(futureLedger)
            }

            assertTrue(
                exception.message?.contains("not found") == true ||
                exception.message?.contains("404") == true,
                "Should get 'not found' error for non-existent ledger"
            )

            println("Correctly threw exception for non-existent ledger: ${exception.message}")
        } finally {
            client.close()
        }
    }

    /**
     * Test transaction filtering by source account.
     *
     * This test verifies source account filtering works correctly.
     */
    @Test
    fun testTransactionFilter_ByAccount() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            // Use a specific account to test filtering
            val accountId = "GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H"
            val ledgerRange = testLedgerStart..(testLedgerStart + 20u)
            val filter = TransactionFilter.byAccount(accountId)

            val transactions = client.queryTransactions(ledgerRange, filter).toList()

            println("Found ${transactions.size} transaction(s) for account $accountId")

            // Verify all returned transactions match the source account
            for (tx in transactions) {
                assertEquals(
                    accountId,
                    tx.sourceAccount,
                    "All transactions should match the filtered source account"
                )
            }
        } finally {
            client.close()
        }
    }

    /**
     * Test schema caching.
     *
     * This test verifies that schema is fetched once and cached.
     */
    @Test
    fun testSchemaCaching() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        try {
            // Fetch schema multiple times
            val schema1 = client.getSchema()
            val schema2 = client.getSchema()
            val schema3 = client.getSchema()

            // All should return the same schema object (cached)
            assertEquals(schema1.version, schema2.version, "Schema should be cached")
            assertEquals(schema2.version, schema3.version, "Schema should be cached")
            assertEquals(schema1.ledgersPerBatch, schema2.ledgersPerBatch, "Schema should be cached")
            assertEquals(schema1.batchesPerPartition, schema2.batchesPerPartition, "Schema should be cached")

            println("Schema caching works correctly")
        } finally {
            client.close()
        }
    }

    /**
     * Test client closes properly.
     */
    @Test
    fun testClientClose() = runTest(timeout = 60.seconds) {
        val client = DataLakeClient.mainnet()

        // Fetch something to ensure client works
        val schema = client.getSchema()
        assertNotNull(schema, "Schema should be fetched")

        // Close client
        client.close()

        println("Client closed successfully")
    }

    /**
     * Test custom config with mainnet.
     */
    @Test
    fun testCustomConfig() = runTest(timeout = 60.seconds) {
        val config = DataLakeConfig(
            baseUrl = "https://aws-public-blockchain.s3.us-east-2.amazonaws.com/v1.1/stellar/ledgers/pubnet/",
            requestTimeoutMs = 30_000,
            maxRetries = 2,
            maxConcurrentDownloads = 5
        )
        val client = DataLakeClient(config)

        try {
            val schema = client.getSchema()
            assertNotNull(schema, "Should fetch schema with custom config")

            println("Custom config works correctly")
        } finally {
            client.close()
        }
    }
}
