package com.soneso.stellar.sdk.datalake

import kotlin.test.*

/**
 * Unit tests for DataLakeConfig.
 *
 * Tests configuration validation and factory methods.
 */
class DataLakeConfigTest {

    /**
     * Test mainnet factory returns correct URL.
     */
    @Test
    fun testMainnetFactory() {
        val config = DataLakeConfig.mainnet()

        assertEquals(
            "https://aws-public-blockchain.s3.us-east-2.amazonaws.com/v1.1/stellar/ledgers/pubnet/",
            config.baseUrl,
            "Mainnet should use AWS Public Blockchain URL"
        )
        assertEquals(60_000, config.requestTimeoutMs, "Default timeout should be 60 seconds")
        assertEquals(3, config.maxRetries, "Default max retries should be 3")
        assertEquals(10, config.maxConcurrentDownloads, "Default max concurrent downloads should be 10")
    }

    /**
     * Test testnet factory returns correct URL.
     */
    @Test
    fun testTestnetFactory() {
        val config = DataLakeConfig.testnet()

        assertEquals(
            "https://datalake-testnet.stellargate.com/ledgers/",
            config.baseUrl,
            "Testnet should use Stellargate URL"
        )
        assertEquals(60_000, config.requestTimeoutMs, "Default timeout should be 60 seconds")
        assertEquals(3, config.maxRetries, "Default max retries should be 3")
        assertEquals(10, config.maxConcurrentDownloads, "Default max concurrent downloads should be 10")
    }

    /**
     * Test custom factory.
     */
    @Test
    fun testCustomFactory() {
        val customUrl = "https://custom-datalake.example.com/ledgers/"
        val config = DataLakeConfig.custom(customUrl)

        assertEquals(customUrl, config.baseUrl, "Custom factory should use provided URL")
        assertEquals(60_000, config.requestTimeoutMs, "Default timeout should be 60 seconds")
        assertEquals(3, config.maxRetries, "Default max retries should be 3")
        assertEquals(10, config.maxConcurrentDownloads, "Default max concurrent downloads should be 10")
    }

    /**
     * Test custom configuration with all parameters.
     */
    @Test
    fun testCustomConfiguration() {
        val config = DataLakeConfig(
            baseUrl = "https://custom.example.com/",
            requestTimeoutMs = 30_000,
            maxRetries = 5,
            maxConcurrentDownloads = 20
        )

        assertEquals("https://custom.example.com/", config.baseUrl)
        assertEquals(30_000, config.requestTimeoutMs)
        assertEquals(5, config.maxRetries)
        assertEquals(20, config.maxConcurrentDownloads)
    }

