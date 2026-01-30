package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.AccountResponse
import com.soneso.stellar.sdk.horizon.responses.Link
import kotlinx.serialization.json.Json
import kotlin.test.*

class AccountResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testAccountResponseAllInnerClassesFullCoverage() {
        val accountResponseJson = """
        {
            "id": "GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK",
            "account_id": "GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK",
            "sequence": "12884901891",
            "sequence_ledger": 43466843,
            "sequence_time": 1684157425,
            "subentry_count": 5,
            "inflation_destination": "GDCHDRSDOBRMSUDKRE2C4U4KDLNEATJPIHHR2ORFL5BSD56G4DQXL4VW",
            "home_domain": "stellar.org", 
            "last_modified_ledger": 43466843,
            "last_modified_time": "2023-05-15T14:30:25Z",
            "thresholds": {
                "low_threshold": 1,
                "med_threshold": 2,
                "high_threshold": 3
            },
            "flags": {
                "auth_required": true,
                "auth_revocable": false,
                "auth_immutable": true,
                "auth_clawback_enabled": false
            },
            "balances": [
                {
                    "asset_type": "native",
                    "balance": "9999.9999000",
                    "buying_liabilities": "100.0000000",
                    "selling_liabilities": "200.0000000"
                },
                {
                    "asset_type": "credit_alphanum4",
                    "asset_code": "USD",
                    "asset_issuer": "GACKTN5DAZGWXRWB2WLM6OPBDHAMT6SJNGLJZPQMEZBUR4JUGBX2UK7V",
                    "limit": "10000.0000000",
                    "balance": "5000.0000000",
                    "buying_liabilities": "50.0000000",
                    "selling_liabilities": "75.0000000",
                    "is_authorized": true,
                    "is_authorized_to_maintain_liabilities": true,
                    "is_clawback_enabled": false,
                    "last_modified_ledger": 43466840,
                    "sponsor": "GSPONSORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
                },
                {
                    "asset_type": "credit_alphanum12",
                    "asset_code": "LONGASSET",
                    "asset_issuer": "GCKFBEIYTKP5RDHGA7BLEQGESGZ6Q6FE2MLJM5Q5P4LGWGDHTPBZQKSV",
                    "limit": "1000000.0000000",
                    "balance": "250000.0000000",
                    "buying_liabilities": "1000.0000000", 
                    "selling_liabilities": "2000.0000000",
                    "is_authorized": false,
                    "is_authorized_to_maintain_liabilities": false,
                    "is_clawback_enabled": true,
                    "last_modified_ledger": 43466841
                },
                {
                    "asset_type": "liquidity_pool_shares",
                    "liquidity_pool_id": "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890",
                    "limit": "1000.0000000",
                    "balance": "500.0000000",
                    "last_modified_ledger": 43466842,
                    "sponsor": "GLIQUIDITYPOOL123456789012345678901234567890"
                }
            ],
            "signers": [
                {
                    "key": "GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK",
                    "type": "ed25519_public_key",
                    "weight": 1,
                    "sponsor": "GSIGNERONEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"
                },
                {
                    "key": "XRPKEY1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                    "type": "sha256_hash",
                    "weight": 2
                },
                {
                    "key": "TXKEY9876543210ZYXWVUTSRQPONMLKJIHGFEDCBA", 
                    "type": "preauth_tx",
                    "weight": 3,
                    "sponsor": "GSIGNERTWO1111111111111111111111111111111"
                }
            ],
            "data": {
                "config": "dGVzdCBjb25maWc=",
                "settings": "c2V0dGluZ3M="
            },
            "num_sponsoring": 10,
            "num_sponsored": 5,
            "sponsor": "GMASTERSPONSORRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
            "paging_token": "GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK",
            "_links": {
                "self": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK"},
                "transactions": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/transactions"},
                "operations": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/operations"},
                "payments": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/payments"},
                "effects": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/effects"},
                "offers": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/offers"},
                "trades": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/trades"},
                "data": {"href": "https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/data/{key}"}
            }
        }
        """.trimIndent()

        val account = json.decodeFromString<AccountResponse>(accountResponseJson)

