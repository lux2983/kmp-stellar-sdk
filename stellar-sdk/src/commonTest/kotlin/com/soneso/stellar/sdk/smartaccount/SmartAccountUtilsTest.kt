//
//  SmartAccountUtilsTest.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount

import com.soneso.stellar.sdk.smartaccount.core.*
import com.soneso.stellar.sdk.smartaccount.oz.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SmartAccountUtilsTest {

    // MARK: - Test Vectors

    /**
     * Test Vector 1: High-S to Low-S normalization
     *
     * Input: DER signature with s > halfOrder
     * Expected: Normalized signature with s' = n - s
     */
    @Test
    fun testNormalizeSignature_highSToLowS() {
        // DER encoded signature (71 bytes)
        // r = 0x0102030405060708091011121314151617181920212223242526272829303132
        // s = 0xffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632550 (high-S, exactly n - 1)
        val derSignature = hexToBytes(
            "3045022001020304050607080910111213141516171819202122232425262728293031320221" +
            "00ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632550"
        )

        val expected = hexToBytes(
            "0102030405060708091011121314151617181920212223242526272829303132" +
            "0000000000000000000000000000000000000000000000000000000000000001"
        )

        val result = SmartAccountUtils.normalizeSignature(derSignature)

        assertEquals(64, result.size, "Normalized signature should be 64 bytes")
        assertEquals(expected.toHex(), result.toHex(), "Signature normalization failed")
    }

    /**
     * Test Vector 1b: Already low-S (no normalization needed)
     *
     * Input: DER signature with s < halfOrder
     * Expected: Same signature in compact format
     */
    @Test
    fun testNormalizeSignature_alreadyLowS() {
        // DER encoded signature (70 bytes)
        // r = 0x0102030405060708091011121314151617181920212223242526272829303132
        // s = 0x0000000000000000000000000000000000000000000000000000000000000005 (low-S)
        val derSignature = hexToBytes(
            "30440220010203040506070809101112131415161718192021222324252627282930313202" +
            "200000000000000000000000000000000000000000000000000000000000000005"
        )

        val expected = hexToBytes(
            "0102030405060708091011121314151617181920212223242526272829303132" +
            "0000000000000000000000000000000000000000000000000000000000000005"
        )

        val result = SmartAccountUtils.normalizeSignature(derSignature)

        assertEquals(64, result.size, "Normalized signature should be 64 bytes")
        assertEquals(expected.toHex(), result.toHex(), "Signature should remain unchanged")
    }

    /**
     * Test Vector 2: DER with leading zeros (33-byte r and s)
     */
    @Test
    fun testNormalizeSignature_withLeadingZeros() {
        // DER with 33-byte r and s (leading 0x00 for positive representation)
        // r = 0x00ff02030405060708091011121314151617181920212223242526272829303132
        // s = 0x0000000000000000000000000000000000000000000000000000000000000010
        val derSignature = hexToBytes(
            "3046022100ff02030405060708091011121314151617181920212223242526272829303132022100" +
            "0000000000000000000000000000000000000000000000000000000000000010"
        )

        val expected = hexToBytes(
            "ff02030405060708091011121314151617181920212223242526272829303132" +
            "0000000000000000000000000000000000000000000000000000000000000010"
        )

        val result = SmartAccountUtils.normalizeSignature(derSignature)

        assertEquals(64, result.size, "Normalized signature should be 64 bytes")
        assertEquals(expected.toHex(), result.toHex(), "Leading zeros should be stripped")
    }

    /**
     * Test Vector 3: Short r and s (less than 32 bytes)
     */
    @Test
    fun testNormalizeSignature_shortComponents() {
        // DER with short r (4 bytes) and s (5 bytes)
        // r = 0x01020304
        // s = 0x0506070809
        val derSignature = hexToBytes(
            "300d02040102030402050506070809"
        )

        val expected = hexToBytes(
            "0000000000000000000000000000000000000000000000000000000001020304" +
            "0000000000000000000000000000000000000000000000000000000506070809"
        )

        val result = SmartAccountUtils.normalizeSignature(derSignature)

        assertEquals(64, result.size, "Normalized signature should be 64 bytes")
        assertEquals(expected.toHex(), result.toHex(), "Short components should be left-padded")
    }

    /**
     * Test Vector 4: Signature with s at exactly half order
     *
     * This tests the boundary: s = halfOrder should remain unchanged
     */
    @Test
    fun testNormalizeSignature_exactHalfOrder() {
        // s = halfOrder = 0x7fffffff800000007fffffffffffffffde737d56d38bcf4279dce5617e3192a8
        // This needs 0x00 prefix in DER because high bit is set
        val derSignature = hexToBytes(
            "3046022001020304050607080910111213141516171819202122232425262728293031320221" +
            "007fffffff800000007fffffffffffffffde737d56d38bcf4279dce5617e3192a8"
        )

        val expected = hexToBytes(
            "0102030405060708091011121314151617181920212223242526272829303132" +
            "7fffffff800000007fffffffffffffffde737d56d38bcf4279dce5617e3192a8"
        )

        val result = SmartAccountUtils.normalizeSignature(derSignature)

        assertEquals(64, result.size, "Normalized signature should be 64 bytes")
        assertEquals(expected.toHex(), result.toHex(), "Half order should not be normalized")
    }

    /**
     * Test Vector 5: Half order + 1 (should be normalized)
     *
     * s = halfOrder + 1 should be normalized to n - (halfOrder + 1)
     * Since n is odd: n = 2*halfOrder + 1, so n - (halfOrder + 1) = halfOrder
     */
    @Test
    fun testNormalizeSignature_halfOrderPlusOne() {
        // s = halfOrder + 1 = 0x7fffffff800000007fffffffffffffffde737d56d38bcf4279dce5617e3192a9
        // n = 0xffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551 (odd)
        // Normalized: n - s = halfOrder = 0x7fffffff800000007fffffffffffffffde737d56d38bcf4279dce5617e3192a8
        // DER needs 0x00 prefix because high bit is set
        val derSignature = hexToBytes(
            "3046022001020304050607080910111213141516171819202122232425262728293031320221" +
            "007fffffff800000007fffffffffffffffde737d56d38bcf4279dce5617e3192a9"
        )

        val expected = hexToBytes(
            "0102030405060708091011121314151617181920212223242526272829303132" +
            "7fffffff800000007fffffffffffffffde737d56d38bcf4279dce5617e3192a8"
        )

        val result = SmartAccountUtils.normalizeSignature(derSignature)

        assertEquals(64, result.size, "Normalized signature should be 64 bytes")
        assertEquals(expected.toHex(), result.toHex(), "s > halfOrder should be normalized")
    }

    // MARK: - Error Cases

    /**
     * Test invalid DER format: wrong header
     */
    @Test
    fun testNormalizeSignature_invalidHeader() {
        val invalidDer = hexToBytes("31450220010203040506070809101112131415161718192021222324252627282930313202200000000000000000000000000000000000000000000000000000000000000005")

        assertFailsWith<ValidationException.InvalidInput> {
            SmartAccountUtils.normalizeSignature(invalidDer)
        }
    }

    /**
     * Test invalid DER format: missing r marker
     */
    @Test
    fun testNormalizeSignature_missingRMarker() {
        val invalidDer = hexToBytes("30450320010203040506070809101112131415161718192021222324252627282930313202200000000000000000000000000000000000000000000000000000000000000005")

        assertFailsWith<ValidationException.InvalidInput> {
            SmartAccountUtils.normalizeSignature(invalidDer)
        }
    }

    /**
     * Test invalid DER format: missing s marker
     */
    @Test
    fun testNormalizeSignature_missingSMarker() {
        val invalidDer = hexToBytes("30450220010203040506070809101112131415161718192021222324252627282930313203200000000000000000000000000000000000000000000000000000000000000005")

        assertFailsWith<ValidationException.InvalidInput> {
            SmartAccountUtils.normalizeSignature(invalidDer)
        }
    }

    /**
     * Test invalid DER format: truncated signature
     */
    @Test
    fun testNormalizeSignature_truncated() {
        val truncatedDer = hexToBytes("3045022001020304050607080910111213141516")

        assertFailsWith<ValidationException.InvalidInput> {
            SmartAccountUtils.normalizeSignature(truncatedDer)
        }
    }

    /**
     * Test invalid DER format: too short
     */
    @Test
    fun testNormalizeSignature_tooShort() {
        val tooShort = hexToBytes("300102")

        assertFailsWith<ValidationException.InvalidInput> {
            SmartAccountUtils.normalizeSignature(tooShort)
        }
    }

    // MARK: - Helper Functions

    /**
     * Converts hex string to byte array.
     */
    private fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace(" ", "").replace("\n", "")
        require(cleanHex.length % 2 == 0) { "Hex string must have even length" }
        return ByteArray(cleanHex.length / 2) { i ->
            cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    /**
     * Converts byte array to hex string.
     */
    private fun ByteArray.toHex(): String = joinToString("") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}
