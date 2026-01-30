package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.FeeStatsResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class FeeStatsResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testComprehensiveFeeStatsResponseDeserialization() {
        val feeStatsJson = """
        {
            "last_ledger": 32069474,
            "last_ledger_base_fee": 100,
            "ledger_capacity_usage": "0.97",
            "fee_charged": {
                "min": 100,
                "max": 50000,
                "mode": 100,
                "p10": 100,
                "p20": 100,
                "p30": 100,
                "p40": 100,
                "p50": 500,
                "p60": 1000,
                "p70": 2000,
                "p80": 5000,
                "p90": 10000,
                "p95": 20000,
                "p99": 50000
            },
            "max_fee": {
                "min": 100,
                "max": 100000,
                "mode": 1000,
                "p10": 500,
                "p20": 1000,
                "p30": 1000,
                "p40": 2000,
                "p50": 5000,
                "p60": 10000,
                "p70": 15000,
                "p80": 25000,
                "p90": 50000,
                "p95": 75000,
                "p99": 100000
            }
        }
        """.trimIndent()

        val feeStats = json.decodeFromString<FeeStatsResponse>(feeStatsJson)

        // Test ALL main properties
        assertEquals(32069474L, feeStats.lastLedger)
        assertEquals(100L, feeStats.lastLedgerBaseFee)
        assertEquals("0.97", feeStats.ledgerCapacityUsage)

        // Test feeCharged FeeDistribution inner class - ALL 14 fields
        assertEquals(100L, feeStats.feeCharged.min)
        assertEquals(50000L, feeStats.feeCharged.max)
        assertEquals(100L, feeStats.feeCharged.mode)
        assertEquals(100L, feeStats.feeCharged.p10)
        assertEquals(100L, feeStats.feeCharged.p20)
        assertEquals(100L, feeStats.feeCharged.p30)
        assertEquals(100L, feeStats.feeCharged.p40)
        assertEquals(500L, feeStats.feeCharged.p50)
        assertEquals(1000L, feeStats.feeCharged.p60)
        assertEquals(2000L, feeStats.feeCharged.p70)
        assertEquals(5000L, feeStats.feeCharged.p80)
        assertEquals(10000L, feeStats.feeCharged.p90)
        assertEquals(20000L, feeStats.feeCharged.p95)
        assertEquals(50000L, feeStats.feeCharged.p99)

        // Test maxFee FeeDistribution inner class - ALL 14 fields
        assertEquals(100L, feeStats.maxFee.min)
        assertEquals(100000L, feeStats.maxFee.max)
        assertEquals(1000L, feeStats.maxFee.mode)
        assertEquals(500L, feeStats.maxFee.p10)
        assertEquals(1000L, feeStats.maxFee.p20)
        assertEquals(1000L, feeStats.maxFee.p30)
        assertEquals(2000L, feeStats.maxFee.p40)
        assertEquals(5000L, feeStats.maxFee.p50)
        assertEquals(10000L, feeStats.maxFee.p60)
        assertEquals(15000L, feeStats.maxFee.p70)
        assertEquals(25000L, feeStats.maxFee.p80)
        assertEquals(50000L, feeStats.maxFee.p90)
        assertEquals(75000L, feeStats.maxFee.p95)
        assertEquals(100000L, feeStats.maxFee.p99)
    }

    @Test
    fun testFeeStatsResponseWithLowCapacityUsage() {
        val feeStatsJson = """
        {
            "last_ledger": 45000000,
            "last_ledger_base_fee": 100,
            "ledger_capacity_usage": "0.15",
            "fee_charged": {
                "min": 100,
                "max": 200,
                "mode": 100,
                "p10": 100,
                "p20": 100,
                "p30": 100,
                "p40": 100,
                "p50": 100,
                "p60": 100,
                "p70": 100,
                "p80": 100,
                "p90": 100,
                "p95": 150,
                "p99": 200
            },
            "max_fee": {
                "min": 100,
                "max": 1000,
                "mode": 100,
                "p10": 100,
                "p20": 100,
                "p30": 100,
                "p40": 100,
                "p50": 100,
                "p60": 100,
                "p70": 200,
                "p80": 500,
                "p90": 800,
                "p95": 900,
                "p99": 1000
            }
        }
        """.trimIndent()

        val feeStats = json.decodeFromString<FeeStatsResponse>(feeStatsJson)

        // Test main properties with different values
        assertEquals(45000000L, feeStats.lastLedger)
        assertEquals(100L, feeStats.lastLedgerBaseFee)
        assertEquals("0.15", feeStats.ledgerCapacityUsage)

        // Test feeCharged with low variation
        assertEquals(100L, feeStats.feeCharged.min)
        assertEquals(200L, feeStats.feeCharged.max)
        assertEquals(100L, feeStats.feeCharged.mode)
        assertEquals(100L, feeStats.feeCharged.p10)
        assertEquals(100L, feeStats.feeCharged.p20)
        assertEquals(100L, feeStats.feeCharged.p30)
        assertEquals(100L, feeStats.feeCharged.p40)
        assertEquals(100L, feeStats.feeCharged.p50)
        assertEquals(100L, feeStats.feeCharged.p60)
        assertEquals(100L, feeStats.feeCharged.p70)
        assertEquals(100L, feeStats.feeCharged.p80)
        assertEquals(100L, feeStats.feeCharged.p90)
        assertEquals(150L, feeStats.feeCharged.p95)
        assertEquals(200L, feeStats.feeCharged.p99)

        // Test maxFee with moderate variation
        assertEquals(100L, feeStats.maxFee.min)
        assertEquals(1000L, feeStats.maxFee.max)
        assertEquals(100L, feeStats.maxFee.mode)
        assertEquals(100L, feeStats.maxFee.p10)
        assertEquals(100L, feeStats.maxFee.p20)
        assertEquals(100L, feeStats.maxFee.p30)
        assertEquals(100L, feeStats.maxFee.p40)
        assertEquals(100L, feeStats.maxFee.p50)
        assertEquals(100L, feeStats.maxFee.p60)
        assertEquals(200L, feeStats.maxFee.p70)
        assertEquals(500L, feeStats.maxFee.p80)
        assertEquals(800L, feeStats.maxFee.p90)
        assertEquals(900L, feeStats.maxFee.p95)
        assertEquals(1000L, feeStats.maxFee.p99)
    }

    @Test
    fun testFeeStatsResponseWithHighCapacityUsage() {
        val feeStatsJson = """
        {
            "last_ledger": 99999999,
            "last_ledger_base_fee": 10000,
            "ledger_capacity_usage": "1.00",
            "fee_charged": {
                "min": 10000,
                "max": 1000000,
                "mode": 50000,
                "p10": 15000,
                "p20": 25000,
                "p30": 35000,
                "p40": 45000,
                "p50": 50000,
                "p60": 60000,
                "p70": 75000,
                "p80": 100000,
                "p90": 250000,
                "p95": 500000,
                "p99": 1000000
            },
            "max_fee": {
                "min": 10000,
                "max": 5000000,
                "mode": 100000,
                "p10": 25000,
                "p20": 50000,
                "p30": 75000,
                "p40": 100000,
                "p50": 150000,
                "p60": 200000,
                "p70": 300000,
                "p80": 500000,
                "p90": 1000000,
                "p95": 2500000,
                "p99": 5000000
            }
        }
        """.trimIndent()

        val feeStats = json.decodeFromString<FeeStatsResponse>(feeStatsJson)

        // Test main properties with extreme values
        assertEquals(99999999L, feeStats.lastLedger)
        assertEquals(10000L, feeStats.lastLedgerBaseFee)
        assertEquals("1.00", feeStats.ledgerCapacityUsage)

        // Test feeCharged with high fee variation
        assertEquals(10000L, feeStats.feeCharged.min)
        assertEquals(1000000L, feeStats.feeCharged.max)
        assertEquals(50000L, feeStats.feeCharged.mode)
        assertEquals(15000L, feeStats.feeCharged.p10)
        assertEquals(25000L, feeStats.feeCharged.p20)
        assertEquals(35000L, feeStats.feeCharged.p30)
        assertEquals(45000L, feeStats.feeCharged.p40)
        assertEquals(50000L, feeStats.feeCharged.p50)
        assertEquals(60000L, feeStats.feeCharged.p60)
        assertEquals(75000L, feeStats.feeCharged.p70)
        assertEquals(100000L, feeStats.feeCharged.p80)
        assertEquals(250000L, feeStats.feeCharged.p90)
        assertEquals(500000L, feeStats.feeCharged.p95)
        assertEquals(1000000L, feeStats.feeCharged.p99)

        // Test maxFee with very high values
        assertEquals(10000L, feeStats.maxFee.min)
        assertEquals(5000000L, feeStats.maxFee.max)
        assertEquals(100000L, feeStats.maxFee.mode)
        assertEquals(25000L, feeStats.maxFee.p10)
        assertEquals(50000L, feeStats.maxFee.p20)
        assertEquals(75000L, feeStats.maxFee.p30)
        assertEquals(100000L, feeStats.maxFee.p40)
        assertEquals(150000L, feeStats.maxFee.p50)
        assertEquals(200000L, feeStats.maxFee.p60)
        assertEquals(300000L, feeStats.maxFee.p70)
        assertEquals(500000L, feeStats.maxFee.p80)
        assertEquals(1000000L, feeStats.maxFee.p90)
        assertEquals(2500000L, feeStats.maxFee.p95)
        assertEquals(5000000L, feeStats.maxFee.p99)
    }

    @Test
    fun testFeeStatsResponseWithZeroCapacityUsage() {
        val feeStatsJson = """
        {
            "last_ledger": 1,
            "last_ledger_base_fee": 100,
            "ledger_capacity_usage": "0.00",
            "fee_charged": {
                "min": 100,
                "max": 100,
                "mode": 100,
                "p10": 100,
                "p20": 100,
                "p30": 100,
                "p40": 100,
                "p50": 100,
                "p60": 100,
                "p70": 100,
                "p80": 100,
                "p90": 100,
                "p95": 100,
                "p99": 100
            },
            "max_fee": {
                "min": 100,
                "max": 100,
                "mode": 100,
                "p10": 100,
                "p20": 100,
                "p30": 100,
                "p40": 100,
                "p50": 100,
                "p60": 100,
                "p70": 100,
                "p80": 100,
                "p90": 100,
                "p95": 100,
                "p99": 100
            }
        }
        """.trimIndent()

        val feeStats = json.decodeFromString<FeeStatsResponse>(feeStatsJson)

        // Test with minimal ledger and no capacity usage
        assertEquals(1L, feeStats.lastLedger)
        assertEquals(100L, feeStats.lastLedgerBaseFee)
        assertEquals("0.00", feeStats.ledgerCapacityUsage)

        // Test feeCharged with uniform distribution (all base fee)
        assertEquals(100L, feeStats.feeCharged.min)
        assertEquals(100L, feeStats.feeCharged.max)
        assertEquals(100L, feeStats.feeCharged.mode)
        assertEquals(100L, feeStats.feeCharged.p10)
        assertEquals(100L, feeStats.feeCharged.p20)
        assertEquals(100L, feeStats.feeCharged.p30)
        assertEquals(100L, feeStats.feeCharged.p40)
        assertEquals(100L, feeStats.feeCharged.p50)
        assertEquals(100L, feeStats.feeCharged.p60)
        assertEquals(100L, feeStats.feeCharged.p70)
        assertEquals(100L, feeStats.feeCharged.p80)
        assertEquals(100L, feeStats.feeCharged.p90)
        assertEquals(100L, feeStats.feeCharged.p95)
        assertEquals(100L, feeStats.feeCharged.p99)

        // Test maxFee with uniform distribution
        assertEquals(100L, feeStats.maxFee.min)
        assertEquals(100L, feeStats.maxFee.max)
        assertEquals(100L, feeStats.maxFee.mode)
        assertEquals(100L, feeStats.maxFee.p10)
        assertEquals(100L, feeStats.maxFee.p20)
        assertEquals(100L, feeStats.maxFee.p30)
        assertEquals(100L, feeStats.maxFee.p40)
        assertEquals(100L, feeStats.maxFee.p50)
        assertEquals(100L, feeStats.maxFee.p60)
        assertEquals(100L, feeStats.maxFee.p70)
        assertEquals(100L, feeStats.maxFee.p80)
        assertEquals(100L, feeStats.maxFee.p90)
        assertEquals(100L, feeStats.maxFee.p95)
        assertEquals(100L, feeStats.maxFee.p99)
    }

    @Test
    fun testFeeStatsResponseFeeDistributionDirectConstruction() {
        val feeDistribution = FeeStatsResponse.FeeDistribution(
            min = 100L,
            max = 10000L,
            mode = 500L,
            p10 = 200L,
            p20 = 300L,
            p30 = 400L,
            p40 = 450L,
            p50 = 500L,
            p60 = 600L,
            p70 = 750L,
            p80 = 1000L,
            p90 = 2000L,
            p95 = 5000L,
            p99 = 10000L
        )

        assertEquals(100L, feeDistribution.min)
        assertEquals(10000L, feeDistribution.max)
        assertEquals(500L, feeDistribution.mode)
        assertEquals(200L, feeDistribution.p10)
        assertEquals(300L, feeDistribution.p20)
        assertEquals(400L, feeDistribution.p30)
        assertEquals(450L, feeDistribution.p40)
        assertEquals(500L, feeDistribution.p50)
        assertEquals(600L, feeDistribution.p60)
        assertEquals(750L, feeDistribution.p70)
        assertEquals(1000L, feeDistribution.p80)
        assertEquals(2000L, feeDistribution.p90)
        assertEquals(5000L, feeDistribution.p95)
        assertEquals(10000L, feeDistribution.p99)
    }
}