        // Test ALL main AccountResponse properties
        assertEquals("GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK", account.id)
        assertEquals("GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK", account.accountId)
        assertEquals(12884901891L, account.sequenceNumber)
        assertEquals(43466843L, account.sequenceLedger)
        assertEquals(1684157425L, account.sequenceTime)
        assertEquals(5, account.subentryCount)
        assertEquals("GDCHDRSDOBRMSUDKRE2C4U4KDLNEATJPIHHR2ORFL5BSD56G4DQXL4VW", account.inflationDestination)
        assertEquals("stellar.org", account.homeDomain)
        assertEquals(43466843, account.lastModifiedLedger)
        assertEquals("2023-05-15T14:30:25Z", account.lastModifiedTime)
        assertEquals(10, account.numSponsoring)
        assertEquals(5, account.numSponsored)
        assertEquals("GMASTERSPONSORRRRRRRRRRRRRRRRRRRRRRRRRRRRR", account.sponsor)
        assertEquals("GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK", account.pagingToken)
        assertEquals(2, account.data.size)
        assertEquals("dGVzdCBjb25maWc=", account.data["config"])
        assertEquals("c2V0dGluZ3M=", account.data["settings"])

        // Test getIncrementedSequenceNumber() function
        assertEquals(12884901892L, account.getIncrementedSequenceNumber())

        // Test ALL properties of Thresholds inner class
        val thresholds = account.thresholds
        assertNotNull(thresholds)
        assertEquals(1, thresholds.lowThreshold)
        assertEquals(2, thresholds.medThreshold)
        assertEquals(3, thresholds.highThreshold)

        // Test ALL properties of Flags inner class
        val flags = account.flags
        assertNotNull(flags)
        assertTrue(flags.authRequired)
        assertFalse(flags.authRevocable)
        assertTrue(flags.authImmutable)
        assertFalse(flags.authClawbackEnabled)

        // Test ALL properties of Balance inner class - ALL balances
        val balances = account.balances
        assertEquals(4, balances.size)

        // Native balance
        val nativeBalance = balances[0]
        assertEquals("native", nativeBalance.assetType)
        assertNull(nativeBalance.assetCode)
        assertNull(nativeBalance.assetIssuer)
        assertNull(nativeBalance.liquidityPoolId)
        assertNull(nativeBalance.limit)
        assertEquals("9999.9999000", nativeBalance.balance)
        assertEquals("100.0000000", nativeBalance.buyingLiabilities)
        assertEquals("200.0000000", nativeBalance.sellingLiabilities)
        assertNull(nativeBalance.isAuthorized)
        assertNull(nativeBalance.isAuthorizedToMaintainLiabilities)
        assertNull(nativeBalance.isClawbackEnabled)
        assertNull(nativeBalance.lastModifiedLedger)
        assertNull(nativeBalance.sponsor)

        // USD credit balance
        val usdBalance = balances[1]
        assertEquals("credit_alphanum4", usdBalance.assetType)
        assertEquals("USD", usdBalance.assetCode)
        assertEquals("GACKTN5DAZGWXRWB2WLM6OPBDHAMT6SJNGLJZPQMEZBUR4JUGBX2UK7V", usdBalance.assetIssuer)
        assertNull(usdBalance.liquidityPoolId)
        assertEquals("10000.0000000", usdBalance.limit)
        assertEquals("5000.0000000", usdBalance.balance)
        assertEquals("50.0000000", usdBalance.buyingLiabilities)
        assertEquals("75.0000000", usdBalance.sellingLiabilities)
        assertEquals(true, usdBalance.isAuthorized)
        assertEquals(true, usdBalance.isAuthorizedToMaintainLiabilities)
        assertEquals(false, usdBalance.isClawbackEnabled)
        assertEquals(43466840, usdBalance.lastModifiedLedger)
        assertEquals("GSPONSORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR", usdBalance.sponsor)

