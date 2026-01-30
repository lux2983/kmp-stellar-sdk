package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.Link
import com.soneso.stellar.sdk.horizon.responses.TransactionResponse
import kotlinx.serialization.json.Json
import kotlin.test.*

class TransactionResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testTransactionResponseLinksInnerClassCoverage() {
        val transactionJson = """
        {
            "id": "abc123",
            "paging_token": "abc123",
            "successful": true,
            "hash": "deadbeef",
            "ledger": 7654321,
            "created_at": "2021-01-01T00:00:00Z",
            "source_account": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "account_muxed": "MAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCAAAAAAAAAAAAGDFE",
            "account_muxed_id": "12345",
            "source_account_sequence": 3298702387052545,
            "fee_account": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "fee_account_muxed": "MFEE4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCAAAAAAAAAAAAGDFE",
            "fee_account_muxed_id": "67890",
            "fee_charged": 100,
            "max_fee": 200,
            "operation_count": 1,
            "envelope_xdr": "AAAA",
            "result_xdr": "BBBB",
            "result_meta_xdr": "CCCC",
            "fee_meta_xdr": "DDDD",
            "signatures": ["sig1", "sig2"],
            "memo_type": "text",
            "memo": "hello world",
            "memo_bytes": "aGVsbG8gd29ybGQ=",
            "_links": {
                "self": {"href": "https://horizon.stellar.org/transactions/abc123"},
                "account": {"href": "https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"},
                "ledger": {"href": "https://horizon.stellar.org/ledgers/7654321"},
                "operations": {"href": "https://horizon.stellar.org/transactions/abc123/operations"},
                "effects": {"href": "https://horizon.stellar.org/transactions/abc123/effects"},
                "precedes": {"href": "https://horizon.stellar.org/transactions?order=asc&cursor=abc123"},
                "succeeds": {"href": "https://horizon.stellar.org/transactions?order=desc&cursor=abc123"}
            }
        }
        """.trimIndent()

        val tx = json.decodeFromString<TransactionResponse>(transactionJson)
        
        // Test ALL properties of Links inner class are accessed
        val links = tx.links
        assertNotNull(links)
        
        // Assert on EVERY property to ensure coverage
        assertEquals("https://horizon.stellar.org/transactions/abc123", links.self.href)
        assertEquals("https://horizon.stellar.org/accounts/GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", links.account.href)
        assertEquals("https://horizon.stellar.org/ledgers/7654321", links.ledger.href)
        assertEquals("https://horizon.stellar.org/transactions/abc123/operations", links.operations.href)
        assertEquals("https://horizon.stellar.org/transactions/abc123/effects", links.effects.href)
        assertEquals("https://horizon.stellar.org/transactions?order=asc&cursor=abc123", links.precedes.href)
        assertEquals("https://horizon.stellar.org/transactions?order=desc&cursor=abc123", links.succeeds.href)

