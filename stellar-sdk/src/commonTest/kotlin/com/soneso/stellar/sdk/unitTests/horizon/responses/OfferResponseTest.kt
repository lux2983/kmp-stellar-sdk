package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.OfferResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OfferResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val offerJson = """
    {
        "id": "12345",
        "paging_token": "12345",
        "seller": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
        "selling": {
            "asset_type": "native"
        },
        "buying": {
            "asset_type": "credit_alphanum4",
            "asset_code": "USD",
            "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
        },
        "amount": "1000.0000000",
        "price_r": {
            "n": 1,
            "d": 2
        },
        "price": "0.5000000",
        "last_modified_ledger": 7654321,
        "last_modified_time": "2021-01-01T00:00:00Z",
        "sponsor": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
        "_links": {
            "self": {"href": "https://horizon.stellar.org/offers/12345"},
            "offer_maker": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val offer = json.decodeFromString<OfferResponse>(offerJson)
        assertEquals("12345", offer.id)
        assertEquals("12345", offer.pagingToken)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", offer.seller)
        assertEquals("1000.0000000", offer.amount)
        assertEquals("0.5000000", offer.price)
        assertEquals(7654321L, offer.lastModifiedLedger)
        assertEquals("2021-01-01T00:00:00Z", offer.lastModifiedTime)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", offer.sponsor)
    }

    @Test
    fun testSellingAsset() {
        val offer = json.decodeFromString<OfferResponse>(offerJson)
        assertEquals("native", offer.sellingAsset.assetType)
        assertTrue(offer.sellingAsset.isNative())
    }

    @Test
    fun testBuyingAsset() {
        val offer = json.decodeFromString<OfferResponse>(offerJson)
        assertEquals("credit_alphanum4", offer.buyingAsset.assetType)
        assertEquals("USD", offer.buyingAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", offer.buyingAsset.assetIssuer)
    }

    @Test
    fun testPriceR() {
        val offer = json.decodeFromString<OfferResponse>(offerJson)
        assertEquals(1L, offer.priceR.numerator)
        assertEquals(2L, offer.priceR.denominator)
    }

    @Test
    fun testLinks() {
        val offer = json.decodeFromString<OfferResponse>(offerJson)
        assertTrue(offer.links.self.href.contains("offers/12345"))
        assertTrue(offer.links.offerMaker.href.contains("accounts/"))
    }

    @Test
    fun testMinimalDeserialization() {
        val minimalJson = """
        {
            "id": "1",
            "paging_token": "1",
            "seller": "GTEST",
            "selling": {"asset_type": "native"},
            "buying": {"asset_type": "native"},
            "amount": "0",
            "price_r": {"n": 1, "d": 1},
            "price": "1.0",
            "_links": {
                "self": {"href": "https://example.com/self"},
                "offer_maker": {"href": "https://example.com/maker"}
            }
        }
        """.trimIndent()
        val offer = json.decodeFromString<OfferResponse>(minimalJson)
        assertEquals("1", offer.id)
        assertNull(offer.lastModifiedLedger)
        assertNull(offer.lastModifiedTime)
        assertNull(offer.sponsor)
    }
}