        // Long asset credit balance
        val longAssetBalance = balances[2]
        assertEquals("credit_alphanum12", longAssetBalance.assetType)
        assertEquals("LONGASSET", longAssetBalance.assetCode)
        assertEquals("GCKFBEIYTKP5RDHGA7BLEQGESGZ6Q6FE2MLJM5Q5P4LGWGDHTPBZQKSV", longAssetBalance.assetIssuer)
        assertNull(longAssetBalance.liquidityPoolId)
        assertEquals("1000000.0000000", longAssetBalance.limit)
        assertEquals("250000.0000000", longAssetBalance.balance)
        assertEquals("1000.0000000", longAssetBalance.buyingLiabilities)
        assertEquals("2000.0000000", longAssetBalance.sellingLiabilities)
        assertEquals(false, longAssetBalance.isAuthorized)
        assertEquals(false, longAssetBalance.isAuthorizedToMaintainLiabilities)
        assertEquals(true, longAssetBalance.isClawbackEnabled)
        assertEquals(43466841, longAssetBalance.lastModifiedLedger)
        assertNull(longAssetBalance.sponsor)

        // Liquidity pool balance
        val poolBalance = balances[3]
        assertEquals("liquidity_pool_shares", poolBalance.assetType)
        assertNull(poolBalance.assetCode)
        assertNull(poolBalance.assetIssuer)
        assertEquals("abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890", poolBalance.liquidityPoolId)
        assertEquals("1000.0000000", poolBalance.limit)
        assertEquals("500.0000000", poolBalance.balance)
        assertNull(poolBalance.buyingLiabilities)
        assertNull(poolBalance.sellingLiabilities)
        assertNull(poolBalance.isAuthorized)
        assertNull(poolBalance.isAuthorizedToMaintainLiabilities)
        assertNull(poolBalance.isClawbackEnabled)
        assertEquals(43466842, poolBalance.lastModifiedLedger)
        assertEquals("GLIQUIDITYPOOL123456789012345678901234567890", poolBalance.sponsor)

        // Test ALL properties of Signer inner class - ALL signers
        val signers = account.signers
        assertEquals(3, signers.size)

        // First signer (ed25519)
        val signer1 = signers[0]
        assertEquals("GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK", signer1.key)
        assertEquals("ed25519_public_key", signer1.type)
        assertEquals(1, signer1.weight)
        assertEquals("GSIGNERONEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE", signer1.sponsor)

        // Second signer (sha256_hash)
        val signer2 = signers[1]
        assertEquals("XRPKEY1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ", signer2.key)
        assertEquals("sha256_hash", signer2.type)
        assertEquals(2, signer2.weight)
        assertNull(signer2.sponsor)

        // Third signer (preauth_tx)
        val signer3 = signers[2]
        assertEquals("TXKEY9876543210ZYXWVUTSRQPONMLKJIHGFEDCBA", signer3.key)
        assertEquals("preauth_tx", signer3.type)
        assertEquals(3, signer3.weight)
        assertEquals("GSIGNERTWO1111111111111111111111111111111", signer3.sponsor)

