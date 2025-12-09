# Data Lake Access

The SDK provides direct access to Stellar's public data lakes for querying historical ledger data beyond the 7-day retention window of Soroban RPC.

## Quick Start

```kotlin
import com.soneso.stellar.sdk.datalake.*
import kotlinx.coroutines.flow.collect

// Create a client for mainnet
val client = DataLakeClient.mainnet()

// Fetch a single ledger batch
val batch = client.fetchLedgerBatch(60_000_000u)
println("Batch contains ledgers ${batch.startSequence.value}..${batch.endSequence.value}")

// Query transactions from a ledger range
client.queryTransactions(
    ledgerRange = 60_000_000u..60_000_010u,
    filter = TransactionFilter.all()
).collect { tx ->
    println("Transaction ${tx.hash} from ${tx.sourceAccount}")
}

// Close when done
client.close()
```

## Data Lake Providers

| Network | Provider | Factory Method |
|---------|----------|----------------|
| Mainnet | AWS Public Blockchain | `DataLakeClient.mainnet()` |
| Testnet | Stellargate | `DataLakeClient.testnet()` |
| Custom | Any SEP-54 compatible | `DataLakeClient(DataLakeConfig.custom(url))` |

## Fetching Ledger Data

### Single Ledger Batch

```kotlin
val client = DataLakeClient.mainnet()

// Fetch a ledger batch containing the specified ledger
val batch = client.fetchLedgerBatch(60_000_000u)

// Access ledger close metas
for (ledgerMeta in batch.ledgerCloseMetas) {
    when (ledgerMeta) {
        is LedgerCloseMetaXdr.V0 -> {
            val ledgerSeq = ledgerMeta.value.ledgerHeader.header.ledgerSeq.value
            println("V0 Ledger: $ledgerSeq")
        }
        is LedgerCloseMetaXdr.V1 -> {
            val ledgerSeq = ledgerMeta.value.ledgerHeader.header.ledgerSeq.value
            println("V1 Ledger: $ledgerSeq")
        }
        is LedgerCloseMetaXdr.V2 -> {
            val ledgerSeq = ledgerMeta.value.ledgerHeader.header.ledgerSeq.value
            println("V2 Ledger: $ledgerSeq")
        }
    }
}

client.close()
```

### Ledger Range with Streaming

Use Flow-based streaming for efficient processing of large ranges:

```kotlin
val client = DataLakeClient.mainnet()

// Stream ledger batches (memory efficient for large ranges)
client.fetchLedgerRange(60_000_000u..60_000_100u).collect { batch ->
    println("Processing batch ${batch.startSequence.value}..${batch.endSequence.value}")
}

client.close()
```

## Querying Transactions

### All Transactions

```kotlin
val client = DataLakeClient.mainnet()

client.queryTransactions(
    ledgerRange = 60_000_000u..60_000_010u,
    filter = TransactionFilter.all()
).collect { tx ->
    println("Hash: ${tx.hash}")
    println("Source: ${tx.sourceAccount}")
    println("Fee: ${tx.fee}")
    println("Operations: ${tx.operationCount}")
    println("Successful: ${tx.successful}")
}

client.close()
```

### Filter by Source Account

```kotlin
val client = DataLakeClient.mainnet()

client.queryTransactions(
    ledgerRange = 60_000_000u..60_000_100u,
    filter = TransactionFilter.byAccount("GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H")
).collect { tx ->
    println("Transaction from account: ${tx.hash}")
}

client.close()
```

### Filter by Contract

```kotlin
val client = DataLakeClient.mainnet()

// Filter for transactions invoking a specific contract
client.queryTransactions(
    ledgerRange = 60_000_000u..60_000_100u,
    filter = TransactionFilter.byContract("CDLZFC3SYJYDZT7K67VZ75HPJVIEUVNIXF47ZG2FB2RMQQVU2HHGCYSC")
).collect { tx ->
    println("Contract invocation: ${tx.hash}")
}

client.close()
```

### Include Failed Transactions

```kotlin
val client = DataLakeClient.mainnet()

client.queryTransactions(
    ledgerRange = 60_000_000u..60_000_100u,
    filter = TransactionFilter(includeFailedTransactions = true)
).collect { tx ->
    println("${tx.hash} - successful: ${tx.successful}")
}

client.close()
```

### Combined Filters

```kotlin
val client = DataLakeClient.mainnet()

val filter = TransactionFilter(
    sourceAccount = "GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H",
    contractId = "CDLZFC3SYJYDZT7K67VZ75HPJVIEUVNIXF47ZG2FB2RMQQVU2HHGCYSC",
    includeFailedTransactions = false
)

client.queryTransactions(60_000_000u..60_000_100u, filter).collect { tx ->
    println("Matching transaction: ${tx.hash}")
}

client.close()
```

## Querying Events

### All Events

```kotlin
val client = DataLakeClient.mainnet()

client.queryEvents(
    ledgerRange = 60_000_000u..60_000_010u,
    filter = EventFilter.all()
).collect { event ->
    println("Event from contract: ${event.contractId}")
    println("Type: ${event.type}")
    println("Transaction: ${event.transactionHash}")
    println("Topics: ${event.topics.size}")
}

client.close()
```

### Filter by Contract

