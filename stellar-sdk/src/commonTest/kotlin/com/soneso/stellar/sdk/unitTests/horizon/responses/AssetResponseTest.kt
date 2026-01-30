package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.AssetResponse
import com.soneso.stellar.sdk.horizon.responses.Link
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AssetResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val assetJson = """
    {
        "asset_type": "credit_alphanum4",
        "asset_code": "USD",
        "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
        "paging_token": "USD_GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B_credit_alphanum4",
        "contract_id": "a1b2c3d4e5f6",
        "num_claimable_balances": 5,
        "num_liquidity_pools": 3,
        "num_contracts": 2,
        "accounts": {
            "authorized": 1000,
            "authorized_to_maintain_liabilities": 50,
            "unauthorized": 10
        },
        "claimable_balances_amount": "500.0000000",
        "liquidity_pools_amount": "1000.0000000",
        "contracts_amount": "250.0000000",
        "balances": {
            "authorized": "5000000.0000000",
            "authorized_to_maintain_liabilities": "100000.0000000",
            "unauthorized": "5000.0000000"
        },
        "flags": {
            "auth_required": true,
            "auth_revocable": true,
            "auth_immutable": false,
            "auth_clawback_enabled": false
        },
        "_links": {
            "toml": {"href": "https://example.com/.well-known/stellar.toml"}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val asset = json.decodeFromString<AssetResponse>(assetJson)
        assertEquals("credit_alphanum4", asset.assetType)
        assertEquals("USD", asset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", asset.assetIssuer)
        assertEquals("USD_GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B_credit_alphanum4", asset.pagingToken)
        assertEquals("a1b2c3d4e5f6", asset.contractId)
        assertEquals(5, asset.numClaimableBalances)
        assertEquals(3, asset.numLiquidityPools)
        assertEquals(2, asset.numContracts)
        assertEquals("500.0000000", asset.claimableBalancesAmount)
        assertEquals("1000.0000000", asset.liquidityPoolsAmount)
        assertEquals("250.0000000", asset.contractsAmount)
    }

    @Test
    fun testAccounts() {
        val asset = json.decodeFromString<AssetResponse>(assetJson)
        assertEquals(1000, asset.accounts.authorized)
        assertEquals(50, asset.accounts.authorizedToMaintainLiabilities)
        assertEquals(10, asset.accounts.unauthorized)
    }

    @Test
    fun testBalances() {
        val asset = json.decodeFromString<AssetResponse>(assetJson)
        assertEquals("5000000.0000000", asset.balances.authorized)
        assertEquals("100000.0000000", asset.balances.authorizedToMaintainLiabilities)
        assertEquals("5000.0000000", asset.balances.unauthorized)
    }

    @Test
    fun testFlags() {
        val asset = json.decodeFromString<AssetResponse>(assetJson)
        assertTrue(asset.flags.authRequired)
        assertTrue(asset.flags.authRevocable)
        assertFalse(asset.flags.authImmutable)
        assertFalse(asset.flags.authClawbackEnabled)
    }

    @Test
    fun testLinks() {
        val asset = json.decodeFromString<AssetResponse>(assetJson)
        assertEquals("https://example.com/.well-known/stellar.toml", asset.links.toml.href)
    }

    @Test
    fun testMinimalDeserialization() {
        val minimalJson = """
        {
            "asset_type": "credit_alphanum12",
            "asset_code": "LONGASSET",
            "asset_issuer": "GTEST",
            "paging_token": "token123",
            "accounts": {"authorized": 0, "authorized_to_maintain_liabilities": 0, "unauthorized": 0},
            "balances": {"authorized": "0", "authorized_to_maintain_liabilities": "0", "unauthorized": "0"},
            "flags": {"auth_required": false, "auth_revocable": false, "auth_immutable": false, "auth_clawback_enabled": false},
            "_links": {"toml": {"href": "https://example.com/toml"}}
        }
        """.trimIndent()
        val asset = json.decodeFromString<AssetResponse>(minimalJson)
        assertEquals("credit_alphanum12", asset.assetType)
        assertEquals("LONGASSET", asset.assetCode)
        assertNull(asset.contractId)
        assertNull(asset.numClaimableBalances)
        assertNull(asset.numLiquidityPools)
        assertNull(asset.numContracts)
        assertNull(asset.claimableBalancesAmount)
    }
}
