package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.TradeAggregationResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeAggregationResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val tradeAggJson = """
    {
        "timestamp": 1609459200000,
        "trade_count": 150,
        "base_volume": "5000.0000000",
        "counter_volume": "2500.0000000",
        "avg": "0.5000000",
        "high": "0.6000000",
        "high_r": {"n": 3, "d": 5},
        "low": "0.4000000",
        "low_r": {"n": 2, "d": 5},
        "open": "0.4500000",
        "open_r": {"n": 9, "d": 20},
        "close": "0.5500000",
        "close_r": {"n": 11, "d": 20}
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val agg = json.decodeFromString<TradeAggregationResponse>(tradeAggJson)
        assertEquals(1609459200000L, agg.timestamp)
        assertEquals(150, agg.tradeCount)
        assertEquals("5000.0000000", agg.baseVolume)
        assertEquals("2500.0000000", agg.counterVolume)
        assertEquals("0.5000000", agg.avg)
    }

    @Test
    fun testOhlcValues() {
        val agg = json.decodeFromString<TradeAggregationResponse>(tradeAggJson)
        assertEquals("0.6000000", agg.high)
        assertEquals(3L, agg.highR.numerator)
        assertEquals(5L, agg.highR.denominator)

        assertEquals("0.4000000", agg.low)
        assertEquals(2L, agg.lowR.numerator)
        assertEquals(5L, agg.lowR.denominator)

        assertEquals("0.4500000", agg.open)
        assertEquals(9L, agg.openR.numerator)
        assertEquals(20L, agg.openR.denominator)

        assertEquals("0.5500000", agg.close)
        assertEquals(11L, agg.closeR.numerator)
        assertEquals(20L, agg.closeR.denominator)
    }

    @Test
    fun testDataClassEquality() {
        val a1 = json.decodeFromString<TradeAggregationResponse>(tradeAggJson)
        val a2 = json.decodeFromString<TradeAggregationResponse>(tradeAggJson)
        assertEquals(a1, a2)
    }
}