        // Test ALL properties of Links inner class
        val links = account.links
        assertNotNull(links)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK", links.self.href)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/transactions", links.transactions.href)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/operations", links.operations.href)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/payments", links.payments.href)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/effects", links.effects.href)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/offers", links.offers.href)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/trades", links.trades.href)
        assertEquals("https://horizon.stellar.org/accounts/GAIRISXKPLOWZBMFRPU5XJJMTOYEOAOHXGPXFC3BNR3TGIEYG3Q6VCDK/data/{key}", links.data.href)
    }

    @Test
    fun testAccountResponseWithNullableFields() {
        val minimalAccountJson = """
        {
            "id": "GMINIMAL123456789012345678901234567890123456",
            "account_id": "GMINIMAL123456789012345678901234567890123456",
            "sequence": "100",
            "subentry_count": 0,
            "last_modified_ledger": 1000,
            "last_modified_time": "2022-01-01T00:00:00Z",
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
                    "asset_type": "native",
                    "balance": "1000.0000000"
                }
            ],
            "signers": [
                {
                    "key": "GMINIMAL123456789012345678901234567890123456",
                    "type": "ed25519_public_key",
                    "weight": 1
                }
            ],
            "data": {},
            "paging_token": "minimal",
            "_links": {
                "self": {"href": "self"},
                "transactions": {"href": "tx"},
                "operations": {"href": "ops"},
                "payments": {"href": "payments"},
                "effects": {"href": "effects"},
                "offers": {"href": "offers"},
                "trades": {"href": "trades"},
                "data": {"href": "data"}
            }
        }
        """.trimIndent()

        val account = json.decodeFromString<AccountResponse>(minimalAccountJson)

        // Test minimal required fields
        assertEquals("GMINIMAL123456789012345678901234567890123456", account.id)
        assertEquals(100L, account.sequenceNumber)
        assertEquals(101L, account.getIncrementedSequenceNumber())
        assertEquals(0, account.subentryCount)

        // Test nullable fields are null when not provided
        assertNull(account.sequenceLedger)
        assertNull(account.sequenceTime)
        assertNull(account.inflationDestination)
        assertNull(account.homeDomain)
        assertNull(account.numSponsoring)
        assertNull(account.numSponsored)
        assertNull(account.sponsor)

        // Test minimal balance
        val balance = account.balances[0]
        assertEquals("native", balance.assetType)
        assertEquals("1000.0000000", balance.balance)
        assertNull(balance.assetCode)
        assertNull(balance.assetIssuer)
        assertNull(balance.limit)
        assertNull(balance.buyingLiabilities)
        assertNull(balance.sellingLiabilities)
        assertNull(balance.isAuthorized)

        // Test minimal signer
        val signer = account.signers[0]
        assertEquals("GMINIMAL123456789012345678901234567890123456", signer.key)
        assertEquals("ed25519_public_key", signer.type)
        assertEquals(1, signer.weight)
        assertNull(signer.sponsor)

        // Test empty data map
        assertTrue(account.data.isEmpty())
    }

    @Test
    fun testDirectInnerClassesConstruction() {
        // Test direct construction of all inner classes to ensure all properties work

        // Test Thresholds
        val thresholds = AccountResponse.Thresholds(
            lowThreshold = 5,
            medThreshold = 10,
            highThreshold = 15
        )
        assertEquals(5, thresholds.lowThreshold)
        assertEquals(10, thresholds.medThreshold)
        assertEquals(15, thresholds.highThreshold)

        // Test Flags
        val flags = AccountResponse.Flags(
            authRequired = true,
            authRevocable = false,
            authImmutable = true,
            authClawbackEnabled = false
        )
        assertTrue(flags.authRequired)
        assertFalse(flags.authRevocable)
        assertTrue(flags.authImmutable)
        assertFalse(flags.authClawbackEnabled)

        // Test Balance
        val balance = AccountResponse.Balance(
            assetType = "credit_alphanum4",
            assetCode = "EUR",
            assetIssuer = "GTEST123",
            liquidityPoolId = null,
            limit = "50000.0000000",
            balance = "25000.0000000",
            buyingLiabilities = "100.0000000",
            sellingLiabilities = "200.0000000",
            isAuthorized = true,
            isAuthorizedToMaintainLiabilities = false,
            isClawbackEnabled = true,
            lastModifiedLedger = 12345,
            sponsor = "GSPONSOR123"
        )
        assertEquals("credit_alphanum4", balance.assetType)
        assertEquals("EUR", balance.assetCode)
        assertEquals("GTEST123", balance.assetIssuer)
        assertEquals("50000.0000000", balance.limit)
        assertEquals("25000.0000000", balance.balance)
        assertEquals("100.0000000", balance.buyingLiabilities)
        assertEquals("200.0000000", balance.sellingLiabilities)
        assertTrue(balance.isAuthorized!!)
        assertFalse(balance.isAuthorizedToMaintainLiabilities!!)
        assertTrue(balance.isClawbackEnabled!!)
        assertEquals(12345, balance.lastModifiedLedger)
        assertEquals("GSPONSOR123", balance.sponsor)

        // Test Signer
        val signer = AccountResponse.Signer(
            key = "GSIGNER123456789012345678901234567890123456",
            type = "ed25519_public_key",
            weight = 5,
            sponsor = "GSPONSOR456"
        )
        assertEquals("GSIGNER123456789012345678901234567890123456", signer.key)
        assertEquals("ed25519_public_key", signer.type)
        assertEquals(5, signer.weight)
        assertEquals("GSPONSOR456", signer.sponsor)

        // Test Links
        val links = AccountResponse.Links(
            self = Link("https://self.com"),
            transactions = Link("https://transactions.com"),
            operations = Link("https://operations.com"),
            payments = Link("https://payments.com"),
            effects = Link("https://effects.com"),
            offers = Link("https://offers.com"),
            trades = Link("https://trades.com"),
            data = Link("https://data.com")
        )
        assertEquals("https://self.com", links.self.href)
        assertEquals("https://transactions.com", links.transactions.href)
        assertEquals("https://operations.com", links.operations.href)
        assertEquals("https://payments.com", links.payments.href)
        assertEquals("https://effects.com", links.effects.href)
        assertEquals("https://offers.com", links.offers.href)
        assertEquals("https://trades.com", links.trades.href)
        assertEquals("https://data.com", links.data.href)
    }

    @Test
    fun testGetIncrementedSequenceNumberFunction() {
        val account1Json = """
        {
            "id": "GTEST1",
            "account_id": "GTEST1",
            "sequence": "0",
            "subentry_count": 0,
            "last_modified_ledger": 1,
            "last_modified_time": "2022-01-01T00:00:00Z",
            "thresholds": {"low_threshold": 0, "med_threshold": 0, "high_threshold": 0},
            "flags": {"auth_required": false, "auth_revocable": false, "auth_immutable": false, "auth_clawback_enabled": false},
            "balances": [{"asset_type": "native", "balance": "0.0000000"}],
            "signers": [{"key": "GTEST1", "type": "ed25519_public_key", "weight": 1}],
            "data": {},
            "paging_token": "test1",
            "_links": {
                "self": {"href": "self"}, "transactions": {"href": "tx"}, "operations": {"href": "ops"},
                "payments": {"href": "payments"}, "effects": {"href": "effects"}, "offers": {"href": "offers"},
                "trades": {"href": "trades"}, "data": {"href": "data"}
            }
        }
        """.trimIndent()

        val account1 = json.decodeFromString<AccountResponse>(account1Json)
        assertEquals(0L, account1.sequenceNumber)
        assertEquals(1L, account1.getIncrementedSequenceNumber())

        val account2Json = account1Json.replace("\"sequence\": \"0\"", "\"sequence\": \"9223372036854775806\"")
        val account2 = json.decodeFromString<AccountResponse>(account2Json)
        assertEquals(9223372036854775806L, account2.sequenceNumber)
        assertEquals(9223372036854775807L, account2.getIncrementedSequenceNumber())

        val account3Json = account1Json.replace("\"sequence\": \"0\"", "\"sequence\": \"999999999\"")
        val account3 = json.decodeFromString<AccountResponse>(account3Json)
        assertEquals(999999999L, account3.sequenceNumber)
        assertEquals(1000000000L, account3.getIncrementedSequenceNumber())
    }

    @Test
    fun testComplexBalanceTypes() {
        val complexBalancesJson = """
        {
            "id": "GCOMPLEX",
            "account_id": "GCOMPLEX",
            "sequence": "100",
            "subentry_count": 10,
            "last_modified_ledger": 1000,
            "last_modified_time": "2022-01-01T00:00:00Z",
            "thresholds": {"low_threshold": 1, "med_threshold": 1, "high_threshold": 1},
            "flags": {"auth_required": false, "auth_revocable": false, "auth_immutable": false, "auth_clawback_enabled": false},
            "balances": [
                {
                    "asset_type": "native",
                    "balance": "0.0000000",
                    "buying_liabilities": "0.0000000",
                    "selling_liabilities": "0.0000000"
                },
                {
                    "asset_type": "credit_alphanum4",
                    "asset_code": "TEST", 
                    "asset_issuer": "GTESTISSUER",
                    "balance": "0.0000000",
                    "limit": "0.0000000",
                    "buying_liabilities": "0.0000000",
                    "selling_liabilities": "0.0000000",
                    "is_authorized": null,
                    "is_authorized_to_maintain_liabilities": null,
                    "is_clawback_enabled": null
                },
                {
                    "asset_type": "liquidity_pool_shares",
                    "liquidity_pool_id": "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                    "balance": "0.0000000",
                    "limit": "922337203685.4775807"
                }
            ],
            "signers": [{"key": "GCOMPLEX", "type": "ed25519_public_key", "weight": 1}],
            "data": {},
            "paging_token": "complex",
            "_links": {
                "self": {"href": "self"}, "transactions": {"href": "tx"}, "operations": {"href": "ops"},
                "payments": {"href": "payments"}, "effects": {"href": "effects"}, "offers": {"href": "offers"},
                "trades": {"href": "trades"}, "data": {"href": "data"}
            }
        }
        """.trimIndent()

        val account = json.decodeFromString<AccountResponse>(complexBalancesJson)

        // Test native balance with liabilities
        val nativeBalance = account.balances[0]
        assertEquals("0.0000000", nativeBalance.balance)
        assertEquals("0.0000000", nativeBalance.buyingLiabilities)
        assertEquals("0.0000000", nativeBalance.sellingLiabilities)

        // Test credit balance with null authorization flags
        val creditBalance = account.balances[1]
        assertEquals("TEST", creditBalance.assetCode)
        assertEquals("GTESTISSUER", creditBalance.assetIssuer)
        assertEquals("0.0000000", creditBalance.limit)
        assertNull(creditBalance.isAuthorized)
        assertNull(creditBalance.isAuthorizedToMaintainLiabilities)
        assertNull(creditBalance.isClawbackEnabled)

        // Test liquidity pool balance with max limit
        val poolBalance = account.balances[2]
        assertEquals("liquidity_pool_shares", poolBalance.assetType)
        assertEquals("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", poolBalance.liquidityPoolId)
        assertEquals("922337203685.4775807", poolBalance.limit)
        assertNull(poolBalance.assetCode)
        assertNull(poolBalance.assetIssuer)
    }

    @Test
    fun testMultipleSignerTypes() {
        val multiSignerJson = """
        {
            "id": "GMULTISIG",
            "account_id": "GMULTISIG",
            "sequence": "500",
            "subentry_count": 3,
            "last_modified_ledger": 2000,
            "last_modified_time": "2023-01-01T00:00:00Z",
            "thresholds": {"low_threshold": 2, "med_threshold": 4, "high_threshold": 6},
            "flags": {"auth_required": true, "auth_revocable": true, "auth_immutable": false, "auth_clawback_enabled": true},
            "balances": [{"asset_type": "native", "balance": "5000.0000000"}],
            "signers": [
                {
                    "key": "GMULTISIG",
                    "type": "ed25519_public_key", 
                    "weight": 2
                },
                {
                    "key": "TBA4ZUBKYSNGWQGIFQNQQLMJ4WMY3COXKJ73RTC7GJD2YMV3HNJ4EGGW",
                    "type": "sha256_hash",
                    "weight": 1
                },
                {
                    "key": "TBBW5FIKPZ2EBR7LGQVZLXKX3DXAMQTDQHHGIFX3T32KFI2GKY6QWPB2",
                    "type": "preauth_tx",
                    "weight": 3
                }
            ],
            "data": {
                "name": "TXVsdGktc2lnIEFjY291bnQ="
            },
            "paging_token": "multisig",
            "_links": {
                "self": {"href": "self"}, "transactions": {"href": "tx"}, "operations": {"href": "ops"},
                "payments": {"href": "payments"}, "effects": {"href": "effects"}, "offers": {"href": "offers"},
                "trades": {"href": "trades"}, "data": {"href": "data"}
            }
        }
        """.trimIndent()

        val account = json.decodeFromString<AccountResponse>(multiSignerJson)
        assertEquals(501L, account.getIncrementedSequenceNumber())

        // Test different thresholds
        assertEquals(2, account.thresholds.lowThreshold)
        assertEquals(4, account.thresholds.medThreshold)
        assertEquals(6, account.thresholds.highThreshold)

        // Test all flags
        assertTrue(account.flags.authRequired)
        assertTrue(account.flags.authRevocable)
        assertFalse(account.flags.authImmutable)
        assertTrue(account.flags.authClawbackEnabled)

        // Test multiple signers
        assertEquals(3, account.signers.size)
        assertEquals("ed25519_public_key", account.signers[0].type)
        assertEquals("sha256_hash", account.signers[1].type)
        assertEquals("preauth_tx", account.signers[2].type)
        assertEquals(2, account.signers[0].weight)
        assertEquals(1, account.signers[1].weight)
        assertEquals(3, account.signers[2].weight)

        // Test data field
        assertEquals(1, account.data.size)
        assertEquals("TXVsdGktc2lnIEFjY291bnQ=", account.data["name"])
    }
}