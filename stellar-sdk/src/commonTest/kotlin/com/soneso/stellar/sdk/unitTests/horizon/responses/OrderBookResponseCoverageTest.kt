package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.OrderBookResponse
import com.soneso.stellar.sdk.AssetTypeNative
import com.soneso.stellar.sdk.AssetTypeCreditAlphaNum4
import com.soneso.stellar.sdk.AssetTypeCreditAlphaNum12
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderBookResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testComprehensiveOrderBookResponseDeserialization() {
        val orderBookJson = """
        {
            "base": {
                "asset_type": "native"
            },
            "counter": {
                "asset_type": "credit_alphanum4",
                "asset_code": "USD",
                "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
            },
            "asks": [
                {
                    "amount": "1000.0000000",
                    "price": "2.5000000",
                    "price_r": {
                        "n": 5,
                        "d": 2
                    }
                },
                {
                    "amount": "500.0000000",
                    "price": "2.6000000",
                    "price_r": {
                        "n": 13,
                        "d": 5
                    }
                },
                {
                    "amount": "750.0000000",
                    "price": "2.7500000",
                    "price_r": {
                        "n": 11,
                        "d": 4
                    }
                }
            ],
            "bids": [
                {
                    "amount": "800.0000000",
                    "price": "2.4000000",
                    "price_r": {
                        "n": 12,
                        "d": 5
                    }
                },
                {
                    "amount": "600.0000000",
                    "price": "2.3000000",
                    "price_r": {
                        "n": 23,
                        "d": 10
                    }
                }
            ]
        }
        """.trimIndent()

        val orderBook = json.decodeFromString<OrderBookResponse>(orderBookJson)

        // Test base asset properties
        assertEquals("native", orderBook.baseAsset.assetType)
        assertEquals(null, orderBook.baseAsset.assetCode)
        assertEquals(null, orderBook.baseAsset.assetIssuer)

        // Test counter asset properties
        assertEquals("credit_alphanum4", orderBook.counterAsset.assetType)
        assertEquals("USD", orderBook.counterAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", orderBook.counterAsset.assetIssuer)

        // Test computed properties
        val baseSdkAsset = orderBook.base
        assertTrue(baseSdkAsset is AssetTypeNative)
        
        val counterSdkAsset = orderBook.counter
        assertTrue(counterSdkAsset is AssetTypeCreditAlphaNum4)
        if (counterSdkAsset is AssetTypeCreditAlphaNum4) {
            assertEquals("USD", counterSdkAsset.code)
            assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", counterSdkAsset.issuer)
        }

        // Test asks list and Row inner class
        assertEquals(3, orderBook.asks.size)
        
        val firstAsk = orderBook.asks[0]
        assertEquals("1000.0000000", firstAsk.amount)
        assertEquals("2.5000000", firstAsk.price)
        assertEquals(5L, firstAsk.priceR.numerator)
        assertEquals(2L, firstAsk.priceR.denominator)
        
        val secondAsk = orderBook.asks[1]
        assertEquals("500.0000000", secondAsk.amount)
        assertEquals("2.6000000", secondAsk.price)
        assertEquals(13L, secondAsk.priceR.numerator)
        assertEquals(5L, secondAsk.priceR.denominator)
        
        val thirdAsk = orderBook.asks[2]
        assertEquals("750.0000000", thirdAsk.amount)
        assertEquals("2.7500000", thirdAsk.price)
        assertEquals(11L, thirdAsk.priceR.numerator)
        assertEquals(4L, thirdAsk.priceR.denominator)

        // Test bids list and Row inner class
        assertEquals(2, orderBook.bids.size)
        
        val firstBid = orderBook.bids[0]
        assertEquals("800.0000000", firstBid.amount)
        assertEquals("2.4000000", firstBid.price)
        assertEquals(12L, firstBid.priceR.numerator)
        assertEquals(5L, firstBid.priceR.denominator)
        
        val secondBid = orderBook.bids[1]
        assertEquals("600.0000000", secondBid.amount)
        assertEquals("2.3000000", secondBid.price)
        assertEquals(23L, secondBid.priceR.numerator)
        assertEquals(10L, secondBid.priceR.denominator)
    }

    @Test
    fun testOrderBookResponseWithCredit12Assets() {
        val orderBookJson = """
        {
            "base": {
                "asset_type": "credit_alphanum12",
                "asset_code": "LONGASSETCD",
                "asset_issuer": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU"
            },
            "counter": {
                "asset_type": "credit_alphanum4",
                "asset_code": "EUR",
                "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
            },
            "asks": [
                {
                    "amount": "100.0000000",
                    "price": "0.8500000",
                    "price_r": {
                        "n": 17,
                        "d": 20
                    }
                }
            ],
            "bids": [
                {
                    "amount": "200.0000000",
                    "price": "0.8000000",
                    "price_r": {
                        "n": 4,
                        "d": 5
                    }
                }
            ]
        }
        """.trimIndent()

        val orderBook = json.decodeFromString<OrderBookResponse>(orderBookJson)

        // Test base asset (credit_alphanum12)
        assertEquals("credit_alphanum12", orderBook.baseAsset.assetType)
        assertEquals("LONGASSETCD", orderBook.baseAsset.assetCode)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", orderBook.baseAsset.assetIssuer)

        // Test counter asset (credit_alphanum4)
        assertEquals("credit_alphanum4", orderBook.counterAsset.assetType)
        assertEquals("EUR", orderBook.counterAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", orderBook.counterAsset.assetIssuer)

        // Test computed properties with credit assets
        val baseSdkAsset = orderBook.base
        assertTrue(baseSdkAsset is AssetTypeCreditAlphaNum12)
        if (baseSdkAsset is AssetTypeCreditAlphaNum12) {
            assertEquals("LONGASSETCD", baseSdkAsset.code)
            assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", baseSdkAsset.issuer)
        }
        
        val counterSdkAsset = orderBook.counter
        assertTrue(counterSdkAsset is AssetTypeCreditAlphaNum4)
        if (counterSdkAsset is AssetTypeCreditAlphaNum4) {
            assertEquals("EUR", counterSdkAsset.code)
            assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", counterSdkAsset.issuer)
        }

        // Test single ask and bid
        assertEquals(1, orderBook.asks.size)
        assertEquals(1, orderBook.bids.size)
        
        assertEquals("100.0000000", orderBook.asks[0].amount)
        assertEquals("0.8500000", orderBook.asks[0].price)
        assertEquals(17L, orderBook.asks[0].priceR.numerator)
        assertEquals(20L, orderBook.asks[0].priceR.denominator)
        
        assertEquals("200.0000000", orderBook.bids[0].amount)
        assertEquals("0.8000000", orderBook.bids[0].price)
        assertEquals(4L, orderBook.bids[0].priceR.numerator)
        assertEquals(5L, orderBook.bids[0].priceR.denominator)
    }

    @Test
    fun testOrderBookResponseWithEmptyLists() {
        val orderBookJson = """
        {
            "base": {
                "asset_type": "native"
            },
            "counter": {
                "asset_type": "native"
            },
            "asks": [],
            "bids": []
        }
        """.trimIndent()

        val orderBook = json.decodeFromString<OrderBookResponse>(orderBookJson)

        // Test both assets as native
        assertEquals("native", orderBook.baseAsset.assetType)
        assertEquals("native", orderBook.counterAsset.assetType)

        // Test computed properties with both native
        val baseSdkAsset = orderBook.base
        val counterSdkAsset = orderBook.counter
        assertTrue(baseSdkAsset is AssetTypeNative)
        assertTrue(counterSdkAsset is AssetTypeNative)

        // Test empty lists
        assertTrue(orderBook.asks.isEmpty())
        assertTrue(orderBook.bids.isEmpty())
    }

    @Test
    fun testOrderBookResponseWithExtremePrices() {
        val orderBookJson = """
        {
            "base": {
                "asset_type": "credit_alphanum4",
                "asset_code": "BTC",
                "asset_issuer": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"
            },
            "counter": {
                "asset_type": "credit_alphanum4",
                "asset_code": "USD",
                "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
            },
            "asks": [
                {
                    "amount": "0.0000001",
                    "price": "99999.9999999",
                    "price_r": {
                        "n": 999999999999,
                        "d": 10000000
                    }
                }
            ],
            "bids": [
                {
                    "amount": "999999.9999999",
                    "price": "0.0000001",
                    "price_r": {
                        "n": 1,
                        "d": 10000000
                    }
                }
            ]
        }
        """.trimIndent()

        val orderBook = json.decodeFromString<OrderBookResponse>(orderBookJson)

        // Test assets
        assertEquals("credit_alphanum4", orderBook.baseAsset.assetType)
        assertEquals("BTC", orderBook.baseAsset.assetCode)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", orderBook.baseAsset.assetIssuer)
        
        assertEquals("credit_alphanum4", orderBook.counterAsset.assetType)
        assertEquals("USD", orderBook.counterAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", orderBook.counterAsset.assetIssuer)

        // Test computed properties
        val baseSdkAsset = orderBook.base
        val counterSdkAsset = orderBook.counter
        assertTrue(baseSdkAsset is AssetTypeCreditAlphaNum4)
        assertTrue(counterSdkAsset is AssetTypeCreditAlphaNum4)

        // Test extreme price values
        assertEquals("0.0000001", orderBook.asks[0].amount)
        assertEquals("99999.9999999", orderBook.asks[0].price)
        assertEquals(999999999999L, orderBook.asks[0].priceR.numerator)
        assertEquals(10000000L, orderBook.asks[0].priceR.denominator)
        
        assertEquals("999999.9999999", orderBook.bids[0].amount)
        assertEquals("0.0000001", orderBook.bids[0].price)
        assertEquals(1L, orderBook.bids[0].priceR.numerator)
        assertEquals(10000000L, orderBook.bids[0].priceR.denominator)
    }

    @Test
    fun testOrderBookResponseRowDirectConstruction() {
        val row = OrderBookResponse.Row(
            amount = "1000.0000000",
            price = "2.5000000",
            priceR = com.soneso.stellar.sdk.horizon.responses.Price(
                numerator = 5L,
                denominator = 2L
            )
        )

        assertEquals("1000.0000000", row.amount)
        assertEquals("2.5000000", row.price)
        assertEquals(5L, row.priceR.numerator)
        assertEquals(2L, row.priceR.denominator)
    }
}