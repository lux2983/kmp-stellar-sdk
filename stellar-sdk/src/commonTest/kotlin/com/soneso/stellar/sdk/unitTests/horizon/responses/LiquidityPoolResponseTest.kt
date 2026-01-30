package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.LiquidityPoolResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LiquidityPoolResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val poolJson = """
    {
        "id": "67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9",
        "paging_token": "67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9",
        "fee_bp": 30,
        "type": "constant_product",
        "total_trustlines": 300,
        "total_shares": "5000.0000000",
        "reserves": [
            {
                "amount": "1000.0000000",
                "asset": "native"
            },
            {
                "amount": "2000.0000000",
                "asset": "USD:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
            }
        ],
        "last_modified_ledger": 7654321,
        "last_modified_time": "2021-06-15T00:00:00Z",
        "_links": {
            "self": {"href": "https://horizon.stellar.org/liquidity_pools/67260c4c"},
            "operations": {"href": "https://horizon.stellar.org/liquidity_pools/67260c4c/operations"},
            "transactions": {"href": "https://horizon.stellar.org/liquidity_pools/67260c4c/transactions"}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val pool = json.decodeFromString<LiquidityPoolResponse>(poolJson)
        assertEquals("67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9", pool.id)
        assertEquals("67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9", pool.pagingToken)
        assertEquals(30, pool.feeBp)
        assertEquals("constant_product", pool.type)
        assertEquals(300L, pool.totalTrustlines)
        assertEquals("5000.0000000", pool.totalShares)
        assertEquals(7654321L, pool.lastModifiedLedger)
        assertEquals("2021-06-15T00:00:00Z", pool.lastModifiedTime)
    }

    @Test
    fun testReserves() {
        val pool = json.decodeFromString<LiquidityPoolResponse>(poolJson)
        assertEquals(2, pool.reserves.size)
        assertEquals("1000.0000000", pool.reserves[0].amount)
        assertEquals("native", pool.reserves[0].asset)
        assertEquals("2000.0000000", pool.reserves[1].amount)
        assertTrue(pool.reserves[1].asset.contains("USD"))
    }

    @Test
    fun testLinks() {
        val pool = json.decodeFromString<LiquidityPoolResponse>(poolJson)
        assertTrue(pool.links.self.href.contains("liquidity_pools"))
        assertTrue(pool.links.operations?.href?.contains("operations") == true)
        assertTrue(pool.links.transactions?.href?.contains("transactions") == true)
    }

    @Test
    fun testMinimalDeserialization() {
        val minimalJson = """
        {
            "id": "abc123",
            "paging_token": "abc123",
            "type": "constant_product",
            "total_shares": "0.0000000",
            "_links": {
                "self": {"href": "https://example.com/self"}
            }
        }
        """.trimIndent()
        val pool = json.decodeFromString<LiquidityPoolResponse>(minimalJson)
        assertEquals("abc123", pool.id)
        assertNull(pool.feeBp)
        assertNull(pool.totalTrustlines)
        assertTrue(pool.reserves.isEmpty())
        assertNull(pool.lastModifiedLedger)
        assertNull(pool.lastModifiedTime)
        assertNull(pool.links.operations)
        assertNull(pool.links.transactions)
    }

    @Test
    fun testReserveEquality() {
        val r1 = LiquidityPoolResponse.Reserve("100.0", "native")
        val r2 = LiquidityPoolResponse.Reserve("100.0", "native")
        assertEquals(r1, r2)
    }
}
