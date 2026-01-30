@file:Suppress("DEPRECATION")
package com.soneso.stellar.sdk.unitTests.horizon.responses.operations

import com.soneso.stellar.sdk.horizon.responses.operations.*
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Comprehensive test for the OperationResponseSerializer polymorphic dispatch.
 * Tests that each known operation type string is correctly deserialized to the right subclass.
 */
class OperationResponseSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Helper to make a minimal JSON for a given type with type-specific required fields
    private fun makeJson(type: String, extraFields: String = ""): String {
        val extra = if (extraFields.isNotEmpty()) ",$extraFields" else ""
        return """
        {
            ${OperationTestHelpers.baseFieldsJson(type)}$extra
        }
        """
    }

    @Test
    fun testCreateAccount() {
        val jsonStr = makeJson("create_account", """
            "account": "${OperationTestHelpers.TEST_ACCOUNT}",
            "funder": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "starting_balance": "100.0000000"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<CreateAccountOperationResponse>(response)
        assertEquals("create_account", response.type)
    }

    @Test
    fun testAccountMerge() {
        val jsonStr = makeJson("account_merge", """
            "account": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "into": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<AccountMergeOperationResponse>(response)
    }

    @Test
    fun testPayment() {
        val jsonStr = makeJson("payment", """
            "amount": "10.0",
            "asset_type": "native",
            "from": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "to": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<PaymentOperationResponse>(response)
    }

    @Test
    fun testPathPaymentStrictReceive() {
        val jsonStr = makeJson("path_payment_strict_receive", """
            "amount": "10.0",
            "source_amount": "5.0",
            "from": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "to": "${OperationTestHelpers.TEST_ACCOUNT}",
            "asset_type": "native",
            "source_asset_type": "native",
            "source_max": "11.0",
            "path": []
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<PathPaymentStrictReceiveOperationResponse>(response)
    }

    @Test
    fun testPathPaymentStrictSend() {
        val jsonStr = makeJson("path_payment_strict_send", """
            "amount": "10.0",
            "source_amount": "5.0",
            "from": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "to": "${OperationTestHelpers.TEST_ACCOUNT}",
            "asset_type": "native",
            "source_asset_type": "native",
            "destination_min": "9.0",
            "path": []
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<PathPaymentStrictSendOperationResponse>(response)
    }

    @Test
    fun testChangeTrust() {
        val jsonStr = makeJson("change_trust", """
            "trustor": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "asset_type": "credit_alphanum4",
            "asset_code": "USD",
            "asset_issuer": "${OperationTestHelpers.TEST_ACCOUNT_2}",
            "limit": "1000.0"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ChangeTrustOperationResponse>(response)
    }

    @Test
    fun testAllowTrust() {
        val jsonStr = makeJson("allow_trust", """
            "trustor": "${OperationTestHelpers.TEST_ACCOUNT}",
            "trustee": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "asset_type": "credit_alphanum4",
            "asset_code": "USD",
            "asset_issuer": "${OperationTestHelpers.TEST_ACCOUNT_2}",
            "authorize": true
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<AllowTrustOperationResponse>(response)
    }

    @Test
    fun testSetTrustLineFlags() {
        val jsonStr = makeJson("set_trust_line_flags", """
            "asset_type": "credit_alphanum4",
            "asset_code": "USD",
            "asset_issuer": "${OperationTestHelpers.TEST_ACCOUNT_2}",
            "trustor": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<SetTrustLineFlagsOperationResponse>(response)
    }

    @Test
    fun testManageSellOffer() {
        val jsonStr = makeJson("manage_sell_offer", """
            "offer_id": 1,
            "amount": "10.0",
            "price": "1.5",
            "price_r": {"n": 3, "d": 2},
            "buying_asset_type": "native",
            "selling_asset_type": "native"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ManageSellOfferOperationResponse>(response)
    }

    @Test
    fun testManageBuyOffer() {
        val jsonStr = makeJson("manage_buy_offer", """
            "offer_id": 1,
            "amount": "10.0",
            "price": "1.5",
            "price_r": {"n": 3, "d": 2},
            "buying_asset_type": "native",
            "selling_asset_type": "native"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ManageBuyOfferOperationResponse>(response)
    }

    @Test
    fun testCreatePassiveSellOffer() {
        val jsonStr = makeJson("create_passive_sell_offer", """
            "offer_id": 1,
            "amount": "10.0",
            "price": "1.5",
            "price_r": {"n": 3, "d": 2},
            "buying_asset_type": "native",
            "selling_asset_type": "native"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<CreatePassiveSellOfferOperationResponse>(response)
    }

    @Test
    fun testSetOptions() {
        val jsonStr = makeJson("set_options", """
            "home_domain": "stellar.org"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<SetOptionsOperationResponse>(response)
    }

    @Test
    fun testManageData() {
        val jsonStr = makeJson("manage_data", """
            "name": "key",
            "value": "val"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ManageDataOperationResponse>(response)
    }

    @Test
    fun testBumpSequence() {
        val jsonStr = makeJson("bump_sequence", """
            "bump_to": 123456
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<BumpSequenceOperationResponse>(response)
    }

    @Test
    fun testCreateClaimableBalance() {
        val jsonStr = makeJson("create_claimable_balance", """
            "asset": "native",
            "amount": "100.0",
            "claimants": [{"destination": "${OperationTestHelpers.TEST_ACCOUNT}", "predicate": {"unconditional": true}}]
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<CreateClaimableBalanceOperationResponse>(response)
    }

    @Test
    fun testClaimClaimableBalance() {
        val jsonStr = makeJson("claim_claimable_balance", """
            "balance_id": "00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072",
            "claimant": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ClaimClaimableBalanceOperationResponse>(response)
    }

    @Test
    fun testClawbackClaimableBalance() {
        val jsonStr = makeJson("clawback_claimable_balance", """
            "balance_id": "00000000929b20b72e5890ab51c24f1cc46fa01c4f318d8d33367d24dd614cfdf5491072"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ClawbackClaimableBalanceOperationResponse>(response)
    }

    @Test
    fun testClawback() {
        val jsonStr = makeJson("clawback", """
            "asset_type": "credit_alphanum4",
            "asset_code": "USD",
            "asset_issuer": "${OperationTestHelpers.TEST_ACCOUNT_2}",
            "amount": "100.0",
            "from": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ClawbackOperationResponse>(response)
    }

    @Test
    fun testBeginSponsoringFutureReserves() {
        val jsonStr = makeJson("begin_sponsoring_future_reserves", """
            "sponsored_id": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<BeginSponsoringFutureReservesOperationResponse>(response)
    }

    @Test
    fun testEndSponsoringFutureReserves() {
        val jsonStr = makeJson("end_sponsoring_future_reserves", """
            "begin_sponsor": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<EndSponsoringFutureReservesOperationResponse>(response)
    }

    @Test
    fun testRevokeSponsorship() {
        val jsonStr = makeJson("revoke_sponsorship", """
            "account_id": "${OperationTestHelpers.TEST_ACCOUNT}"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<RevokeSponsorshipOperationResponse>(response)
    }

    @Test
    fun testLiquidityPoolDeposit() {
        val jsonStr = makeJson("liquidity_pool_deposit", """
            "liquidity_pool_id": "dd7b1ab831c273310ddbec6f97870aa83c2fbd78ce22aded37ecbf4f3380fac7",
            "reserves_max": [{"asset": "native", "amount": "100.0"}],
            "min_price": "0.5",
            "min_price_r": {"n": 1, "d": 2},
            "max_price": "2.0",
            "max_price_r": {"n": 2, "d": 1},
            "reserves_deposited": [{"asset": "native", "amount": "80.0"}],
            "shares_received": "100.0"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<LiquidityPoolDepositOperationResponse>(response)
    }

    @Test
    fun testLiquidityPoolWithdraw() {
        val jsonStr = makeJson("liquidity_pool_withdraw", """
            "liquidity_pool_id": "dd7b1ab831c273310ddbec6f97870aa83c2fbd78ce22aded37ecbf4f3380fac7",
            "reserves_min": [{"asset": "native", "amount": "100.0"}],
            "reserves_received": [{"asset": "native", "amount": "120.0"}],
            "shares": "200.0"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<LiquidityPoolWithdrawOperationResponse>(response)
    }

    @Test
    fun testInvokeHostFunction() {
        val jsonStr = makeJson("invoke_host_function", """
            "function": "HostFunctionTypeInvokeContract"
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<InvokeHostFunctionOperationResponse>(response)
    }

    @Test
    fun testExtendFootprintTTL() {
        val jsonStr = makeJson("extend_footprint_ttl", """
            "extend_to": 500000
        """.trimIndent())
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<ExtendFootprintTTLOperationResponse>(response)
    }

    @Test
    fun testRestoreFootprint() {
        val jsonStr = makeJson("restore_footprint")
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<RestoreFootprintOperationResponse>(response)
    }

    @Test
    fun testInflation() {
        val jsonStr = makeJson("inflation")
        val response = json.decodeFromString<OperationResponse>(jsonStr)
        assertIs<InflationOperationResponse>(response)
    }

    @Test
    fun testUnknownTypeThrows() {
        val jsonStr = makeJson("unknown_type_xyz")
        assertFailsWith<IllegalArgumentException> {
            json.decodeFromString<OperationResponse>(jsonStr)
        }
    }

    @Test
    fun testMissingTypeThrows() {
        val jsonStr = """
        {
            "id": "12345",
            "source_account": "${OperationTestHelpers.TEST_SOURCE_ACCOUNT}",
            "paging_token": "12345",
            "created_at": "2023-01-15T12:00:00Z",
            "transaction_hash": "abc123",
            "transaction_successful": true,
            ${OperationTestHelpers.LINKS_JSON}
        }
        """
        assertFailsWith<IllegalArgumentException> {
            json.decodeFromString<OperationResponse>(jsonStr)
        }
    }

    @Test
    fun testBaseFieldsPreservedAcrossAllTypes() {
        // Verify that base fields are always accessible from the polymorphic type
        val jsonStr = makeJson("inflation")
        val response = json.decodeFromString<OperationResponse>(jsonStr)

        assertEquals(OperationTestHelpers.TEST_ID, response.id)
        assertEquals(OperationTestHelpers.TEST_SOURCE_ACCOUNT, response.sourceAccount)
        assertEquals(OperationTestHelpers.TEST_PAGING_TOKEN, response.pagingToken)
        assertEquals(OperationTestHelpers.TEST_CREATED_AT, response.createdAt)
        assertEquals(OperationTestHelpers.TEST_TX_HASH, response.transactionHash)
        assertTrue(response.transactionSuccessful)
        assertEquals("inflation", response.type)
        assertNotNull(response.links)
        assertNull(response.transaction)
    }

    @Test
    fun testLinksDeserialization() {
        val jsonStr = makeJson("inflation")
        val response = json.decodeFromString<OperationResponse>(jsonStr)

        assertEquals("https://horizon.stellar.org/operations/12345/effects", response.links.effects.href)
        assertEquals("https://horizon.stellar.org/operations/12345", response.links.self.href)
        assertEquals("https://horizon.stellar.org/transactions/abc123", response.links.transaction.href)
    }
}
