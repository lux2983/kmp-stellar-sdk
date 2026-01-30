package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.ClaimableBalanceResponse
import com.soneso.stellar.sdk.horizon.responses.Claimant
import com.soneso.stellar.sdk.horizon.responses.Predicate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClaimableBalanceResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val claimableBalanceJson = """
    {
        "id": "00000000da0d57da7d4850e7fc10d2a9d0ebc731f7afb40574c03395b17d49149b91f5be",
        "asset": "native",
        "amount": "100.0000000",
        "sponsor": "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEBD9AFZQ7TM4JRS9",
        "last_modified_ledger": 28411995,
        "last_modified_time": "2020-02-26T19:29:16Z",
        "claimants": [
            {
                "destination": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
                "predicate": {
                    "unconditional": true
                }
            },
            {
                "destination": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
                "predicate": {
                    "and": [
                        {"not": {"abs_before": "2020-06-01T00:00:00Z"}},
                        {"abs_before": "2020-12-31T00:00:00Z"}
                    ]
                }
            }
        ],
        "flags": {
            "clawback_enabled": false
        },
        "paging_token": "28411995-00000000da0d57da7d4850e7fc10d2a9d0ebc731f7afb40574c03395b17d49149b91f5be",
        "_links": {
            "self": {"href": "https://horizon.stellar.org/claimable_balances/00000000da0d57da"},
            "transactions": {"href": "https://horizon.stellar.org/claimable_balances/00000000da0d57da/transactions"},
            "operations": {"href": "https://horizon.stellar.org/claimable_balances/00000000da0d57da/operations"}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val balance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)
        assertEquals("00000000da0d57da7d4850e7fc10d2a9d0ebc731f7afb40574c03395b17d49149b91f5be", balance.id)
        assertEquals("native", balance.assetString)
        assertEquals("100.0000000", balance.amount)
        assertEquals("GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEBD9AFZQ7TM4JRS9", balance.sponsor)
        assertEquals(28411995L, balance.lastModifiedLedger)
        assertEquals("2020-02-26T19:29:16Z", balance.lastModifiedTime)
    }

    @Test
    fun testClaimants() {
        val balance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)
        assertEquals(2, balance.claimants.size)

        val claimant1 = balance.claimants[0]
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", claimant1.destination)
        assertEquals(true, claimant1.predicate.unconditional)

        val claimant2 = balance.claimants[1]
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", claimant2.destination)
        assertEquals(2, claimant2.predicate.and?.size)
    }

    @Test
    fun testFlags() {
        val balance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)
        assertEquals(false, balance.flags?.clawbackEnabled)
    }

    @Test
    fun testLinks() {
        val balance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)
        assertTrue(balance.links.self.href.contains("claimable_balances"))
        assertTrue(balance.links.transactions?.href?.contains("transactions") == true)
        assertTrue(balance.links.operations?.href?.contains("operations") == true)
    }

    @Test
    fun testPagingToken() {
        val balance = json.decodeFromString<ClaimableBalanceResponse>(claimableBalanceJson)
        assertTrue(balance.pagingToken.contains("28411995"))
    }

    @Test
    fun testMinimalDeserialization() {
        val minimalJson = """
        {
            "id": "test-id",
            "asset": "USD:GTEST",
            "amount": "50.0",
            "paging_token": "token",
            "_links": {
                "self": {"href": "https://example.com/self"}
            }
        }
        """.trimIndent()
        val balance = json.decodeFromString<ClaimableBalanceResponse>(minimalJson)
        assertEquals("test-id", balance.id)
        assertEquals("USD:GTEST", balance.assetString)
        assertNull(balance.sponsor)
        assertNull(balance.lastModifiedLedger)
        assertNull(balance.lastModifiedTime)
        assertTrue(balance.claimants.isEmpty())
        assertNull(balance.flags)
        assertNull(balance.links.transactions)
        assertNull(balance.links.operations)
    }
}