        // Test all main transaction properties are read
        assertEquals("abc123", tx.id)
        assertEquals("abc123", tx.pagingToken)
        assertTrue(tx.successful)
        assertEquals("deadbeef", tx.hash)
        assertEquals(7654321L, tx.ledger)
        assertEquals("2021-01-01T00:00:00Z", tx.createdAt)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", tx.sourceAccount)
        assertEquals("MAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCAAAAAAAAAAAAGDFE", tx.accountMuxed)
        assertEquals("12345", tx.accountMuxedId)
        assertEquals(3298702387052545L, tx.sourceAccountSequence)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", tx.feeAccount)
        assertEquals("MFEE4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCAAAAAAAAAAAAGDFE", tx.feeAccountMuxed)
        assertEquals("67890", tx.feeAccountMuxedId)
        assertEquals(100L, tx.feeCharged)
        assertEquals(200L, tx.maxFee)
        assertEquals(1, tx.operationCount)
        assertEquals("AAAA", tx.envelopeXdr)
        assertEquals("BBBB", tx.resultXdr)
        assertEquals("CCCC", tx.resultMetaXdr)
        assertEquals("DDDD", tx.feeMetaXdr)
        assertEquals(2, tx.signatures.size)
        assertEquals("sig1", tx.signatures[0])
        assertEquals("sig2", tx.signatures[1])
        assertEquals("text", tx.memoType)
        assertEquals("hello world", tx.memoValue)
        assertEquals("aGVsbG8gd29ybGQ=", tx.memoBytes)
        // These should be null for this test
        assertNull(tx.preconditions)
        assertNull(tx.feeBumpTransaction)
        assertNull(tx.innerTransaction)
    }

    @Test
    fun testPreconditionsInnerClassCoverage() {
        val transactionWithPreconditionsJson = """
        {
            "id": "pre123",
            "paging_token": "pre123",
            "successful": true,
            "hash": "prehash",
            "ledger": 1000,
            "created_at": "2021-06-01T12:00:00Z",
            "source_account": "GPRE123",
            "source_account_sequence": 9999,
            "fee_account": "GPRE123",
            "fee_charged": 500,
            "max_fee": 1000,
            "operation_count": 2,
            "signatures": ["presig"],
            "memo_type": "none",
            "preconditions": {
                "timebounds": {
                    "min_time": "1622548800",
                    "max_time": "1622635200"
                },
                "ledgerbounds": {
                    "min_ledger": 1000,
                    "max_ledger": 2000
                },
                "min_account_sequence": 9900,
                "min_account_sequence_age": 3600,
                "min_account_sequence_ledger_gap": 10,
                "extra_signers": ["GSIGNER1", "GSIGNER2", "GSIGNER3"]
            },
            "_links": {
                "self": {"href": "self"},
                "account": {"href": "account"},
                "ledger": {"href": "ledger"},
                "operations": {"href": "ops"},
                "effects": {"href": "fx"},
                "precedes": {"href": "pre"},
                "succeeds": {"href": "suc"}
            }
        }
        """.trimIndent()

        val tx = json.decodeFromString<TransactionResponse>(transactionWithPreconditionsJson)
        
        // Test ALL properties of Preconditions inner class
        val preconditions = tx.preconditions
        assertNotNull(preconditions)
        
        // Test TimeBounds inner class - ALL properties
        val timeBounds = preconditions.timeBounds
        assertNotNull(timeBounds)
        assertEquals("1622548800", timeBounds.minTime)
        assertEquals("1622635200", timeBounds.maxTime)
        
        // Test LedgerBounds inner class - ALL properties  
        val ledgerBounds = preconditions.ledgerBounds
        assertNotNull(ledgerBounds)
        assertEquals(1000L, ledgerBounds.minLedger)
        assertEquals(2000L, ledgerBounds.maxLedger)
        
        // Test ALL remaining Preconditions properties
        assertEquals(9900L, preconditions.minAccountSequence)
        assertEquals(3600L, preconditions.minAccountSequenceAge)
        assertEquals(10L, preconditions.minAccountSequenceLedgerGap)
        val extraSigners = preconditions.extraSigners
        assertNotNull(extraSigners)
        assertEquals(3, extraSigners.size)
        assertEquals("GSIGNER1", extraSigners[0])
        assertEquals("GSIGNER2", extraSigners[1])
        assertEquals("GSIGNER3", extraSigners[2])
    }

    @Test
    fun testFeeBumpTransactionInnerClassCoverage() {
        val feeBumpJson = """
        {
            "id": "bump123",
            "paging_token": "bump123",
            "successful": true,
            "hash": "bumphash",
            "ledger": 2000,
            "created_at": "2021-07-01T12:00:00Z",
            "source_account": "GBUMP123",
            "source_account_sequence": 8888,
            "fee_account": "GBUMP123",
            "fee_charged": 1000,
            "max_fee": 2000,
            "operation_count": 1,
            "signatures": ["bumpsig"],
            "memo_type": "none",
            "fee_bump_transaction": {
                "hash": "feebumphash123",
                "signatures": ["feebumpsig1", "feebumpsig2", "feebumpsig3"]
            },
            "_links": {
                "self": {"href": "self"},
                "account": {"href": "account"},
                "ledger": {"href": "ledger"},
                "operations": {"href": "ops"},
                "effects": {"href": "fx"},
                "precedes": {"href": "pre"},
                "succeeds": {"href": "suc"}
            }
        }
        """.trimIndent()

        val tx = json.decodeFromString<TransactionResponse>(feeBumpJson)
        
        // Test ALL properties of FeeBumpTransaction inner class
        val feeBumpTransaction = tx.feeBumpTransaction
        assertNotNull(feeBumpTransaction)
        
        assertEquals("feebumphash123", feeBumpTransaction.hash)
        val fbSignatures = feeBumpTransaction.signatures
        assertEquals(3, fbSignatures.size)
        assertEquals("feebumpsig1", fbSignatures[0])
        assertEquals("feebumpsig2", fbSignatures[1])
        assertEquals("feebumpsig3", fbSignatures[2])
        
        // Ensure other inner classes are null for this test
        assertNull(tx.preconditions)
        assertNull(tx.innerTransaction)
    }

    @Test
    fun testInnerTransactionInnerClassCoverage() {
        val innerTxJson = """
        {
            "id": "inner123", 
            "paging_token": "inner123",
            "successful": true,
            "hash": "innerhash",
            "ledger": 3000,
            "created_at": "2021-08-01T12:00:00Z",
            "source_account": "GINNER123",
            "source_account_sequence": 7777,
            "fee_account": "GINNER123",
            "fee_charged": 150,
            "max_fee": 300,
            "operation_count": 1,
            "signatures": ["innersig"],
            "memo_type": "none",
            "inner_transaction": {
                "hash": "innerhashabc",
                "signatures": ["innersig1", "innersig2"],
                "max_fee": 250
            },
            "_links": {
                "self": {"href": "self"},
                "account": {"href": "account"},
                "ledger": {"href": "ledger"},
                "operations": {"href": "ops"},
                "effects": {"href": "fx"},
                "precedes": {"href": "pre"},
                "succeeds": {"href": "suc"}
            }
        }
        """.trimIndent()

        val tx = json.decodeFromString<TransactionResponse>(innerTxJson)
        
        // Test ALL properties of InnerTransaction inner class
        val innerTransaction = tx.innerTransaction
        assertNotNull(innerTransaction)
        
        assertEquals("innerhashabc", innerTransaction.hash)
        val innerSignatures = innerTransaction.signatures  
        assertEquals(2, innerSignatures.size)
        assertEquals("innersig1", innerSignatures[0])
        assertEquals("innersig2", innerSignatures[1])
        assertEquals(250L, innerTransaction.maxFee)
        
        // Ensure other inner classes are null for this test
        assertNull(tx.preconditions)
        assertNull(tx.feeBumpTransaction)
    }

    @Test
    fun testDirectInnerClassConstruction() {
        // Test direct construction of inner classes to ensure all properties are covered
        
        // Test Links construction
        val links = TransactionResponse.Links(
            self = Link("https://self.com"),
            account = Link("https://account.com"),
            ledger = Link("https://ledger.com"),
            operations = Link("https://operations.com"),
            effects = Link("https://effects.com"),
            precedes = Link("https://precedes.com"),
            succeeds = Link("https://succeeds.com")
        )
        assertEquals("https://self.com", links.self.href)
        assertEquals("https://account.com", links.account.href)
        assertEquals("https://ledger.com", links.ledger.href)
        assertEquals("https://operations.com", links.operations.href)
        assertEquals("https://effects.com", links.effects.href)
        assertEquals("https://precedes.com", links.precedes.href)
        assertEquals("https://succeeds.com", links.succeeds.href)

        // Test TimeBounds construction
        val timeBounds = TransactionResponse.Preconditions.TimeBounds(
            minTime = "1000",
            maxTime = "2000"
        )
        assertEquals("1000", timeBounds.minTime)
        assertEquals("2000", timeBounds.maxTime)

        // Test LedgerBounds construction
        val ledgerBounds = TransactionResponse.Preconditions.LedgerBounds(
            minLedger = 100L,
            maxLedger = 200L
        )
        assertEquals(100L, ledgerBounds.minLedger)
        assertEquals(200L, ledgerBounds.maxLedger)

        // Test Preconditions construction
        val preconditions = TransactionResponse.Preconditions(
            timeBounds = timeBounds,
            ledgerBounds = ledgerBounds,
            minAccountSequence = 500L,
            minAccountSequenceAge = 600L,
            minAccountSequenceLedgerGap = 10L,
            extraSigners = listOf("signer1", "signer2")
        )
        assertEquals(timeBounds, preconditions.timeBounds)
        assertEquals(ledgerBounds, preconditions.ledgerBounds)
        assertEquals(500L, preconditions.minAccountSequence)
        assertEquals(600L, preconditions.minAccountSequenceAge)
        assertEquals(10L, preconditions.minAccountSequenceLedgerGap)
        assertEquals(2, preconditions.extraSigners?.size)
        assertEquals("signer1", preconditions.extraSigners?.get(0))

        // Test FeeBumpTransaction construction
        val feeBumpTx = TransactionResponse.FeeBumpTransaction(
            hash = "bumphashabc",
            signatures = listOf("bumpsig1", "bumpsig2")
        )
        assertEquals("bumphashabc", feeBumpTx.hash)
        assertEquals(2, feeBumpTx.signatures.size)
        assertEquals("bumpsig1", feeBumpTx.signatures[0])
        assertEquals("bumpsig2", feeBumpTx.signatures[1])

        // Test InnerTransaction construction
        val innerTx = TransactionResponse.InnerTransaction(
            hash = "innerhashabc",
            signatures = listOf("innersig1"),
            maxFee = 1000L
        )
        assertEquals("innerhashabc", innerTx.hash)
        assertEquals(1, innerTx.signatures.size)
        assertEquals("innersig1", innerTx.signatures[0])
        assertEquals(1000L, innerTx.maxFee)
    }

    @Test
    fun testPreconditionsWithNullFields() {
        val minimalPreconditionsJson = """
        {
            "id": "minimal123",
            "paging_token": "minimal123",
            "successful": false,
            "hash": "minimalhash",
            "ledger": 100,
            "created_at": "2021-01-01T00:00:00Z",
            "source_account": "GMINIMAL",
            "source_account_sequence": 1,
            "fee_account": "GMINIMAL",
            "fee_charged": 100,
            "max_fee": 100,
            "operation_count": 0,
            "signatures": [],
            "memo_type": "none",
            "preconditions": {},
            "_links": {
                "self": {"href": "self"},
                "account": {"href": "account"},
                "ledger": {"href": "ledger"},
                "operations": {"href": "ops"},
                "effects": {"href": "fx"},
                "precedes": {"href": "pre"},
                "succeeds": {"href": "suc"}
            }
        }
        """.trimIndent()

        val tx = json.decodeFromString<TransactionResponse>(minimalPreconditionsJson)
        
        // Test Preconditions with all null/missing fields
        val preconditions = tx.preconditions
        assertNotNull(preconditions)
        
        // ALL should be null for minimal preconditions
        assertNull(preconditions.timeBounds)
        assertNull(preconditions.ledgerBounds)
        assertNull(preconditions.minAccountSequence)
        assertNull(preconditions.minAccountSequenceAge)
        assertNull(preconditions.minAccountSequenceLedgerGap)
        assertNull(preconditions.extraSigners)
    }

    @Test
    fun testTimeBoundsWithNullFields() {
        val timeBoundsNullJson = """
        {
            "id": "time123",
            "paging_token": "time123",
            "successful": true,
            "hash": "timehash",
            "ledger": 200,
            "created_at": "2021-01-01T00:00:00Z",
            "source_account": "GTIME",
            "source_account_sequence": 2,
            "fee_account": "GTIME",
            "fee_charged": 100,
            "max_fee": 100,
            "operation_count": 0,
            "signatures": [],
            "memo_type": "none",
            "preconditions": {
                "timebounds": {}
            },
            "_links": {
                "self": {"href": "self"},
                "account": {"href": "account"},
                "ledger": {"href": "ledger"},
                "operations": {"href": "ops"},
                "effects": {"href": "fx"},
                "precedes": {"href": "pre"},
                "succeeds": {"href": "suc"}
            }
        }
        """.trimIndent()

        val tx = json.decodeFromString<TransactionResponse>(timeBoundsNullJson)
        
        val timeBounds = tx.preconditions?.timeBounds
        assertNotNull(timeBounds)
        
        // Both should be null for empty timebounds
        assertNull(timeBounds.minTime)
        assertNull(timeBounds.maxTime)
    }
}