package com.soneso.stellar.sdk.datalake

import kotlin.test.*

/**
 * Unit tests for DataLakePathCalculator.
 *
 * Tests SEP-54 path calculation with known test vectors from the implementation plan.
 */
class DataLakePathCalculatorTest {

    /**
     * Test SEP-54 path calculation for ledger 1,000,000.
     *
     * Expected path: FFF159FF--960000-1023999/FFF0BDBF--1000000.xdr.zst
     * - Partition size: 64000 ledgers (1000 ledgers/batch * 64 batches/partition)
     * - Partition start: 960000
     * - Partition end: 1023999
     * - Partition token: 0xFFFFFFFF - 960000 = 0xFFF159FF
     * - File token: 0xFFFFFFFF - 1000000 = 0xFFF0BDBF
     */
    @Test
    fun testCalculateLedgerPath_Ledger1000000() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        val ledgerSequence = 1_000_000u
        val baseUrl = "https://example.com/ledgers/"

        val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledgerSequence)

        assertEquals(
            "https://example.com/ledgers/FFF159FF--960000-1023999/FFF0BDBF--1000000.xdr.zst",
            url,
            "Path should match SEP-54 format for ledger 1,000,000"
        )
    }

    /**
     * Test SEP-54 path calculation for ledger 60,226,044.
     *
     * Expected path: FC690DFF--60224000-60287999/FC690603--60226044.xdr.zst
     * - Partition size: 64000 ledgers
     * - Partition start: 60224000
     * - Partition end: 60287999
     * - Partition token: 0xFFFFFFFF - 60224000 = 0xFC690DFF
     * - File token: 0xFFFFFFFF - 60226044 = 0xFC690603
     */
    @Test
    fun testCalculateLedgerPath_Ledger60226044() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        val ledgerSequence = 60_226_044u
        val baseUrl = "https://example.com/ledgers/"

        val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledgerSequence)

        assertEquals(
            "https://example.com/ledgers/FC690DFF--60224000-60287999/FC690603--60226044.xdr.zst",
            url,
            "Path should match SEP-54 format for ledger 60,226,044"
        )
    }

    /**
     * Test partition boundary calculation.
     *
     * Ledger 0 should be in partition 0-63999.
     */
    @Test
    fun testCalculateLedgerPath_LedgerZero() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        val ledgerSequence = 0u
        val baseUrl = "https://example.com/ledgers/"

        val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledgerSequence)

        // Partition token: 0xFFFFFFFF - 0 = 0xFFFFFFFF
        // File token: 0xFFFFFFFF - 0 = 0xFFFFFFFF
        assertEquals(
            "https://example.com/ledgers/FFFFFFFF--0-63999/FFFFFFFF--0.xdr.zst",
            url,
            "Ledger 0 should be in first partition"
        )
    }

    /**
     * Test partition boundary at exact partition start.
     */
    @Test
    fun testCalculateLedgerPath_PartitionBoundaryStart() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        val ledgerSequence = 64000u
        val baseUrl = "https://example.com/ledgers/"

        val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledgerSequence)

        // Partition token: 0xFFFFFFFF - 64000 = 0xFFFF05FF
        // File token: 0xFFFFFFFF - 64000 = 0xFFFF05FF
        assertEquals(
            "https://example.com/ledgers/FFFF05FF--64000-127999/FFFF05FF--64000.xdr.zst",
            url,
            "Ledger at partition start should be in correct partition"
        )
    }

    /**
     * Test partition boundary at exact partition end.
     */
    @Test
    fun testCalculateLedgerPath_PartitionBoundaryEnd() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        val ledgerSequence = 63999u
        val baseUrl = "https://example.com/ledgers/"

        val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledgerSequence)

        // Partition token: 0xFFFFFFFF - 0 = 0xFFFFFFFF
        // File token: 0xFFFFFFFF - 63999 = 0xFFFF0600
        assertEquals(
            "https://example.com/ledgers/FFFFFFFF--0-63999/FFFF0600--63999.xdr.zst",
            url,
            "Ledger at partition end should be in correct partition"
        )
    }

    /**
     * Test hex token calculation.
     *
     * The hex token is calculated as 0xFFFFFFFF - ledgerSequence,
     * formatted as 8-character uppercase hex string.
     */
    @Test
    fun testHexTokenCalculation() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        // Test various values
        val testCases = listOf(
            0u to "FFFFFFFF",
            1u to "FFFFFFFE",
            255u to "FFFFFF00",
            65535u to "FFFF0000",
            16777215u to "FF000000",
            4294967295u to "00000000" // Max uint32
        )

        val baseUrl = "https://example.com/"
        for ((ledger, expectedToken) in testCases) {
            val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledger)
            assertTrue(
                url.contains(expectedToken),
                "URL for ledger $ledger should contain hex token $expectedToken"
            )
        }
    }

    /**
     * Test batch start calculation for single-ledger batches.
     */
    @Test
    fun testGetBatchStartLedger_SingleLedgerBatch() {
        // With ledgersPerBatch = 1, batch start is always the ledger itself
        val testCases = listOf(
            0u to 0u,
            1u to 1u,
            100u to 100u,
            1000000u to 1000000u
        )

        for ((ledger, expectedStart) in testCases) {
            val batchStart = DataLakePathCalculator.getBatchStartLedger(ledger, 1u)
            assertEquals(
                expectedStart,
                batchStart,
                "Batch start for ledger $ledger should be $expectedStart"
            )
        }
    }

    /**
     * Test batch start calculation for multi-ledger batches.
     */
    @Test
    fun testGetBatchStartLedger_MultiLedgerBatch() {
        // With ledgersPerBatch = 10
        val ledgersPerBatch = 10u
        val testCases = listOf(
            0u to 0u,
            1u to 0u,
            9u to 0u,
            10u to 10u,
            11u to 10u,
            19u to 10u,
            20u to 20u,
            99u to 90u,
            100u to 100u
        )

        for ((ledger, expectedStart) in testCases) {
            val batchStart = DataLakePathCalculator.getBatchStartLedger(ledger, ledgersPerBatch)
            assertEquals(
                expectedStart,
                batchStart,
                "Batch start for ledger $ledger should be $expectedStart with batch size $ledgersPerBatch"
            )
        }
    }

    /**
     * Test path calculation with multi-ledger batches.
     */
    @Test
    fun testCalculateLedgerPath_MultiLedgerBatch() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 10,
            batchesPerPartition = 6400
        )

        val ledgerSequence = 1_000_005u
        val baseUrl = "https://example.com/ledgers/"

        val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledgerSequence)

        // Partition size: 10 * 6400 = 64000
        // Partition start: 960000
        // Batch start: 1000000 (floor(1000005 / 10) * 10)
        // Batch end: 1000009
        assertTrue(
            url.contains("960000-1023999"),
            "URL should contain correct partition range"
        )
        assertTrue(
            url.contains("1000000-1000009.xdr.zst"),
            "URL should contain correct batch range"
        )
    }

    /**
     * Test partition size calculation with different configurations.
     */
    @Test
    fun testPartitionSizeCalculation() {
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
                "Partition size should be $expectedSize for $ledgersPerBatch ledgers/batch and $batchesPerPartition batches/partition"
            )
        }
    }

    /**
     * Test max UInt value (edge case).
     */
    @Test
    fun testCalculateLedgerPath_MaxUInt() {
        val schema = DataLakeSchema(
            networkPassphrase = "Test SDF Network ; September 2015",
            version = "1.1",
            compression = "zstd",
            ledgersPerBatch = 1,
            batchesPerPartition = 64000
        )

        val ledgerSequence = UInt.MAX_VALUE
        val baseUrl = "https://example.com/ledgers/"

        val url = DataLakePathCalculator.getLedgerUrl(baseUrl, schema, ledgerSequence)

        // Token for max value: 0xFFFFFFFF - 0xFFFFFFFF = 0x00000000
        assertTrue(
            url.contains("00000000--${UInt.MAX_VALUE}.xdr.zst"),
            "URL should handle max UInt value correctly"
        )
    }
}