    /**
     * Test validation: blank URL should fail.
     */
    @Test
    fun testValidation_BlankUrl() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(baseUrl = "")
        }

        assertTrue(
            exception.message?.contains("baseUrl must not be blank") == true,
            "Should reject blank URL"
        )
    }

    /**
     * Test validation: URL without trailing slash should fail.
     */
    @Test
    fun testValidation_NoTrailingSlash() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(baseUrl = "https://example.com/ledgers")
        }

        assertTrue(
            exception.message?.contains("baseUrl must end with /") == true,
            "Should reject URL without trailing slash"
        )
    }

    /**
     * Test validation: negative timeout should fail.
     */
    @Test
    fun testValidation_NegativeTimeout() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(
                baseUrl = "https://example.com/",
                requestTimeoutMs = -1000
            )
        }

        assertTrue(
            exception.message?.contains("requestTimeoutMs must be positive") == true,
            "Should reject negative timeout"
        )
    }

    /**
     * Test validation: zero timeout should fail.
     */
    @Test
    fun testValidation_ZeroTimeout() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(
                baseUrl = "https://example.com/",
                requestTimeoutMs = 0
            )
        }

        assertTrue(
            exception.message?.contains("requestTimeoutMs must be positive") == true,
            "Should reject zero timeout"
        )
    }

    /**
     * Test validation: negative max retries should fail.
     */
    @Test
    fun testValidation_NegativeMaxRetries() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(
                baseUrl = "https://example.com/",
                maxRetries = -1
            )
        }

        assertTrue(
            exception.message?.contains("maxRetries must be non-negative") == true,
            "Should reject negative max retries"
        )
    }

    /**
     * Test validation: zero max retries is valid.
     */
    @Test
    fun testValidation_ZeroMaxRetries() {
        val config = DataLakeConfig(
            baseUrl = "https://example.com/",
            maxRetries = 0
        )

        assertEquals(0, config.maxRetries, "Zero max retries should be allowed")
    }

    /**
     * Test validation: zero max concurrent downloads should fail.
     */
    @Test
    fun testValidation_ZeroMaxConcurrentDownloads() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(
                baseUrl = "https://example.com/",
                maxConcurrentDownloads = 0
            )
        }

        assertTrue(
            exception.message?.contains("maxConcurrentDownloads must be positive") == true,
            "Should reject zero max concurrent downloads"
        )
    }

    /**
     * Test validation: negative max concurrent downloads should fail.
     */
    @Test
    fun testValidation_NegativeMaxConcurrentDownloads() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(
                baseUrl = "https://example.com/",
                maxConcurrentDownloads = -5
            )
        }

        assertTrue(
            exception.message?.contains("maxConcurrentDownloads must be positive") == true,
            "Should reject negative max concurrent downloads"
        )
    }

    /**
     * Test DataLakeSchema partition size calculation.
     */
    @Test
    fun testSchemaPartitionSizeCalculation() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        assertEquals(64000u, schema.partitionSize, "Partition size should be ledgersPerBatch * batchesPerPartition")
    }

    /**
     * Test DataLakeSchema with different batch configurations.
     */
    @Test
    fun testSchemaWithDifferentBatchConfigurations() {
        val testCases = listOf(
            Triple(1, 64000, 64000u),
            Triple(10, 6400, 64000u),
            Triple(100, 640, 64000u),
            Triple(1000, 64, 64000u)
        )

        for ((ledgersPerBatch, batchesPerPartition, expectedSize) in testCases) {
            val schema = DataLakeSchema(
                networkPassphrase = "Test SDF Network ; September 2015",
                version = "1.1",
                compression = "zstd",
                ledgersPerBatch = ledgersPerBatch,
                batchesPerPartition = batchesPerPartition
            )

            assertEquals(
                expectedSize,
                schema.partitionSize,
                "Schema with $ledgersPerBatch ledgers/batch and $batchesPerPartition batches/partition should have partition size $expectedSize"
            )
        }
    }

    /**
     * Test DataLakeSchema fields.
     */
    @Test
    fun testSchemaFields() {
        val schema = DataLakeSchema(
            networkPassphrase = "Public Global Stellar Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        assertEquals("Public Global Stellar Network ; September 2015", schema.networkPassphrase)
        assertEquals("1.1", schema.version)
        assertEquals("zstd", schema.compression)
        assertEquals(1, schema.ledgersPerBatch)
        assertEquals(64000, schema.batchesPerPartition)
    }

    /**
     * Test whitespace in URL should not be trimmed.
     */
    @Test
    fun testValidation_WhitespaceUrl() {
        val exception = assertFailsWith<IllegalArgumentException> {
            DataLakeConfig(baseUrl = "   ")
        }

        assertTrue(
            exception.message?.contains("baseUrl must not be blank") == true,
            "Should reject whitespace-only URL"
        )
    }

    /**
     * Test URL with trailing slash is accepted.
     */
    @Test
    fun testValidation_ValidUrlWithTrailingSlash() {
        val config = DataLakeConfig(baseUrl = "https://example.com/ledgers/")
        assertEquals("https://example.com/ledgers/", config.baseUrl)
    }

    /**
     * Test minimum valid timeout (1ms).
     */
    @Test
    fun testValidation_MinimumValidTimeout() {
        val config = DataLakeConfig(
            baseUrl = "https://example.com/",
            requestTimeoutMs = 1
        )

        assertEquals(1, config.requestTimeoutMs, "Minimum timeout of 1ms should be valid")
    }

    /**
     * Test large timeout value.
     */
    @Test
    fun testValidation_LargeTimeout() {
        val config = DataLakeConfig(
            baseUrl = "https://example.com/",
            requestTimeoutMs = 600_000 // 10 minutes
        )

        assertEquals(600_000, config.requestTimeoutMs, "Large timeout should be accepted")
    }

    /**
     * Test large max concurrent downloads.
     */
    @Test
    fun testValidation_LargeMaxConcurrentDownloads() {
        val config = DataLakeConfig(
            baseUrl = "https://example.com/",
            maxConcurrentDownloads = 100
        )

        assertEquals(100, config.maxConcurrentDownloads, "Large max concurrent downloads should be accepted")
    }
}
