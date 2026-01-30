package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.RootResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RootResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val rootJson = """
    {
        "horizon_version": "2.26.0",
        "core_version": "stellar-core 19.10.0 (236556c6768e6a4e1e7dd2a78d5f0e3b87a09f9e)",
        "ingest_latest_ledger": 7505182,
        "history_latest_ledger": 7505182,
        "history_latest_ledger_closed_at": "2021-01-01T00:00:00Z",
        "history_elder_ledger": 1,
        "core_latest_ledger": 7505183,
        "network_passphrase": "Test SDF Network ; September 2015",
        "current_protocol_version": 18,
        "supported_protocol_version": 18,
        "core_supported_protocol_version": 18,
        "_links": {
            "account": {"href": "https://horizon.stellar.org/accounts/{account_id}", "templated": true},
            "accounts": {"href": "https://horizon.stellar.org/accounts{?signer,sponsor,asset,liquidity_pool,cursor,limit,order}", "templated": true},
            "account_transactions": {"href": "https://horizon.stellar.org/accounts/{account_id}/transactions{?cursor,limit,order}", "templated": true},
            "assets": {"href": "https://horizon.stellar.org/assets{?asset_code,asset_issuer,cursor,limit,order}", "templated": true},
            "effects": {"href": "https://horizon.stellar.org/effects{?cursor,limit,order}", "templated": true},
            "fee_stats": {"href": "https://horizon.stellar.org/fee_stats"},
            "friendbot": {"href": "https://friendbot.stellar.org/?addr={account}", "templated": true},
            "ledger": {"href": "https://horizon.stellar.org/ledgers/{sequence}", "templated": true},
            "ledgers": {"href": "https://horizon.stellar.org/ledgers{?cursor,limit,order}", "templated": true},
            "self": {"href": "https://horizon.stellar.org/"},
            "transactions": {"href": "https://horizon.stellar.org/transactions{?cursor,limit,order}", "templated": true}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val root = json.decodeFromString<RootResponse>(rootJson)
        assertEquals("2.26.0", root.horizonVersion)
        assertTrue(root.stellarCoreVersion.contains("19.10.0"))
        assertEquals(7505182L, root.ingestLatestLedger)
        assertEquals(7505182L, root.historyLatestLedger)
        assertEquals("2021-01-01T00:00:00Z", root.historyLatestLedgerClosedAt)
        assertEquals(1L, root.historyElderLedger)
        assertEquals(7505183L, root.coreLatestLedger)
        assertEquals("Test SDF Network ; September 2015", root.networkPassphrase)
        assertEquals(18, root.currentProtocolVersion)
        assertEquals(18, root.supportedProtocolVersion)
        assertEquals(18, root.coreSupportedProtocolVersion)
    }

    @Test
    fun testLinks() {
        val root = json.decodeFromString<RootResponse>(rootJson)
        assertTrue(root.links.account?.templated == true)
        assertTrue(root.links.accounts?.href?.contains("accounts") == true)
        assertTrue(root.links.assets?.href?.contains("assets") == true)
        assertTrue(root.links.effects?.href?.contains("effects") == true)
        assertEquals("https://horizon.stellar.org/fee_stats", root.links.feeStats?.href)
        assertTrue(root.links.friendbot?.href?.contains("friendbot") == true)
        assertTrue(root.links.ledger?.href?.contains("ledgers") == true)
        assertTrue(root.links.ledgers?.href?.contains("ledgers") == true)
        assertEquals("https://horizon.stellar.org/", root.links.self?.href)
    }

    @Test
    fun testMinimalLinks() {
        val minimalJson = """
        {
            "horizon_version": "1.0",
            "core_version": "core",
            "history_latest_ledger": 1,
            "history_latest_ledger_closed_at": "2021-01-01T00:00:00Z",
            "history_elder_ledger": 1,
            "core_latest_ledger": 1,
            "network_passphrase": "test",
            "current_protocol_version": 1,
            "supported_protocol_version": 1,
            "core_supported_protocol_version": 1,
            "_links": {}
        }
        """.trimIndent()
        val root = json.decodeFromString<RootResponse>(minimalJson)
        assertNull(root.ingestLatestLedger)
        assertNull(root.links.account)
        assertNull(root.links.accounts)
        assertNull(root.links.claimableBalances)
        assertNull(root.links.liquidityPools)
        assertNull(root.links.offers)
        assertNull(root.links.operations)
        assertNull(root.links.orderBook)
        assertNull(root.links.payments)
        assertNull(root.links.strictReceivePaths)
        assertNull(root.links.strictSendPaths)
        assertNull(root.links.tradeAggregations)
        assertNull(root.links.trades)
        assertNull(root.links.transaction)
        assertNull(root.links.transactions)
    }
}
