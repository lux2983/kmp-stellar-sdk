package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.PathResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val pathJson = """
    {
        "destination_amount": "20.0000000",
        "destination_asset_type": "credit_alphanum4",
        "destination_asset_code": "EUR",
        "destination_asset_issuer": "GDSBCQO34HWPGUGQSP3QBFEXVTSR2PW46UIGTHVWGWJGQKH3AFNHXHXN",
        "source_amount": "30.0000000",
        "source_asset_type": "credit_alphanum4",
        "source_asset_code": "USD",
        "source_asset_issuer": "GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B",
        "path": [
            {"asset_type": "native"},
            {"asset_type": "credit_alphanum4", "asset_code": "BTC", "asset_issuer": "GBTEST123"}
        ]
    }
    """.trimIndent()

    @Test
    fun testDeserialization() {
        val path = json.decodeFromString<PathResponse>(pathJson)
        assertEquals("20.0000000", path.destinationAmount)
        assertEquals("credit_alphanum4", path.destinationAssetType)
        assertEquals("EUR", path.destinationAssetCode)
        assertEquals("GDSBCQO34HWPGUGQSP3QBFEXVTSR2PW46UIGTHVWGWJGQKH3AFNHXHXN", path.destinationAssetIssuer)
        assertEquals("30.0000000", path.sourceAmount)
        assertEquals("credit_alphanum4", path.sourceAssetType)
        assertEquals("USD", path.sourceAssetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", path.sourceAssetIssuer)
    }

    @Test
    fun testPath() {
        val path = json.decodeFromString<PathResponse>(pathJson)
        assertEquals(2, path.path.size)
        assertTrue(path.path[0].isNative())
        assertEquals("BTC", path.path[1].assetCode)
    }

    @Test
    fun testGetDestinationAsset() {
        val path = json.decodeFromString<PathResponse>(pathJson)
        val destAsset = path.getDestinationAsset()
        assertEquals("credit_alphanum4", destAsset.assetType)
        assertEquals("EUR", destAsset.assetCode)
        assertEquals("GDSBCQO34HWPGUGQSP3QBFEXVTSR2PW46UIGTHVWGWJGQKH3AFNHXHXN", destAsset.assetIssuer)
    }

    @Test
    fun testGetSourceAsset() {
        val path = json.decodeFromString<PathResponse>(pathJson)
        val sourceAsset = path.getSourceAsset()
        assertEquals("credit_alphanum4", sourceAsset.assetType)
        assertEquals("USD", sourceAsset.assetCode)
        assertEquals("GCDNJUBQSX7AJWLJACMJ7I4BC3Z47BQUTMHEICZLE6MU4KQBRYG5JY6B", sourceAsset.assetIssuer)
    }

    @Test
    fun testNativePathDeserialization() {
        val nativePathJson = """
        {
            "destination_amount": "10.0",
            "destination_asset_type": "native",
            "source_amount": "10.0",
            "source_asset_type": "native",
            "path": []
        }
        """.trimIndent()
        val path = json.decodeFromString<PathResponse>(nativePathJson)
        assertEquals("native", path.destinationAssetType)
        assertNull(path.destinationAssetCode)
        assertNull(path.destinationAssetIssuer)
        assertEquals("native", path.sourceAssetType)
        assertNull(path.sourceAssetCode)
        assertNull(path.sourceAssetIssuer)
        assertTrue(path.path.isEmpty())

        val destAsset = path.getDestinationAsset()
        assertTrue(destAsset.isNative())
        val srcAsset = path.getSourceAsset()
        assertTrue(srcAsset.isNative())
    }
}