```kotlin
val client = DataLakeClient.mainnet()

client.queryEvents(
    ledgerRange = 60_000_000u..60_000_100u,
    filter = EventFilter.byContract("CDLZFC3SYJYDZT7K67VZ75HPJVIEUVNIXF47ZG2FB2RMQQVU2HHGCYSC")
).collect { event ->
    println("Contract event: ${event.type}")
}

client.close()
```

### Filter by Event Type

```kotlin
val client = DataLakeClient.mainnet()

// Filter for diagnostic events only
client.queryEvents(
    ledgerRange = 60_000_000u..60_000_100u,
    filter = EventFilter(eventType = EventType.DIAGNOSTIC)
).collect { event ->
    println("Diagnostic event: ${event.transactionHash}")
}

client.close()
```

## Configuration

### Custom Configuration

```kotlin
val config = DataLakeConfig(
    baseUrl = "https://aws-public-blockchain.s3.us-east-2.amazonaws.com/v1.1/stellar/ledgers/pubnet/",
    requestTimeoutMs = 30_000,     // 30 second timeout
    maxRetries = 2,                // Retry twice on failure
    maxConcurrentDownloads = 5     // Limit concurrent downloads
)

val client = DataLakeClient(config)
// Use client...
client.close()
```

### Schema Information

```kotlin
val client = DataLakeClient.mainnet()

// Get schema configuration (cached after first call)
val schema = client.getSchema()

println("Network: ${schema.networkPassphrase}")
println("Compression: ${schema.compression}")
println("Ledgers per batch: ${schema.ledgersPerBatch}")
println("Batches per partition: ${schema.batchesPerPartition}")
println("Partition size: ${schema.partitionSize}")

client.close()
```

## Error Handling

```kotlin
val client = DataLakeClient.mainnet()

try {
    val batch = client.fetchLedgerBatch(UInt.MAX_VALUE)
} catch (e: DataLakeException) {
    when {
        e.message?.contains("not found") == true -> {
            println("Ledger does not exist in data lake")
        }
        e.message?.contains("Decompression failed") == true -> {
            println("Failed to decompress ledger data: ${e.cause?.message}")
        }
        e.message?.contains("XDR parsing failed") == true -> {
            println("Failed to parse XDR data: ${e.cause?.message}")
        }
        else -> {
            println("Data lake error: ${e.message}")
        }
    }
} finally {
    client.close()
}
```

## Transaction Data Model

`DataLakeTransaction` contains:

| Property | Type | Description |
|----------|------|-------------|
| `hash` | `String` | 64-character hex transaction hash |
| `ledger` | `UInt` | Ledger sequence number |
| `ledgerCloseTime` | `ULong` | Unix timestamp of ledger close |
| `sourceAccount` | `String` | G... account that submitted the transaction |
| `fee` | `Long` | Fee charged in stroops |
| `operationCount` | `Int` | Number of operations |
| `successful` | `Boolean` | Whether the transaction succeeded |
| `envelopeXdr` | `ByteArray` | Raw envelope XDR bytes |
| `resultXdr` | `ByteArray` | Raw result XDR bytes |
| `resultMetaXdr` | `ByteArray?` | Raw result meta XDR bytes (if available) |

## Event Data Model

`DataLakeEvent` contains:

| Property | Type | Description |
|----------|------|-------------|
| `ledger` | `UInt` | Ledger sequence number |
| `ledgerCloseTime` | `ULong` | Unix timestamp of ledger close |
| `transactionHash` | `String` | Transaction that emitted the event |
| `contractId` | `String` | C... contract ID (empty for system events) |
| `type` | `EventType` | CONTRACT, SYSTEM, or DIAGNOSTIC |
| `topics` | `List<SCValXdr>` | Event topic values |
| `data` | `SCValXdr` | Event data value |

## Platform Support

Data Lake access is supported on all KMP platforms:

| Platform | Zstd Library |
|----------|-------------|
| JVM/Android | zstd-jni |
| iOS/macOS | zstd-kmp (Square) |
| JavaScript | fflate |

## Caching

Enable optional caching to avoid redundant downloads when querying the same ledger range multiple times:

```kotlin
// Create cache with LRU eviction (100 entries max)
val cache = InMemoryDataLakeCache(maxEntries = 100)

// Create client with caching
val client = DataLakeClient.mainnet(cache = cache)

// First fetch downloads from network
val batch1 = client.fetchLedgerBatch(60_000_000u)

// Second fetch returns from cache (no network call)
val batch2 = client.fetchLedgerBatch(60_000_000u)

// Monitor cache size
println("Cached batches: ${cache.size()}")

// Clear cache if needed
cache.clear()

client.close()
```

Cache characteristics:
- LRU (Least Recently Used) eviction when full
- Thread-safe for concurrent access
- Each entry stores decompressed XDR data (1-2 MB per batch)
- Cache key is normalized to batch start sequence (efficient for multi-ledger batches)

## Performance Notes

- Each ledger file is 50-200 KB compressed
- Decompression is fast (~10ms per ledger)
- Use `maxConcurrentDownloads` to control parallelism
- Flow-based APIs enable memory-efficient streaming
- Automatic retry with exponential backoff handles transient failures
- Schema is cached after first fetch
- Optional caching reduces network traffic for repeated queries

## Related Documentation

- [SEP-54: Data Indexing Standard](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0054.md)
- [SDK Usage Examples](sdk-usage-examples.md)
- [Soroban RPC Usage](soroban-rpc-usage.md)
