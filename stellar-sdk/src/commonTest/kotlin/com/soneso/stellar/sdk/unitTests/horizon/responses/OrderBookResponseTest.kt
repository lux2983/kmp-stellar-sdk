package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.OrderBookResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderBookResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val orderBookJson = """
    {
        "base": {
            "asset_type": "native"
        },
        "counter": {
            "asset_type": "credit_alphanum4",
            "asset_code": "USD",
            "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
        },
        "bids": [
            {
                "amount": "100.0000000",
                "price": "0.5000000",
                "price_r": {"n": 1, "d": 2}
            },
            {
                "amount": "200.0000000",
                "price": "0.4500000",
                "price_r": {"n": 9, "d": 20}
            }
        ],
        "asks": [
            {
                "amount": "50.0000000",
                "price": "0.5500000",
                "price_r": {"n": 11, "d": 20}
            }
        ]
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val orderBook = json.decodeFromString<OrderBookResponse>(orderBookJson)
        assertEquals("native", orderBook.baseAsset.assetType)
        assertEquals("credit_alphanum4", orderBook.counterAsset.assetType)
        assertEquals("USD", orderBook.counterAsset.assetCode)
    }

    @Test
    fun testBids() {
        val orderBook = json.decodeFromString<OrderBookResponse>(orderBookJson)
        assertEquals(2, orderBook.bids.size)

        val bid1 = orderBook.bids[0]
        assertEquals("100.0000000", bid1.amount)
        assertEquals("0.5000000", bid1.price)
        assertEquals(1L, bid1.priceR.numerator)
        assertEquals(2L, bid1.priceR.denominator)

        val bid2 = orderBook.bids[1]
        assertEquals("200.0000000", bid2.amount)
        assertEquals("0.4500000", bid2.price)
        assertEquals(9L, bid2.priceR.numerator)
        assertEquals(20L, bid2.priceR.denominator)
    }

    @Test
    fun testAsks() {
        val orderBook = json.decodeFromString<OrderBookResponse>(orderBookJson)
        assertEquals(1, orderBook.asks.size)

        val ask = orderBook.asks[0]
        assertEquals("50.0000000", ask.amount)
        assertEquals("0.5500000", ask.price)
        assertEquals(11L, ask.priceR.numerator)
        assertEquals(20L, ask.priceR.denominator)
    }

    @Test
    fun testEmptyOrderBook() {
        val emptyJson = """
        {
            "base": {"asset_type": "native"},
            "counter": {"asset_type": "native"},
            "asks": [],
            "bids": []
        }
        """.trimIndent()
        val orderBook = json.decodeFromString<OrderBookResponse>(emptyJson)
        assertTrue(orderBook.bids.isEmpty())
        assertTrue(orderBook.asks.isEmpty())
    }

    @Test
    fun testRowEquality() {
        val r1 = OrderBookResponse.Row("100.0", "0.5", com.soneso.stellar.sdk.horizon.responses.Price(1, 2))
        val r2 = OrderBookResponse.Row("100.0", "0.5", com.soneso.stellar.sdk.horizon.responses.Price(1, 2))
        assertEquals(r1, r2)
    }
}
