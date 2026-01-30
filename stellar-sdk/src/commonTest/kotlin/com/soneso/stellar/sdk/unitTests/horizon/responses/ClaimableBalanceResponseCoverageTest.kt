package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.ClaimableBalanceResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ClaimableBalanceResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testComprehensiveClaimableBalanceResponseDeserialization() {
        val claimableBalanceJson = """
        {
            "id": "00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072",
            "asset": "USD:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
            "amount": "1000.0000000",
            "sponsor": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "last_modified_ledger": 32069474,
            "last_modified_time": "2021-01-01T12:00:00Z",
            "claimants": [
                {
                    "destination": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
                    "predicate": {
                        "unconditional": true
                    }
                },
                {
                    "destination": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
                    "predicate": {
                        "abs_before": "2025-01-01T00:00:00Z",
                        "rel_before": "3600"
                    }
                }
            ],
            "flags": {
                "clawback_enabled": true
            },
            "paging_token": "00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072",
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/claimable_balances/00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072"
                },
                "transactions": {
                    "href": "https://horizon.stellar.org/claimable_balances/00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072/transactions",
                    "templated": true
                },
                "operations": {
                    "href": "https://horizon.stellar.org/claimable_balances/00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072/operations",
                    "templated": false
                }
            }
        }
        """.trimIndent()

        val claimableBalance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)

        // Test ALL main properties
        assertEquals("00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072", claimableBalance.id)
        assertEquals("USD:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", claimableBalance.assetString)
        assertEquals("1000.0000000", claimableBalance.amount)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", claimableBalance.sponsor)
        assertEquals(32069474L, claimableBalance.lastModifiedLedger)
        assertEquals("2021-01-01T12:00:00Z", claimableBalance.lastModifiedTime)
        assertEquals("00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072", claimableBalance.pagingToken)

        // Test claimants list
        assertEquals(2, claimableBalance.claimants.size)
        
        val firstClaimant = claimableBalance.claimants[0]
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", firstClaimant.destination)
        assertEquals(true, firstClaimant.predicate.unconditional)
        assertNull(firstClaimant.predicate.absBefore)
        assertNull(firstClaimant.predicate.relBefore)
        assertNull(firstClaimant.predicate.and)
        assertNull(firstClaimant.predicate.or)
        assertNull(firstClaimant.predicate.not)
        
        val secondClaimant = claimableBalance.claimants[1]
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", secondClaimant.destination)
        assertNull(secondClaimant.predicate.unconditional)
        assertEquals("2025-01-01T00:00:00Z", secondClaimant.predicate.absBefore)
        assertEquals("3600", secondClaimant.predicate.relBefore)
        assertNull(secondClaimant.predicate.and)
        assertNull(secondClaimant.predicate.or)
        assertNull(secondClaimant.predicate.not)

        // Test Flags inner class
        assertEquals(true, claimableBalance.flags?.clawbackEnabled)

        // Test Links inner class
        assertEquals("https://horizon.stellar.org/claimable_balances/00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072", claimableBalance.links.self.href)
        assertEquals("https://horizon.stellar.org/claimable_balances/00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072/transactions", claimableBalance.links.transactions?.href)
        assertEquals("https://horizon.stellar.org/claimable_balances/00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072/operations", claimableBalance.links.operations?.href)
        assertNull(claimableBalance.links.self.templated)
        assertEquals(true, claimableBalance.links.transactions?.templated)
        assertEquals(false, claimableBalance.links.operations?.templated)
    }

    @Test
    fun testClaimableBalanceResponseWithNullableFields() {
        val claimableBalanceJson = """
        {
            "id": "minimal-claimable-balance-id",
            "asset": "native",
            "amount": "50.0000000",
            "sponsor": null,
            "last_modified_ledger": null,
            "last_modified_time": null,
            "claimants": [],
            "flags": null,
            "paging_token": "minimal-paging-token",
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/claimable_balances/minimal"
                },
                "transactions": null,
                "operations": null
            }
        }
        """.trimIndent()

        val claimableBalance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)

        // Test all properties with null/empty values
        assertEquals("minimal-claimable-balance-id", claimableBalance.id)
        assertEquals("native", claimableBalance.assetString)
        assertEquals("50.0000000", claimableBalance.amount)
        assertNull(claimableBalance.sponsor)
        assertNull(claimableBalance.lastModifiedLedger)
        assertNull(claimableBalance.lastModifiedTime)
        assertTrue(claimableBalance.claimants.isEmpty())
        assertNull(claimableBalance.flags)
        assertEquals("minimal-paging-token", claimableBalance.pagingToken)

        // Test Links with null optional fields
        assertEquals("https://horizon.stellar.org/claimable_balances/minimal", claimableBalance.links.self.href)
        assertNull(claimableBalance.links.transactions)
        assertNull(claimableBalance.links.operations)
    }

    @Test
    fun testClaimableBalanceResponseWithComplexPredicates() {
        val claimableBalanceJson = """
        {
            "id": "complex-predicate-id",
            "asset": "LONGASSETCODE:GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
            "amount": "2500.0000000",
            "sponsor": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
            "last_modified_ledger": 45000000,
            "last_modified_time": "2022-06-15T10:30:00Z",
            "claimants": [
                {
                    "destination": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
                    "predicate": {
                        "and": [
                            {
                                "abs_before": "2030-12-31T23:59:59Z"
                            },
                            {
                                "not": {
                                    "rel_before": "86400"
                                }
                            }
                        ]
                    }
                },
                {
                    "destination": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
                    "predicate": {
                        "or": [
                            {
                                "unconditional": true
                            },
                            {
                                "rel_before": "7200"
                            }
                        ]
                    }
                }
            ],
            "flags": {
                "clawback_enabled": false
            },
            "paging_token": "complex-predicate-paging",
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/claimable_balances/complex",
                    "templated": false
                },
                "transactions": {
                    "href": "https://horizon.stellar.org/claimable_balances/complex/transactions"
                },
                "operations": {
                    "href": "https://horizon.stellar.org/claimable_balances/complex/operations",
                    "templated": true
                }
            }
        }
        """.trimIndent()

        val claimableBalance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)

        // Test properties
        assertEquals("complex-predicate-id", claimableBalance.id)
        assertEquals("LONGASSETCODE:GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", claimableBalance.assetString)
        assertEquals("2500.0000000", claimableBalance.amount)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", claimableBalance.sponsor)
        assertEquals(45000000L, claimableBalance.lastModifiedLedger)
        assertEquals("2022-06-15T10:30:00Z", claimableBalance.lastModifiedTime)
        assertEquals("complex-predicate-paging", claimableBalance.pagingToken)

        // Test complex predicates
        assertEquals(2, claimableBalance.claimants.size)
        
        // First claimant with AND predicate
        val firstClaimant = claimableBalance.claimants[0]
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", firstClaimant.destination)
        assertNull(firstClaimant.predicate.unconditional)
        assertEquals(2, firstClaimant.predicate.and?.size)
        assertEquals("2030-12-31T23:59:59Z", firstClaimant.predicate.and?.get(0)?.absBefore)
        assertEquals("86400", firstClaimant.predicate.and?.get(1)?.not?.relBefore)

        // Second claimant with OR predicate
        val secondClaimant = claimableBalance.claimants[1]
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", secondClaimant.destination)
        assertEquals(2, secondClaimant.predicate.or?.size)
        assertEquals(true, secondClaimant.predicate.or?.get(0)?.unconditional)
        assertEquals("7200", secondClaimant.predicate.or?.get(1)?.relBefore)

        // Test Flags
        assertEquals(false, claimableBalance.flags?.clawbackEnabled)

        // Test Links
        assertEquals("https://horizon.stellar.org/claimable_balances/complex", claimableBalance.links.self.href)
        assertEquals(false, claimableBalance.links.self.templated)
        assertEquals("https://horizon.stellar.org/claimable_balances/complex/transactions", claimableBalance.links.transactions?.href)
        assertNull(claimableBalance.links.transactions?.templated)
        assertEquals("https://horizon.stellar.org/claimable_balances/complex/operations", claimableBalance.links.operations?.href)
        assertEquals(true, claimableBalance.links.operations?.templated)
    }

    @Test
    fun testClaimableBalanceResponseInnerClassesDirectConstruction() {
        // Test direct construction of Flags inner class
        val flags1 = ClaimableBalanceResponse.Flags(clawbackEnabled = true)
        assertEquals(true, flags1.clawbackEnabled)
        
        val flags2 = ClaimableBalanceResponse.Flags(clawbackEnabled = null)
        assertNull(flags2.clawbackEnabled)

        // Test direct construction of Links inner class
        val links = ClaimableBalanceResponse.Links(
            self = com.soneso.stellar.sdk.horizon.responses.Link("https://test.self", true),
            transactions = com.soneso.stellar.sdk.horizon.responses.Link("https://test.transactions", false),
            operations = com.soneso.stellar.sdk.horizon.responses.Link("https://test.operations", null)
        )
        assertEquals("https://test.self", links.self.href)
        assertEquals(true, links.self.templated)
        assertEquals("https://test.transactions", links.transactions?.href)
        assertEquals(false, links.transactions?.templated)
        assertEquals("https://test.operations", links.operations?.href)
        assertNull(links.operations?.templated)
        
        // Test Links with null optional fields
        val linksWithNulls = ClaimableBalanceResponse.Links(
            self = com.soneso.stellar.sdk.horizon.responses.Link("https://test.only.self", false),
            transactions = null,
            operations = null
        )
        assertEquals("https://test.only.self", linksWithNulls.self.href)
        assertEquals(false, linksWithNulls.self.templated)
        assertNull(linksWithNulls.transactions)
        assertNull(linksWithNulls.operations)
    }
}