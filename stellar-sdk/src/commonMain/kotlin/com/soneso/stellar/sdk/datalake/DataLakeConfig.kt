package com.soneso.stellar.sdk.datalake

import kotlinx.serialization.Serializable

/**
 * Configuration for data lake access.
 */
data class DataLakeConfig(
    val baseUrl: String,
    val requestTimeoutMs: Long = 60_000,
    val maxRetries: Int = 3,
    val maxConcurrentDownloads: Int = 10
) {
    init {
        require(baseUrl.isNotBlank()) { "baseUrl must not be blank" }
        require(baseUrl.endsWith("/")) { "baseUrl must end with /" }
        require(requestTimeoutMs > 0) { "requestTimeoutMs must be positive" }
        require(maxRetries >= 0) { "maxRetries must be non-negative" }
        require(maxConcurrentDownloads > 0) { "maxConcurrentDownloads must be positive" }
    }

    companion object {
        fun mainnet() = DataLakeConfig(
            baseUrl = "https://aws-public-blockchain.s3.us-east-2.amazonaws.com/v1.1/stellar/ledgers/pubnet/"
        )

        fun testnet() = DataLakeConfig(
            baseUrl = "https://datalake-testnet.stellargate.com/ledgers/"
        )

        fun custom(baseUrl: String) = DataLakeConfig(baseUrl = baseUrl)
    }
}

/**
 * Schema configuration read from the data lake's .config.json file.
 */
@Serializable
data class DataLakeSchema(
    val networkPassphrase: String,
    val version: String,
    val compression: String,
    val ledgersPerBatch: Int,
    val batchesPerPartition: Int
) {
    val partitionSize: UInt get() = (ledgersPerBatch * batchesPerPartition).toUInt()
}
