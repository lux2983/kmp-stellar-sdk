package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.TradeResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TradeResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val tradeJson = """
    {
        "id": "107449584845914113-0",
        "paging_token": "107449584845914113-0",
        "ledger_close_time": "2021-01-01T00:00:00Z",
        "trade_type": "orderbook",
        "offer_id": 12345,
        "base_offer_id": 67890,
        "base_account": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
        "base_amount": "100.0000000",
        "base_asset_type": "native",
        "counter_offer_id": 11111,
        "counter_account": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
        "counter_amount": "50.0000000",
        "counter_asset_type": "credit_alphanum4",
        "counter_asset_code": "USD",
        "counter_asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
        "base_is_seller": true,
        "price": {"n": 1, "d": 2},
        "_links": {
            "base": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"},
            "counter": {"href": "https://horizon.stellar.org/accounts/GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU"},
            "operation": {"href": "https://horizon.stellar.org/operations/107449584845914113"}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val trade = json.decodeFromString<TradeResponse>(tradeJson)
        assertEquals("107449584845914113-0", trade.id)
        assertEquals("107449584845914113-0", trade.pagingToken)
        assertEquals("2021-01-01T00:00:00Z", trade.ledgerCloseTime)
        assertEquals("orderbook", trade.tradeType)
        assertEquals(12345L, trade.offerId)
        assertEquals(67890L, trade.baseOfferId)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", trade.baseAccount)
        assertEquals("100.0000000", trade.baseAmount)
        assertEquals("native", trade.baseAssetType)
        assertNull(trade.baseAssetCode)
        assertNull(trade.baseAssetIssuer)
    }

    @Test
    fun testCounterSide() {
        val trade = json.decodeFromString<TradeResponse>(tradeJson)
        assertEquals(11111L, trade.counterOfferId)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", trade.counterAccount)
        assertEquals("50.0000000", trade.counterAmount)
        assertEquals("credit_alphanum4", trade.counterAssetType)
        assertEquals("USD", trade.counterAssetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", trade.counterAssetIssuer)
    }

    @Test
    fun testPriceAndSeller() {
        val trade = json.decodeFromString<TradeResponse>(tradeJson)
        assertEquals(true, trade.baseIsSeller)
        assertEquals(1L, trade.price?.numerator)
        assertEquals(2L, trade.price?.denominator)
    }

    @Test
    fun testLinks() {
        val trade = json.decodeFromString<TradeResponse>(tradeJson)
        assertTrue(trade.links.base.href.contains("accounts/"))
        assertTrue(trade.links.counter.href.contains("accounts/"))
        assertTrue(trade.links.operation.href.contains("operations/"))
    }

    @Test
    fun testLiquidityPoolTradeDeserialization() {
        val lpTradeJson = """
        {
            "id": "trade123",
            "paging_token": "trade123",
            "ledger_close_time": "2021-06-15T00:00:00Z",
            "trade_type": "liquidity_pool",
            "liquidity_pool_fee_bp": 30,
            "base_liquidity_pool_id": "pool123",
            "base_amount": "100.0",
            "base_asset_type": "native",
            "counter_account": "GTEST",
            "counter_amount": "200.0",
            "counter_asset_type": "credit_alphanum4",
            "counter_asset_code": "USD",
            "counter_asset_issuer": "GISSUER",
            "_links": {
                "base": {"href": "https://example.com/base"},
                "counter": {"href": "https://example.com/counter"},
                "operation": {"href": "https://example.com/op"}
            }
        }
        """.trimIndent()
        val trade = json.decodeFromString<TradeResponse>(lpTradeJson)
        assertEquals("liquidity_pool", trade.tradeType)
        assertEquals(30, trade.liquidityPoolFeeBP)
        assertEquals("pool123", trade.baseLiquidityPoolId)
        assertNull(trade.baseAccount)
        assertNull(trade.baseOfferId)
        assertNull(trade.offerId)
        assertNull(trade.price)
        assertNull(trade.baseIsSeller)
    }
}
