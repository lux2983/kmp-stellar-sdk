package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.PathResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathResponseCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testComprehensivePathResponseDeserialization() {
        val pathJson = """
        {
            "destination_amount": "1000.0000000",
            "destination_asset_type": "credit_alphanum4",
            "destination_asset_code": "USD",
            "destination_asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
            "source_amount": "500.0000000",
            "source_asset_type": "native",
            "source_asset_code": null,
            "source_asset_issuer": null,
            "path": [
                {
                    "asset_type": "credit_alphanum4",
                    "asset_code": "EUR",
                    "asset_issuer": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU"
                },
                {
                    "asset_type": "credit_alphanum12",
                    "asset_code": "LONGASSETCODE",
                    "asset_issuer": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7"
                }
            ]
        }
        """.trimIndent()

        val pathResponse = json.decodeFromString<PathResponse>(pathJson)

        // Test ALL destination properties
        assertEquals("1000.0000000", pathResponse.destinationAmount)
        assertEquals("credit_alphanum4", pathResponse.destinationAssetType)
        assertEquals("USD", pathResponse.destinationAssetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", pathResponse.destinationAssetIssuer)

        // Test ALL source properties
        assertEquals("500.0000000", pathResponse.sourceAmount)
        assertEquals("native", pathResponse.sourceAssetType)
        assertNull(pathResponse.sourceAssetCode)
        assertNull(pathResponse.sourceAssetIssuer)

        // Test path list with multiple assets
        assertEquals(2, pathResponse.path.size)
        
        val firstPathAsset = pathResponse.path[0]
        assertEquals("credit_alphanum4", firstPathAsset.assetType)
        assertEquals("EUR", firstPathAsset.assetCode)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", firstPathAsset.assetIssuer)
        
        val secondPathAsset = pathResponse.path[1]
        assertEquals("credit_alphanum12", secondPathAsset.assetType)
        assertEquals("LONGASSETCODE", secondPathAsset.assetCode)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", secondPathAsset.assetIssuer)

        // Test getDestinationAsset() method
        val destinationAsset = pathResponse.getDestinationAsset()
        assertEquals("credit_alphanum4", destinationAsset.assetType)
        assertEquals("USD", destinationAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", destinationAsset.assetIssuer)

        // Test getSourceAsset() method
        val sourceAsset = pathResponse.getSourceAsset()
        assertEquals("native", sourceAsset.assetType)
        assertNull(sourceAsset.assetCode)
        assertNull(sourceAsset.assetIssuer)
    }

    @Test
    fun testPathResponseWithNativeToCredit12() {
        val pathJson = """
        {
            "destination_amount": "2500.0000000",
            "destination_asset_type": "credit_alphanum12",
            "destination_asset_code": "VERYLONGCODE",
            "destination_asset_issuer": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
            "source_amount": "750.0000000",
            "source_asset_type": "native",
            "source_asset_code": null,
            "source_asset_issuer": null,
            "path": [
                {
                    "asset_type": "native"
                }
            ]
        }
        """.trimIndent()

        val pathResponse = json.decodeFromString<PathResponse>(pathJson)

        // Test all properties
        assertEquals("2500.0000000", pathResponse.destinationAmount)
        assertEquals("credit_alphanum12", pathResponse.destinationAssetType)
        assertEquals("VERYLONGCODE", pathResponse.destinationAssetCode)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", pathResponse.destinationAssetIssuer)
        
        assertEquals("750.0000000", pathResponse.sourceAmount)
        assertEquals("native", pathResponse.sourceAssetType)
        assertNull(pathResponse.sourceAssetCode)
        assertNull(pathResponse.sourceAssetIssuer)

        // Test path with single native asset
        assertEquals(1, pathResponse.path.size)
        assertEquals("native", pathResponse.path[0].assetType)
        assertNull(pathResponse.path[0].assetCode)
        assertNull(pathResponse.path[0].assetIssuer)

        // Test methods
        val destinationAsset = pathResponse.getDestinationAsset()
        assertEquals("credit_alphanum12", destinationAsset.assetType)
        assertEquals("VERYLONGCODE", destinationAsset.assetCode)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", destinationAsset.assetIssuer)

        val sourceAsset = pathResponse.getSourceAsset()
        assertEquals("native", sourceAsset.assetType)
        assertNull(sourceAsset.assetCode)
        assertNull(sourceAsset.assetIssuer)
    }

    @Test
    fun testPathResponseWithCreditToCredit() {
        val pathJson = """
        {
            "destination_amount": "100.0000000",
            "destination_asset_type": "credit_alphanum4",
            "destination_asset_code": "EUR",
            "destination_asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
            "source_amount": "110.0000000",
            "source_asset_type": "credit_alphanum4",
            "source_asset_code": "USD",
            "source_asset_issuer": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU",
            "path": []
        }
        """.trimIndent()

        val pathResponse = json.decodeFromString<PathResponse>(pathJson)

        // Test all properties for credit-to-credit conversion
        assertEquals("100.0000000", pathResponse.destinationAmount)
        assertEquals("credit_alphanum4", pathResponse.destinationAssetType)
        assertEquals("EUR", pathResponse.destinationAssetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", pathResponse.destinationAssetIssuer)
        
        assertEquals("110.0000000", pathResponse.sourceAmount)
        assertEquals("credit_alphanum4", pathResponse.sourceAssetType)
        assertEquals("USD", pathResponse.sourceAssetCode)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", pathResponse.sourceAssetIssuer)

        // Test empty path (direct conversion)
        assertTrue(pathResponse.path.isEmpty())

        // Test methods with credit assets
        val destinationAsset = pathResponse.getDestinationAsset()
        assertEquals("credit_alphanum4", destinationAsset.assetType)
        assertEquals("EUR", destinationAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", destinationAsset.assetIssuer)

        val sourceAsset = pathResponse.getSourceAsset()
        assertEquals("credit_alphanum4", sourceAsset.assetType)
        assertEquals("USD", sourceAsset.assetCode)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", sourceAsset.assetIssuer)
    }

    @Test
    fun testPathResponseWithComplexPath() {
        val pathJson = """
        {
            "destination_amount": "5000.0000000",
            "destination_asset_type": "credit_alphanum4",
            "destination_asset_code": "BTC",
            "destination_asset_issuer": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "source_amount": "150000.0000000",
            "source_asset_type": "native",
            "source_asset_code": null,
            "source_asset_issuer": null,
            "path": [
                {
                    "asset_type": "credit_alphanum4",
                    "asset_code": "USD",
                    "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
                },
                {
                    "asset_type": "credit_alphanum4",
                    "asset_code": "EUR",
                    "asset_issuer": "GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU"
                },
                {
                    "asset_type": "native"
                },
                {
                    "asset_type": "credit_alphanum12",
                    "asset_code": "STABLECOIN12",
                    "asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B"
                }
            ]
        }
        """.trimIndent()

        val pathResponse = json.decodeFromString<PathResponse>(pathJson)

        // Test all properties
        assertEquals("5000.0000000", pathResponse.destinationAmount)
        assertEquals("credit_alphanum4", pathResponse.destinationAssetType)
        assertEquals("BTC", pathResponse.destinationAssetCode)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", pathResponse.destinationAssetIssuer)
        
        assertEquals("150000.0000000", pathResponse.sourceAmount)
        assertEquals("native", pathResponse.sourceAssetType)
        assertNull(pathResponse.sourceAssetCode)
        assertNull(pathResponse.sourceAssetIssuer)

        // Test complex path with 4 intermediate assets
        assertEquals(4, pathResponse.path.size)
        
        // First: USD
        assertEquals("credit_alphanum4", pathResponse.path[0].assetType)
        assertEquals("USD", pathResponse.path[0].assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", pathResponse.path[0].assetIssuer)
        
        // Second: EUR
        assertEquals("credit_alphanum4", pathResponse.path[1].assetType)
        assertEquals("EUR", pathResponse.path[1].assetCode)
        assertEquals("GBVFTZL5HIPT4PFQVTZVIWR77V7LWYCXU4CLYWWHHOEXB64XPG5LDMTU", pathResponse.path[1].assetIssuer)
        
        // Third: Native
        assertEquals("native", pathResponse.path[2].assetType)
        assertNull(pathResponse.path[2].assetCode)
        assertNull(pathResponse.path[2].assetIssuer)
        
        // Fourth: STABLECOIN12
        assertEquals("credit_alphanum12", pathResponse.path[3].assetType)
        assertEquals("STABLECOIN12", pathResponse.path[3].assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", pathResponse.path[3].assetIssuer)

        // Test methods
        val destinationAsset = pathResponse.getDestinationAsset()
        assertEquals("credit_alphanum4", destinationAsset.assetType)
        assertEquals("BTC", destinationAsset.assetCode)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", destinationAsset.assetIssuer)

        val sourceAsset = pathResponse.getSourceAsset()
        assertEquals("native", sourceAsset.assetType)
        assertNull(sourceAsset.assetCode)
        assertNull(sourceAsset.assetIssuer)
    }

    @Test
    fun testPathResponseMethodEdgeCases() {
        // Test methods with various asset type combinations
        val pathJson = """
        {
            "destination_amount": "1.0000000",
            "destination_asset_type": "native",
            "destination_asset_code": null,
            "destination_asset_issuer": null,
            "source_amount": "1.0000000",
            "source_asset_type": "credit_alphanum12",
            "source_asset_code": "MAXLENGTHCODE",
            "source_asset_issuer": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "path": []
        }
        """.trimIndent()

        val pathResponse = json.decodeFromString<PathResponse>(pathJson)

        // Test edge case: native destination, credit12 source
        assertEquals("1.0000000", pathResponse.destinationAmount)
        assertEquals("native", pathResponse.destinationAssetType)
        assertNull(pathResponse.destinationAssetCode)
        assertNull(pathResponse.destinationAssetIssuer)
        
        assertEquals("1.0000000", pathResponse.sourceAmount)
        assertEquals("credit_alphanum12", pathResponse.sourceAssetType)
        assertEquals("MAXLENGTHCODE", pathResponse.sourceAssetCode)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", pathResponse.sourceAssetIssuer)

        assertTrue(pathResponse.path.isEmpty())

        // Test getDestinationAsset() method with native
        val destinationAsset = pathResponse.getDestinationAsset()
        assertEquals("native", destinationAsset.assetType)
        assertNull(destinationAsset.assetCode)
        assertNull(destinationAsset.assetIssuer)

        // Test getSourceAsset() method with credit_alphanum12
        val sourceAsset = pathResponse.getSourceAsset()
        assertEquals("credit_alphanum12", sourceAsset.assetType)
        assertEquals("MAXLENGTHCODE", sourceAsset.assetCode)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", sourceAsset.assetIssuer)
    }
}