package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.TradeAggregationResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeAggregationResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testComprehensiveTradeAggregationResponseDeserialization() {
        val tradeAggregationJson = """
        {
            "timestamp": 1609459200000,
            "trade_count": 250,
            "base_volume": "12500.0000000",
            "counter_volume": "31250.0000000",
            "avg": "2.5000000",
            "high": "2.7500000",
            "high_r": {
                "n": 11,
                "d": 4
            },
            "low": "2.2500000",
            "low_r": {
                "n": 9,
                "d": 4
            },
            "open": "2.4000000",
            "open_r": {
                "n": 12,
                "d": 5
            },
            "close": "2.6000000",
            "close_r": {
                "n": 13,
                "d": 5
            }
        }
        """.trimIndent()

        val tradeAggregation = json.decodeFromString<TradeAggregationResponse>(tradeAggregationJson)

        // Test ALL properties to achieve complete coverage
        assertEquals(1609459200000L, tradeAggregation.timestamp)
        assertEquals(250, tradeAggregation.tradeCount)
        assertEquals("12500.0000000", tradeAggregation.baseVolume)
        assertEquals("31250.0000000", tradeAggregation.counterVolume)
        assertEquals("2.5000000", tradeAggregation.avg)
        assertEquals("2.7500000", tradeAggregation.high)
        assertEquals("2.2500000", tradeAggregation.low)
        assertEquals("2.4000000", tradeAggregation.open)
        assertEquals("2.6000000", tradeAggregation.close)

        // Test all Price objects (highR, lowR, openR, closeR)
        assertEquals(11L, tradeAggregation.highR.numerator)
        assertEquals(4L, tradeAggregation.highR.denominator)
        
        assertEquals(9L, tradeAggregation.lowR.numerator)
        assertEquals(4L, tradeAggregation.lowR.denominator)
        
        assertEquals(12L, tradeAggregation.openR.numerator)
        assertEquals(5L, tradeAggregation.openR.denominator)
        
        assertEquals(13L, tradeAggregation.closeR.numerator)
        assertEquals(5L, tradeAggregation.closeR.denominator)
    }

    @Test
    fun testTradeAggregationResponseWithZeroValues() {
        val tradeAggregationJson = """
        {
            "timestamp": 0,
            "trade_count": 0,
            "base_volume": "0.0000000",
            "counter_volume": "0.0000000",
            "avg": "0.0000000",
            "high": "0.0000000",
            "high_r": {
                "n": 0,
                "d": 1
            },
            "low": "0.0000000",
            "low_r": {
                "n": 0,
                "d": 1
            },
            "open": "0.0000000",
            "open_r": {
                "n": 0,
                "d": 1
            },
            "close": "0.0000000",
            "close_r": {
                "n": 0,
                "d": 1
            }
        }
        """.trimIndent()

        val tradeAggregation = json.decodeFromString<TradeAggregationResponse>(tradeAggregationJson)

        // Test all properties with zero/minimal values
        assertEquals(0L, tradeAggregation.timestamp)
        assertEquals(0, tradeAggregation.tradeCount)
        assertEquals("0.0000000", tradeAggregation.baseVolume)
        assertEquals("0.0000000", tradeAggregation.counterVolume)
        assertEquals("0.0000000", tradeAggregation.avg)
        assertEquals("0.0000000", tradeAggregation.high)
        assertEquals("0.0000000", tradeAggregation.low)
        assertEquals("0.0000000", tradeAggregation.open)
        assertEquals("0.0000000", tradeAggregation.close)

        // Test all Price objects with zero numerators
        assertEquals(0L, tradeAggregation.highR.numerator)
        assertEquals(1L, tradeAggregation.highR.denominator)
        
        assertEquals(0L, tradeAggregation.lowR.numerator)
        assertEquals(1L, tradeAggregation.lowR.denominator)
        
        assertEquals(0L, tradeAggregation.openR.numerator)
        assertEquals(1L, tradeAggregation.openR.denominator)
        
        assertEquals(0L, tradeAggregation.closeR.numerator)
        assertEquals(1L, tradeAggregation.closeR.denominator)
    }

    @Test
    fun testTradeAggregationResponseWithLargeValues() {
        val tradeAggregationJson = """
        {
            "timestamp": 9223372036854775807,
            "trade_count": 2147483647,
            "base_volume": "999999999.9999999",
            "counter_volume": "888888888.8888888",
            "avg": "1.1234567",
            "high": "5.9999999",
            "high_r": {
                "n": 9223372036854775807,
                "d": 1000000000000000000
            },
            "low": "0.0000001",
            "low_r": {
                "n": 1,
                "d": 9223372036854775807
            },
            "open": "3.3333333",
            "open_r": {
                "n": 33333333,
                "d": 10000000
            },
            "close": "4.7777777",
            "close_r": {
                "n": 47777777,
                "d": 10000000
            }
        }
        """.trimIndent()

        val tradeAggregation = json.decodeFromString<TradeAggregationResponse>(tradeAggregationJson)

        // Test all properties with large/edge case values
        assertEquals(9223372036854775807L, tradeAggregation.timestamp)
        assertEquals(2147483647, tradeAggregation.tradeCount)
        assertEquals("999999999.9999999", tradeAggregation.baseVolume)
        assertEquals("888888888.8888888", tradeAggregation.counterVolume)
        assertEquals("1.1234567", tradeAggregation.avg)
        assertEquals("5.9999999", tradeAggregation.high)
        assertEquals("0.0000001", tradeAggregation.low)
        assertEquals("3.3333333", tradeAggregation.open)
        assertEquals("4.7777777", tradeAggregation.close)

        // Test all Price objects with large values
        assertEquals(9223372036854775807L, tradeAggregation.highR.numerator)
        assertEquals(1000000000000000000L, tradeAggregation.highR.denominator)
        
        assertEquals(1L, tradeAggregation.lowR.numerator)
        assertEquals(9223372036854775807L, tradeAggregation.lowR.denominator)
        
        assertEquals(33333333L, tradeAggregation.openR.numerator)
        assertEquals(10000000L, tradeAggregation.openR.denominator)
        
        assertEquals(47777777L, tradeAggregation.closeR.numerator)
        assertEquals(10000000L, tradeAggregation.closeR.denominator)
    }

    @Test
    fun testTradeAggregationResponseEdgeCaseScenarios() {
        // Test with minimal trade activity
        val tradeAggregationJson = """
        {
            "timestamp": 1672531200000,
            "trade_count": 1,
            "base_volume": "1.0000000",
            "counter_volume": "1.0000000",
            "avg": "1.0000000",
            "high": "1.0000000",
            "high_r": {
                "n": 1,
                "d": 1
            },
            "low": "1.0000000",
            "low_r": {
                "n": 1,
                "d": 1
            },
            "open": "1.0000000",
            "open_r": {
                "n": 1,
                "d": 1
            },
            "close": "1.0000000",
            "close_r": {
                "n": 1,
                "d": 1
            }
        }
        """.trimIndent()

        val tradeAggregation = json.decodeFromString<TradeAggregationResponse>(tradeAggregationJson)

        // Verify single trade scenario (all prices should be equal)
        assertEquals(1672531200000L, tradeAggregation.timestamp)
        assertEquals(1, tradeAggregation.tradeCount)
        assertEquals("1.0000000", tradeAggregation.baseVolume)
        assertEquals("1.0000000", tradeAggregation.counterVolume)
        assertEquals("1.0000000", tradeAggregation.avg)
        assertEquals("1.0000000", tradeAggregation.high)
        assertEquals("1.0000000", tradeAggregation.low)
        assertEquals("1.0000000", tradeAggregation.open)
        assertEquals("1.0000000", tradeAggregation.close)

        // All Price ratios should be 1/1
        assertEquals(1L, tradeAggregation.highR.numerator)
        assertEquals(1L, tradeAggregation.highR.denominator)
        assertEquals(1L, tradeAggregation.lowR.numerator)
        assertEquals(1L, tradeAggregation.lowR.denominator)
        assertEquals(1L, tradeAggregation.openR.numerator)
        assertEquals(1L, tradeAggregation.openR.denominator)
        assertEquals(1L, tradeAggregation.closeR.numerator)
        assertEquals(1L, tradeAggregation.closeR.denominator)
    }
}