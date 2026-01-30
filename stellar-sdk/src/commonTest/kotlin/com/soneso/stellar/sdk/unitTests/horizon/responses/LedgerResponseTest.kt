package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.LedgerResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LedgerResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val ledgerJson = """
    {
        "id": "abc123",
        "paging_token": "7505182",
        "hash": "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
        "prev_hash": "f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5",
        "sequence": 7505182,
        "successful_transaction_count": 10,
        "failed_transaction_count": 2,
        "operation_count": 25,
        "tx_set_operation_count": 30,
        "closed_at": "2021-01-01T00:00:00Z",
        "total_coins": "105443902087.3472865",
        "fee_pool": "1807982.8413787",
        "base_fee_in_stroops": "100",
        "base_reserve_in_stroops": "5000000",
        "max_tx_set_size": 100,
        "protocol_version": 18,
        "header_xdr": "AAAA",
        "_links": {
            "self": {"href": "https://horizon.stellar.org/ledgers/7505182"},
            "transactions": {"href": "https://horizon.stellar.org/ledgers/7505182/transactions{?cursor,limit,order}", "templated": true},
            "operations": {"href": "https://horizon.stellar.org/ledgers/7505182/operations{?cursor,limit,order}", "templated": true},
            "payments": {"href": "https://horizon.stellar.org/ledgers/7505182/payments{?cursor,limit,order}", "templated": true},
            "effects": {"href": "https://horizon.stellar.org/ledgers/7505182/effects{?cursor,limit,order}", "templated": true}
        }
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val ledger = json.decodeFromString<LedgerResponse>(ledgerJson)
        assertEquals("abc123", ledger.id)
        assertEquals("7505182", ledger.pagingToken)
        assertEquals("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2", ledger.hash)
        assertEquals("f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5", ledger.prevHash)
        assertEquals(7505182L, ledger.sequence)
        assertEquals(10, ledger.successfulTransactionCount)
        assertEquals(2, ledger.failedTransactionCount)
        assertEquals(25, ledger.operationCount)
        assertEquals(30, ledger.txSetOperationCount)
        assertEquals("2021-01-01T00:00:00Z", ledger.closedAt)
        assertEquals("105443902087.3472865", ledger.totalCoins)
        assertEquals("1807982.8413787", ledger.feePool)
        assertEquals("100", ledger.baseFeeInStroops)
        assertEquals("5000000", ledger.baseReserveInStroops)
        assertEquals(100, ledger.maxTxSetSize)
        assertEquals(18, ledger.protocolVersion)
        assertEquals("AAAA", ledger.headerXdr)
    }

    @Test
    fun testLinks() {
        val ledger = json.decodeFromString<LedgerResponse>(ledgerJson)
        assertTrue(ledger.links.self.href.contains("ledgers/7505182"))
        assertTrue(ledger.links.transactions.templated == true)
        assertTrue(ledger.links.operations.templated == true)
        assertTrue(ledger.links.payments.templated == true)
        assertTrue(ledger.links.effects.templated == true)
    }

    @Test
    fun testMinimalDeserialization() {
        val minimalJson = """
        {
            "id": "min123",
            "paging_token": "1",
            "hash": "aaa",
            "sequence": 1,
            "closed_at": "2021-01-01T00:00:00Z",
            "total_coins": "100.0",
            "fee_pool": "0.0",
            "base_fee_in_stroops": "100",
            "base_reserve_in_stroops": "5000000",
            "_links": {
                "self": {"href": "https://example.com/self"},
                "transactions": {"href": "https://example.com/tx"},
                "operations": {"href": "https://example.com/ops"},
                "payments": {"href": "https://example.com/pay"},
                "effects": {"href": "https://example.com/fx"}
            }
        }
        """.trimIndent()
        val ledger = json.decodeFromString<LedgerResponse>(minimalJson)
        assertEquals("min123", ledger.id)
        assertNull(ledger.prevHash)
        assertNull(ledger.successfulTransactionCount)
        assertNull(ledger.failedTransactionCount)
        assertNull(ledger.operationCount)
        assertNull(ledger.txSetOperationCount)
        assertNull(ledger.maxTxSetSize)
        assertNull(ledger.protocolVersion)
        assertNull(ledger.headerXdr)
    }
}
