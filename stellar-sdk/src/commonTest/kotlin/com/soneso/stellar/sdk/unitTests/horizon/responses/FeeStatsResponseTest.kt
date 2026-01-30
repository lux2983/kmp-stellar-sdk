package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.FeeStatsResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class FeeStatsResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val feeStatsJson = """
    {
        "last_ledger": 7505182,
        "last_ledger_base_fee": 100,
        "ledger_capacity_usage": "0.97",
        "fee_charged": {
            "min": 100,
            "max": 50000,
            "mode": 200,
            "p10": 100,
            "p20": 100,
            "p30": 150,
            "p40": 200,
            "p50": 200,
            "p60": 250,
            "p70": 300,
            "p80": 500,
            "p90": 1000,
            "p95": 5000,
            "p99": 25000
        },
        "max_fee": {
            "min": 200,
            "max": 100000,
            "mode": 500,
            "p10": 200,
            "p20": 300,
            "p30": 400,
            "p40": 500,
            "p50": 600,
            "p60": 700,
            "p70": 1000,
            "p80": 5000,
            "p90": 10000,
            "p95": 50000,
            "p99": 100000
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val feeStats = json.decodeFromString<FeeStatsResponse>(feeStatsJson)
        assertEquals(7505182L, feeStats.lastLedger)
        assertEquals(100L, feeStats.lastLedgerBaseFee)
        assertEquals("0.97", feeStats.ledgerCapacityUsage)
    }

    @Test
    fun testFeeCharged() {
        val feeStats = json.decodeFromString<FeeStatsResponse>(feeStatsJson)
        val fc = feeStats.feeCharged
        assertEquals(100L, fc.min)
        assertEquals(50000L, fc.max)
        assertEquals(200L, fc.mode)
        assertEquals(100L, fc.p10)
        assertEquals(100L, fc.p20)
        assertEquals(150L, fc.p30)
        assertEquals(200L, fc.p40)
        assertEquals(200L, fc.p50)
        assertEquals(250L, fc.p60)
        assertEquals(300L, fc.p70)
        assertEquals(500L, fc.p80)
        assertEquals(1000L, fc.p90)
        assertEquals(5000L, fc.p95)
        assertEquals(25000L, fc.p99)
    }

    @Test
    fun testMaxFee() {
        val feeStats = json.decodeFromString<FeeStatsResponse>(feeStatsJson)
        val mf = feeStats.maxFee
        assertEquals(200L, mf.min)
        assertEquals(100000L, mf.max)
        assertEquals(500L, mf.mode)
        assertEquals(200L, mf.p10)
        assertEquals(300L, mf.p20)
        assertEquals(400L, mf.p30)
        assertEquals(500L, mf.p40)
        assertEquals(600L, mf.p50)
        assertEquals(700L, mf.p60)
        assertEquals(1000L, mf.p70)
        assertEquals(5000L, mf.p80)
        assertEquals(10000L, mf.p90)
        assertEquals(50000L, mf.p95)
        assertEquals(100000L, mf.p99)
    }

    @Test
    fun testFeeDistributionEquality() {
        val d1 = FeeStatsResponse.FeeDistribution(100, 200, 150, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 195)
        val d2 = FeeStatsResponse.FeeDistribution(100, 200, 150, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 195)
        assertEquals(d1, d2)
    }
}
