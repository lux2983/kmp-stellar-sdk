package com.soneso.stellar.sdk.datalake

import com.soneso.stellar.sdk.StrKey
import kotlin.test.*

/**
 * Unit tests for XDR extension functions in DataLakeXdrExtensions.kt.
 *
 * Tests helper functions for hex conversion and contract ID encoding.
 */
class DataLakeXdrExtensionsTest {

    /**
     * Test ByteArray.toHexString() with known values.
     */
    @Test
    fun testToHexString_KnownValues() {
        val testCases = listOf(
            byteArrayOf(0x00) to "00",
            byteArrayOf(0xFF.toByte()) to "ff",
            byteArrayOf(0x01, 0x23, 0x45, 0x67, 0x89.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte()) to "0123456789abcdef",
            byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte()) to "deadbeef"
        )

        for ((bytes, expectedHex) in testCases) {
            val hex = bytes.toHexString()
            assertEquals(expectedHex, hex, "Hex string should match expected value")
        }
    }

    /**
     * Test ByteArray.toHexString() with empty array.
     */
    @Test
    fun testToHexString_EmptyArray() {
        val bytes = byteArrayOf()
        val hex = bytes.toHexString()

        assertEquals("", hex, "Empty byte array should produce empty string")
    }

    /**
     * Test ByteArray.toHexString() with single byte.
     */
    @Test
    fun testToHexString_SingleByte() {
        val testCases = listOf(
            0x00.toByte() to "00",
            0x0F.toByte() to "0f",
            0xF0.toByte() to "f0",
            0xFF.toByte() to "ff"
        )

        for ((byte, expectedHex) in testCases) {
            val bytes = byteArrayOf(byte)
            val hex = bytes.toHexString()
            assertEquals(expectedHex, hex, "Single byte should convert correctly")
        }
    }

    /**
     * Test ByteArray.toHexString() with transaction hash.
     *
     * Transaction hashes are 32-byte SHA-256 hashes.
     */
    @Test
    fun testToHexString_TransactionHash() {
        // Create a 32-byte array (typical transaction hash)
        val hash = ByteArray(32) { it.toByte() }
        val hex = hash.toHexString()

        assertEquals(64, hex.length, "32-byte hash should produce 64-character hex string")
        assertTrue(hex.matches(Regex("[0-9a-f]{64}")), "Hex string should be lowercase hexadecimal")
    }

    /**
     * Test ByteArray.toContractIdString() with known contract ID.
     */
    @Test
    fun testToContractIdString_ValidContractId() {
        // Create a 32-byte contract ID (all zeros for simplicity)
        val contractIdBytes = ByteArray(32) { 0 }

        val contractIdString = contractIdBytes.toContractIdString()

        // Verify it starts with 'C' (contract address prefix)
        assertTrue(contractIdString.startsWith("C"), "Contract ID should start with 'C'")

        // Verify it's a valid contract ID by decoding it
        val decoded = StrKey.decodeContract(contractIdString)
        assertTrue(
            decoded.contentEquals(contractIdBytes),
            "Decoded contract ID should match original bytes"
        )
    }

    /**
     * Test ByteArray.toContractIdString() with different contract IDs.
     */
    @Test
    fun testToContractIdString_DifferentContractIds() {
        // Test with different patterns
        val testCases = listOf(
            ByteArray(32) { 0 },           // All zeros
            ByteArray(32) { 0xFF.toByte() }, // All ones
            ByteArray(32) { it.toByte() }   // Sequential bytes
        )

        for (contractIdBytes in testCases) {
            val contractIdString = contractIdBytes.toContractIdString()

            // Verify round-trip
            val decoded = StrKey.decodeContract(contractIdString)
            assertTrue(
                decoded.contentEquals(contractIdBytes),
                "Contract ID should round-trip correctly"
            )
        }
    }

    /**
     * Test ByteArray.toContractIdString() with invalid length should fail.
     */
    @Test
    fun testToContractIdString_InvalidLength() {
        val testCases = listOf(
            ByteArray(0),   // Empty
            ByteArray(16),  // Too short
            ByteArray(31),  // One byte short
            ByteArray(33),  // One byte long
            ByteArray(64)   // Double length
        )

        for (invalidBytes in testCases) {
            val exception = assertFailsWith<IllegalArgumentException> {
                invalidBytes.toContractIdString()
            }

            assertTrue(
                exception.message?.contains("Contract ID must be 32 bytes") == true,
                "Should reject non-32-byte arrays with clear error message"
            )
        }
    }

    /**
     * Test ByteArray.toContractIdString() exactly 32 bytes.
     */
    @Test
    fun testToContractIdString_Exactly32Bytes() {
        val contractIdBytes = ByteArray(32) { (it * 7).toByte() } // Pseudo-random pattern
        val contractIdString = contractIdBytes.toContractIdString()

        // Verify it's a valid strkey
        assertTrue(contractIdString.startsWith("C"), "Should start with 'C'")
        assertTrue(contractIdString.length > 32, "Encoded string should be longer than raw bytes due to base32 and checksum")

        // Verify round-trip
        val decoded = StrKey.decodeContract(contractIdString)
        assertTrue(
            decoded.contentEquals(contractIdBytes),
            "Should decode to original bytes"
        )
    }

    /**
     * Test toHexString with all possible byte values.
     */
    @Test
    fun testToHexString_AllByteValues() {
        // Create array with all 256 possible byte values
        val allBytes = ByteArray(256) { it.toByte() }
        val hex = allBytes.toHexString()

        assertEquals(512, hex.length, "256 bytes should produce 512-character hex string")
        assertTrue(hex.matches(Regex("[0-9a-f]{512}")), "Should be valid lowercase hex")

        // Verify it's lowercase
        assertEquals(hex, hex.lowercase(), "Hex string should be lowercase")
    }

    /**
     * Test toHexString preserves leading zeros.
     */
    @Test
    fun testToHexString_PreservesLeadingZeros() {
        val bytes = byteArrayOf(0x00, 0x01, 0x00, 0xFF.toByte())
        val hex = bytes.toHexString()

        assertEquals("000100ff", hex, "Should preserve leading zeros in each byte")
    }

    /**
     * Test toContractIdString with known SAC contract ID.
     *
     * Stellar Asset Contracts have deterministic contract IDs.
     */
    @Test
    fun testToContractIdString_StellarAssetContract() {
        // This is a real SAC contract ID pattern (deterministic based on asset)
        // Using a test pattern here
        val contractIdBytes = ByteArray(32) { i ->
            when {
                i < 4 -> 0x00 // First 4 bytes often zeros in SAC IDs
                else -> (i * 13).toByte() // Pseudo-random pattern
            }
        }

        val contractIdString = contractIdBytes.toContractIdString()

        // Verify it's a valid contract ID
        assertTrue(contractIdString.startsWith("C"))

        // Verify length (base32 encoded 32 bytes + checksum)
        assertTrue(contractIdString.length in 55..60, "Contract ID string should be around 56 characters")

        // Verify round-trip
        val decoded = StrKey.decodeContract(contractIdString)
        assertTrue(
            decoded.contentEquals(contractIdBytes),
            "Contract ID should round-trip correctly"
        )
    }

    /**
     * Test toHexString matches expected format for transaction hashes.
     *
     * Transaction hashes should be 64-character lowercase hex strings.
     */
    @Test
    fun testToHexString_TransactionHashFormat() {
        // Simulate a transaction hash (32 random bytes)
        val txHash = ByteArray(32) { (it * 17 + 42).toByte() }
        val hex = txHash.toHexString()

        // Verify format
        assertEquals(64, hex.length, "Transaction hash should be 64 characters")
        assertTrue(
            hex.matches(Regex("[0-9a-f]{64}")),
            "Transaction hash should be lowercase hexadecimal"
        )

        // Verify no uppercase letters
        assertFalse(hex.contains(Regex("[A-F]")), "Should not contain uppercase letters")
    }

    /**
     * Test toContractIdString produces unique IDs for different inputs.
     */
    @Test
    fun testToContractIdString_UniquenessForDifferentInputs() {
        val contractId1 = ByteArray(32) { 0x00 }
        val contractId2 = ByteArray(32) { 0x01 }
        val contractId3 = ByteArray(32) { 0xFF.toByte() }

        val string1 = contractId1.toContractIdString()
        val string2 = contractId2.toContractIdString()
        val string3 = contractId3.toContractIdString()

        // Verify all different
        assertNotEquals(string1, string2, "Different contract IDs should produce different strings")
        assertNotEquals(string2, string3, "Different contract IDs should produce different strings")
        assertNotEquals(string1, string3, "Different contract IDs should produce different strings")
    }

    /**
     * Test toContractIdString consistency.
     *
     * Same input should always produce same output.
     */
    @Test
    fun testToContractIdString_Consistency() {
        val contractIdBytes = ByteArray(32) { (it * 3).toByte() }

        val string1 = contractIdBytes.toContractIdString()
        val string2 = contractIdBytes.toContractIdString()
        val string3 = contractIdBytes.toContractIdString()

        assertEquals(string1, string2, "Same input should produce same output")
        assertEquals(string2, string3, "Same input should produce same output")
    }

    /**
     * Test toHexString with maximum byte values.
     */
    @Test
    fun testToHexString_MaxByteValues() {
        val bytes = ByteArray(32) { 0xFF.toByte() }
        val hex = bytes.toHexString()

        assertEquals("ff".repeat(32), hex, "All 0xFF bytes should produce all 'ff' hex string")
    }

    /**
     * Test toHexString with minimum byte values.
     */
    @Test
    fun testToHexString_MinByteValues() {
        val bytes = ByteArray(32) { 0x00 }
        val hex = bytes.toHexString()

        assertEquals("00".repeat(32), hex, "All 0x00 bytes should produce all '00' hex string")
    }
}
