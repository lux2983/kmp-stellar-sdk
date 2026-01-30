package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.LiquidityPoolResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LiquidityPoolResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testComprehensiveLiquidityPoolResponseDeserialization() {
        val liquidityPoolJson = """
        {
            "id": "67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9",
            "paging_token": "67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9",
            "fee_bp": 30,
            "type": "constant_product",
            "total_trustlines": 42,
            "total_shares": "1000.0000000",
            "reserves": [
                {
                    "amount": "500.0000000",
                    "asset": "native"
                },
                {
                    "amount": "2500.0000000",
                    "asset": "USD:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
                }
            ],
            "last_modified_ledger": 32069474,
            "last_modified_time": "2021-01-01T12:00:00Z",
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/liquidity_pools/67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9"
                },
                "operations": {
                    "href": "https://horizon.stellar.org/liquidity_pools/67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9/operations",
                    "templated": true
                },
                "transactions": {
                    "href": "https://horizon.stellar.org/liquidity_pools/67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9/transactions",
                    "templated": false
                }
            }
        }
        """.trimIndent()

        val liquidityPool = json.decodeFromString<LiquidityPoolResponse>(liquidityPoolJson)

        // Test ALL main properties
        assertEquals("67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9", liquidityPool.id)
        assertEquals("67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9", liquidityPool.pagingToken)
        assertEquals(30, liquidityPool.feeBp)
        assertEquals("constant_product", liquidityPool.type)
        assertEquals(42L, liquidityPool.totalTrustlines)
        assertEquals("1000.0000000", liquidityPool.totalShares)
        assertEquals(32069474L, liquidityPool.lastModifiedLedger)
        assertEquals("2021-01-01T12:00:00Z", liquidityPool.lastModifiedTime)

        // Test reserves list and Reserve inner class
        assertEquals(2, liquidityPool.reserves.size)
        
        val firstReserve = liquidityPool.reserves[0]
        assertEquals("500.0000000", firstReserve.amount)
        assertEquals("native", firstReserve.asset)
        
        val secondReserve = liquidityPool.reserves[1]
        assertEquals("2500.0000000", secondReserve.amount)
        assertEquals("USD:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", secondReserve.asset)

        // Test Links inner class
        assertEquals("https://horizon.stellar.org/liquidity_pools/67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9", liquidityPool.links.self.href)
        assertEquals("https://horizon.stellar.org/liquidity_pools/67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9/operations", liquidityPool.links.operations?.href)
        assertEquals("https://horizon.stellar.org/liquidity_pools/67260c4c1807b262ff851b0a3fe141194936bb0215b2f77447f1df11998eabb9/transactions", liquidityPool.links.transactions?.href)
        assertNull(liquidityPool.links.self.templated)
        assertEquals(true, liquidityPool.links.operations?.templated)
        assertEquals(false, liquidityPool.links.transactions?.templated)
    }

    @Test
    fun testLiquidityPoolResponseWithNullableFields() {
        val liquidityPoolJson = """
        {
            "id": "minimal-pool-id",
            "paging_token": "minimal-pool-paging",
            "fee_bp": null,
            "type": "constant_product",
            "total_trustlines": null,
            "total_shares": "0.0000000",
            "reserves": [],
            "last_modified_ledger": null,
            "last_modified_time": null,
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/liquidity_pools/minimal"
                },
                "operations": null,
                "transactions": null
            }
        }
        """.trimIndent()

        val liquidityPool = json.decodeFromString<LiquidityPoolResponse>(liquidityPoolJson)

        // Test all properties with null/empty values
        assertEquals("minimal-pool-id", liquidityPool.id)
        assertEquals("minimal-pool-paging", liquidityPool.pagingToken)
        assertNull(liquidityPool.feeBp)
        assertEquals("constant_product", liquidityPool.type)
        assertNull(liquidityPool.totalTrustlines)
        assertEquals("0.0000000", liquidityPool.totalShares)
        assertTrue(liquidityPool.reserves.isEmpty())
        assertNull(liquidityPool.lastModifiedLedger)
        assertNull(liquidityPool.lastModifiedTime)

        // Test Links with null optional fields
        assertEquals("https://horizon.stellar.org/liquidity_pools/minimal", liquidityPool.links.self.href)
        assertNull(liquidityPool.links.operations)
        assertNull(liquidityPool.links.transactions)
    }

    @Test
    fun testLiquidityPoolResponseWithMultipleReserves() {
        val liquidityPoolJson = """
        {
            "id": "multi-reserve-pool",
            "paging_token": "multi-reserve-paging",
            "fee_bp": 50,
            "type": "constant_product",
            "total_trustlines": 100,
            "total_shares": "10000.0000000",
            "reserves": [
                {
                    "amount": "1000.0000000",
                    "asset": "native"
                },
                {
                    "amount": "5000.0000000",
                    "asset": "EUR:GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU"
                },
                {
                    "amount": "7500.0000000",
                    "asset": "LONGASSETCODE:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
                }
            ],
            "last_modified_ledger": 45000000,
            "last_modified_time": "2022-01-01T15:30:00Z",
            "_links": {
                "self": {
                    "href": "https://horizon.stellar.org/liquidity_pools/multi-reserve",
                    "templated": false
                },
                "operations": {
                    "href": "https://horizon.stellar.org/liquidity_pools/multi-reserve/operations"
                },
                "transactions": {
                    "href": "https://horizon.stellar.org/liquidity_pools/multi-reserve/transactions",
                    "templated": true
                }
            }
        }
        """.trimIndent()

        val liquidityPool = json.decodeFromString<LiquidityPoolResponse>(liquidityPoolJson)

        // Test properties with different values
        assertEquals("multi-reserve-pool", liquidityPool.id)
        assertEquals("multi-reserve-paging", liquidityPool.pagingToken)
        assertEquals(50, liquidityPool.feeBp)
        assertEquals("constant_product", liquidityPool.type)
        assertEquals(100L, liquidityPool.totalTrustlines)
        assertEquals("10000.0000000", liquidityPool.totalShares)
        assertEquals(45000000L, liquidityPool.lastModifiedLedger)
        assertEquals("2022-01-01T15:30:00Z", liquidityPool.lastModifiedTime)

        // Test multiple reserves
        assertEquals(3, liquidityPool.reserves.size)
        
        assertEquals("1000.0000000", liquidityPool.reserves[0].amount)
        assertEquals("native", liquidityPool.reserves[0].asset)
        
        assertEquals("5000.0000000", liquidityPool.reserves[1].amount)
        assertEquals("EUR:GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", liquidityPool.reserves[1].asset)
        
        assertEquals("7500.0000000", liquidityPool.reserves[2].amount)
        assertEquals("LONGASSETCODE:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", liquidityPool.reserves[2].asset)

        // Test Links with mixed templated values
        assertEquals("https://horizon.stellar.org/liquidity_pools/multi-reserve", liquidityPool.links.self.href)
        assertEquals(false, liquidityPool.links.self.templated)
        assertEquals("https://horizon.stellar.org/liquidity_pools/multi-reserve/operations", liquidityPool.links.operations?.href)
        assertNull(liquidityPool.links.operations?.templated)
        assertEquals("https://horizon.stellar.org/liquidity_pools/multi-reserve/transactions", liquidityPool.links.transactions?.href)
        assertEquals(true, liquidityPool.links.transactions?.templated)
    }

    @Test
    fun testLiquidityPoolResponseInnerClassesDirectConstruction() {
        // Test direct construction of Reserve inner class
        val reserve1 = LiquidityPoolResponse.Reserve(
            amount = "1000.0000000",
            asset = "native"
        )
        assertEquals("1000.0000000", reserve1.amount)
        assertEquals("native", reserve1.asset)
        
        val reserve2 = LiquidityPoolResponse.Reserve(
            amount = "500.0000000",
            asset = "USD:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
        )
        assertEquals("500.0000000", reserve2.amount)
        assertEquals("USD:GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", reserve2.asset)

        // Test direct construction of Links inner class
        val links = LiquidityPoolResponse.Links(
            self = com.soneso.stellar.sdk.horizon.responses.Link("https://test.self", true),
            operations = com.soneso.stellar.sdk.horizon.responses.Link("https://test.operations", false),
            transactions = com.soneso.stellar.sdk.horizon.responses.Link("https://test.transactions", null)
        )
        assertEquals("https://test.self", links.self.href)
        assertEquals(true, links.self.templated)
        assertEquals("https://test.operations", links.operations?.href)
        assertEquals(false, links.operations?.templated)
        assertEquals("https://test.transactions", links.transactions?.href)
        assertNull(links.transactions?.templated)
        
        // Test Links with null optional fields
        val linksWithNulls = LiquidityPoolResponse.Links(
            self = com.soneso.stellar.sdk.horizon.responses.Link("https://test.only.self", false),
            operations = null,
            transactions = null
        )
        assertEquals("https://test.only.self", linksWithNulls.self.href)
        assertEquals(false, linksWithNulls.self.templated)
        assertNull(linksWithNulls.operations)
        assertNull(linksWithNulls.transactions)
    }
}