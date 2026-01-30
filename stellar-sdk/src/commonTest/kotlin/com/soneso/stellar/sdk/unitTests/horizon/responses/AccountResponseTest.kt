package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.AccountResponse
import com.soneso.stellar.sdk.horizon.responses.Link
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val accountJson = """
    {
        "id": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
        "account_id": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
        "sequence": 3298702387052545,
        "sequence_ledger": 7654321,
        "sequence_time": 1609459200,
        "subentry_count": 1,
        "inflation_destination": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
        "home_domain": "example.com",
        "last_modified_ledger": 7654321,
        "last_modified_time": "2021-01-01T00:00:00Z",
        "thresholds": {
            "low_threshold": 0,
            "med_threshold": 0,
            "high_threshold": 0
        },
        "flags": {
            "auth_required": false,
            "auth_revocable": false,
            "auth_immutable": false,
            "auth_clawback_enabled": false
        },
        "balances": [
            {
                "asset_type": "credit_alphanum4",
                "asset_code": "USD",
                "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
                "balance": "100.0000000",
                "buying_liabilities": "0.0000000",
                "selling_liabilities": "0.0000000",
                "limit": "922337203685.4775807",
                "is_authorized": true,
                "is_authorized_to_maintain_liabilities": true,
                "is_clawback_enabled": false,
                "last_modified_ledger": 7654320
            },
            {
                "asset_type": "native",
                "balance": "999.9999900",
                "buying_liabilities": "0.0000000",
                "selling_liabilities": "0.0000000"
            }
        ],
        "signers": [
            {
                "key": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
                "type": "ed25519_public_key",
                "weight": 1
            }
        ],
        "data": {
            "config": "dGVzdA=="
        },
        "num_sponsoring": 0,
        "num_sponsored": 0,
        "paging_token": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
        "_links": {
            "self": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"},
            "transactions": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7/transactions{?cursor,limit,order}", "templated": true},
            "operations": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7/operations{?cursor,limit,order}", "templated": true},
            "payments": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7/payments{?cursor,limit,order}", "templated": true},
            "effects": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7/effects{?cursor,limit,order}", "templated": true},
            "offers": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7/offers{?cursor,limit,order}", "templated": true},
            "trades": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7/trades{?cursor,limit,order}", "templated": true},
            "data": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7/data/{key}", "templated": true}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", account.id)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", account.accountId)
        assertEquals(3298702387052545L, account.sequenceNumber)
        assertEquals(7654321L, account.sequenceLedger)
        assertEquals(1609459200L, account.sequenceTime)
        assertEquals(1, account.subentryCount)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", account.inflationDestination)
        assertEquals("example.com", account.homeDomain)
        assertEquals(7654321, account.lastModifiedLedger)
        assertEquals("2021-01-01T00:00:00Z", account.lastModifiedTime)
        assertEquals(0, account.numSponsoring)
        assertEquals(0, account.numSponsored)
        assertNull(account.sponsor)
    }

    @Test
    fun testThresholds() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertEquals(0, account.thresholds.lowThreshold)
        assertEquals(0, account.thresholds.medThreshold)
        assertEquals(0, account.thresholds.highThreshold)
    }

    @Test
    fun testFlags() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertFalse(account.flags.authRequired)
        assertFalse(account.flags.authRevocable)
        assertFalse(account.flags.authImmutable)
        assertFalse(account.flags.authClawbackEnabled)
    }

    @Test
    fun testBalances() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertEquals(2, account.balances.size)

        val creditBalance = account.balances[0]
        assertEquals("credit_alphanum4", creditBalance.assetType)
        assertEquals("USD", creditBalance.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", creditBalance.assetIssuer)
        assertEquals("100.0000000", creditBalance.balance)
        assertEquals("0.0000000", creditBalance.buyingLiabilities)
        assertEquals("0.0000000", creditBalance.sellingLiabilities)
        assertEquals(true, creditBalance.isAuthorized)
        assertEquals(true, creditBalance.isAuthorizedToMaintainLiabilities)
        assertEquals(false, creditBalance.isClawbackEnabled)
        assertEquals(7654320, creditBalance.lastModifiedLedger)

        val nativeBalance = account.balances[1]
        assertEquals("native", nativeBalance.assetType)
        assertNull(nativeBalance.assetCode)
        assertNull(nativeBalance.assetIssuer)
        assertEquals("999.9999900", nativeBalance.balance)
    }

    @Test
    fun testSigners() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertEquals(1, account.signers.size)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", account.signers[0].key)
        assertEquals("ed25519_public_key", account.signers[0].type)
        assertEquals(1, account.signers[0].weight)
        assertNull(account.signers[0].sponsor)
    }

    @Test
    fun testData() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertEquals(1, account.data.size)
        assertEquals("dGVzdA==", account.data["config"])
    }

    @Test
    fun testPagingToken() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", account.pagingToken)
    }

    @Test
    fun testLinks() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertTrue(account.links.self.href.contains("accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"))
        assertTrue(account.links.transactions.templated == true)
    }

    @Test
    fun testGetIncrementedSequenceNumber() {
        val account = json.decodeFromString<AccountResponse>(accountJson)
        assertEquals(3298702387052546L, account.getIncrementedSequenceNumber())
    }

    @Test
    fun testMinimalAccountDeserialization() {
        val minimalJson = """
        {
            "id": "GTEST",
            "account_id": "GTEST",
            "sequence": 100,
            "subentry_count": 0,
            "last_modified_ledger": 1,
            "last_modified_time": "2021-01-01T00:00:00Z",
            "thresholds": {"low_threshold": 1, "med_threshold": 2, "high_threshold": 3},
            "flags": {"auth_required": true, "auth_revocable": true, "auth_immutable": false, "auth_clawback_enabled": true},
            "balances": [],
            "signers": [],
            "paging_token": "GTEST",
            "_links": {
                "self": {"href": "https://example.com/self"},
                "transactions": {"href": "https://example.com/tx"},
                "operations": {"href": "https://example.com/ops"},
                "payments": {"href": "https://example.com/pay"},
                "effects": {"href": "https://example.com/fx"},
                "offers": {"href": "https://example.com/offers"},
                "trades": {"href": "https://example.com/trades"},
                "data": {"href": "https://example.com/data"}
            }
        }
        """.trimIndent()
        val account = json.decodeFromString<AccountResponse>(minimalJson)
        assertEquals("GTEST", account.id)
        assertEquals(100L, account.sequenceNumber)
        assertNull(account.sequenceLedger)
        assertNull(account.sequenceTime)
        assertNull(account.inflationDestination)
        assertNull(account.homeDomain)
        assertTrue(account.balances.isEmpty())
        assertTrue(account.signers.isEmpty())
        assertTrue(account.data.isEmpty())
        assertTrue(account.flags.authRequired)
        assertTrue(account.flags.authRevocable)
        assertTrue(account.flags.authClawbackEnabled)
        assertEquals(1, account.thresholds.lowThreshold)
        assertEquals(2, account.thresholds.medThreshold)
        assertEquals(3, account.thresholds.highThreshold)
    }
}
