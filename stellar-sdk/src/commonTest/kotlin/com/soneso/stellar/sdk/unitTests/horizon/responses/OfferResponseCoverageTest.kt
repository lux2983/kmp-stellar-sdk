package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.OfferResponse
import com.soneso.stellar.sdk.AssetTypeNative
import com.soneso.stellar.sdk.AssetTypeCreditAlphaNum4
import com.soneso.stellar.sdk.AssetTypeCreditAlphaNum12
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OfferResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testComprehensiveOfferResponseDeserialization() {
        val offerJson = """
        {
            "id": "4611686018427387905",
            "paging_token": "4611686018427387905",
            "seller": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "selling": {
                "asset_type": "credit_alphanum4",
                "asset_code": "USD",
                "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
            },
            "buying": {
                "asset_type": "native"
            },
            "amount": "1000.0000000",
            "price_r": {
                "n": 5,
                "d": 2
            },
            "price": "2.5000000",
            "last_modified_ledger": 32069474,
            "last_modified_time": "2021-01-01T12:00:00Z",
            "sponsor": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/offers/4611686018427387905",
                    "templated": false
                },
                "offer_maker": {
                    "href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"
                }
            }
        }
        """.trimIndent()

        val offer = json.decodeFromString<OfferResponse>(offerJson)

        // Test ALL main properties
        assertEquals("4611686018427387905", offer.id)
        assertEquals("4611686018427387905", offer.pagingToken)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", offer.seller)
        
        // Test selling asset properties
        assertEquals("credit_alphanum4", offer.sellingAsset.assetType)
        assertEquals("USD", offer.sellingAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", offer.sellingAsset.assetIssuer)
        
        // Test buying asset properties
        assertEquals("native", offer.buyingAsset.assetType)
        assertNull(offer.buyingAsset.assetCode)
        assertNull(offer.buyingAsset.assetIssuer)
        
        assertEquals("1000.0000000", offer.amount)
        assertEquals(5L, offer.priceR.numerator)
        assertEquals(2L, offer.priceR.denominator)
        assertEquals("2.5000000", offer.price)
        assertEquals(32069474L, offer.lastModifiedLedger)
        assertEquals("2021-01-01T12:00:00Z", offer.lastModifiedTime)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", offer.sponsor)

        // Test computed properties that use toSdkAsset()
        val buyingSdkAsset = offer.buying
        assertTrue(buyingSdkAsset is AssetTypeNative)
        
        val sellingSdkAsset = offer.selling
        assertTrue(sellingSdkAsset is AssetTypeCreditAlphaNum4)
        if (sellingSdkAsset is AssetTypeCreditAlphaNum4) {
            assertEquals("USD", sellingSdkAsset.code)
            assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", sellingSdkAsset.issuer)
        }

        // Test Links inner class
        assertEquals("https://horizon.stellar.org/offers/4611686018427387905", offer.links.self.href)
        assertEquals(false, offer.links.self.templated)
        assertEquals("https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", offer.links.offerMaker.href)
        assertNull(offer.links.offerMaker.templated)
    }

    @Test
    fun testOfferResponseWithCredit12AndNullableFields() {
        val offerJson = """
        {
            "id": "minimal-offer",
            "paging_token": "minimal-paging",
            "seller": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
            "selling": {
                "asset_type": "native"
            },
            "buying": {
                "asset_type": "credit_alphanum12",
                "asset_code": "LONGASSETCD",
                "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
            },
            "amount": "50.0000000",
            "price_r": {
                "n": 1,
                "d": 10
            },
            "price": "0.1000000",
            "last_modified_ledger": null,
            "last_modified_time": null,
            "sponsor": null,
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/offers/minimal",
                    "templated": true
                },
                "offer_maker": {
                    "href": "https://horizon.stellar.org/accounts/GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
                    "templated": false
                }
            }
        }
        """.trimIndent()

        val offer = json.decodeFromString<OfferResponse>(offerJson)

        // Test all properties including null values
        assertEquals("minimal-offer", offer.id)
        assertEquals("minimal-paging", offer.pagingToken)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", offer.seller)
        
        // Test selling asset (native)
        assertEquals("native", offer.sellingAsset.assetType)
        assertNull(offer.sellingAsset.assetCode)
        assertNull(offer.sellingAsset.assetIssuer)
        
        // Test buying asset (credit_alphanum12)
        assertEquals("credit_alphanum12", offer.buyingAsset.assetType)
        assertEquals("LONGASSETCD", offer.buyingAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", offer.buyingAsset.assetIssuer)
        
        assertEquals("50.0000000", offer.amount)
        assertEquals(1L, offer.priceR.numerator)
        assertEquals(10L, offer.priceR.denominator)
        assertEquals("0.1000000", offer.price)
        assertNull(offer.lastModifiedLedger)
        assertNull(offer.lastModifiedTime)
        assertNull(offer.sponsor)

        // Test computed properties with credit_alphanum12
        val sellingSdkAsset = offer.selling
        assertTrue(sellingSdkAsset is AssetTypeNative)
        
        val buyingSdkAsset = offer.buying
        assertTrue(buyingSdkAsset is AssetTypeCreditAlphaNum12)
        if (buyingSdkAsset is AssetTypeCreditAlphaNum12) {
            assertEquals("LONGASSETCD", buyingSdkAsset.code)
            assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", buyingSdkAsset.issuer)
        }

        // Test Links with templated values
        assertEquals("https://horizon.stellar.org/offers/minimal", offer.links.self.href)
        assertEquals(true, offer.links.self.templated)
        assertEquals("https://horizon.stellar.org/accounts/GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", offer.links.offerMaker.href)
        assertEquals(false, offer.links.offerMaker.templated)
    }

    @Test
    fun testOfferResponseLinksDirectConstruction() {
        val links = OfferResponse.Links(
            self = com.soneso.stellar.sdk.horizon.responses.Link("https://test.self", true),
            offerMaker = com.soneso.stellar.sdk.horizon.responses.Link("https://test.maker", false)
        )

        assertEquals("https://test.self", links.self.href)
        assertEquals("https://test.maker", links.offerMaker.href)
        assertEquals(true, links.self.templated)
        assertEquals(false, links.offerMaker.templated)
    }

    @Test
    fun testOfferResponseComputedPropertiesEdgeCases() {
        // Test with both assets as credit types
        val offerJson = """
        {
            "id": "edge-case-offer",
            "paging_token": "edge-case-paging",
            "seller": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "selling": {
                "asset_type": "credit_alphanum4",
                "asset_code": "EUR",
                "asset_issuer": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU"
            },
            "buying": {
                "asset_type": "credit_alphanum4",
                "asset_code": "USD",
                "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
            },
            "amount": "100.0000000",
            "price_r": {
                "n": 11,
                "d": 10
            },
            "price": "1.1000000",
            "_links": {
                "self": {
                    "href": "https://test.offer"
                },
                "offer_maker": {
                    "href": "https://test.maker"
                }
            }
        }
        """.trimIndent()

        val offer = json.decodeFromString<OfferResponse>(offerJson)

        // Test both computed properties with credit assets
        val sellingSdkAsset = offer.selling
        assertTrue(sellingSdkAsset is AssetTypeCreditAlphaNum4)
        if (sellingSdkAsset is AssetTypeCreditAlphaNum4) {
            assertEquals("EUR", sellingSdkAsset.code)
            assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", sellingSdkAsset.issuer)
        }
        
        val buyingSdkAsset = offer.buying
        assertTrue(buyingSdkAsset is AssetTypeCreditAlphaNum4)
        if (buyingSdkAsset is AssetTypeCreditAlphaNum4) {
            assertEquals("USD", buyingSdkAsset.code)
            assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", buyingSdkAsset.issuer)
        }
    }
}