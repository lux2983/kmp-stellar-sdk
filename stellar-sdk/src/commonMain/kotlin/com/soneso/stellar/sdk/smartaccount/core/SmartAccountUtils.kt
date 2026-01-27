//
//  SmartAccountUtils.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.core

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.soneso.stellar.sdk.KeyPair
import com.soneso.stellar.sdk.StrKey
import com.soneso.stellar.sdk.crypto.getSha256Crypto
import com.soneso.stellar.sdk.xdr.ContractIDPreimageFromAddressXdr
import com.soneso.stellar.sdk.xdr.ContractIDPreimageXdr
import com.soneso.stellar.sdk.xdr.HashIDPreimageContractIDXdr
import com.soneso.stellar.sdk.xdr.HashIDPreimageXdr
import com.soneso.stellar.sdk.xdr.HashXdr
import com.soneso.stellar.sdk.xdr.SCAddressXdr
import com.soneso.stellar.sdk.xdr.Uint256Xdr
import com.soneso.stellar.sdk.xdr.XdrWriter

/**
 * Utility functions for Smart Account operations.
 *
 * Provides cryptographic utilities for WebAuthn signature processing,
 * public key extraction, and contract address derivation.
 */
object SmartAccountUtils {

    // MARK: - Signature Normalization

    /**
     * Normalizes a DER-encoded secp256r1 signature to compact format with low-S normalization.
     *
     * This function performs the following steps:
     * 1. Parses DER format: `0x30 [total_len] 0x02 [r_len] [r_bytes] 0x02 [s_len] [s_bytes]`
     * 2. Extracts r and s components (stripping leading 0x00 padding if present)
     * 3. Converts s to BigInteger and normalizes to low-S form if needed
     * 4. Pads both r and s to exactly 32 bytes
     * 5. Returns concatenated r || s (64 bytes total)
     *
     * Low-S normalization ensures that s values greater than half the curve order
     * are converted to their complements (n - s), which is required for Stellar/Soroban
     * signature verification.
     *
     * @param derSignature DER-encoded signature bytes
     * @return Compact 64-byte signature (32-byte r || 32-byte s)
     * @throws ValidationException.InvalidInput if the DER format is invalid
     *
     * Example:
     * ```kotlin
     * val derSig = byteArrayOf(...)  // DER-encoded signature from WebAuthn
     * val compactSig = SmartAccountUtils.normalizeSignature(derSig)
     * // compactSig is now 64 bytes: r (32 bytes) || s (32 bytes)
     * ```
     */
    fun normalizeSignature(derSignature: ByteArray): ByteArray {
        // Validate DER signature header
        if (derSignature.size < 8 || derSignature[0] != 0x30.toByte()) {
            throw ValidationException.invalidInput(
                "derSignature",
                "Invalid DER signature format"
            )
        }

        // Parse r component
        var offset = 2
        if (offset + 1 >= derSignature.size || derSignature[offset] != 0x02.toByte()) {
            throw ValidationException.invalidInput(
                "derSignature",
                "Invalid DER signature format: missing r component marker"
            )
        }

        val rLength = derSignature[offset + 1].toInt() and 0xFF
        if (offset + 2 + rLength > derSignature.size) {
            throw ValidationException.invalidInput(
                "derSignature",
                "Invalid DER signature format: truncated r component"
            )
        }

        var r = derSignature.copyOfRange(offset + 2, offset + 2 + rLength)

        // Strip leading 0x00 padding from r if present
        while (r.size > 1 && r[0] == 0x00.toByte()) {
            r = r.copyOfRange(1, r.size)
        }

        // Parse s component
        offset = offset + 2 + rLength
        if (offset + 1 >= derSignature.size || derSignature[offset] != 0x02.toByte()) {
            throw ValidationException.invalidInput(
                "derSignature",
                "Invalid DER signature format: missing s component marker"
            )
        }

        val sLength = derSignature[offset + 1].toInt() and 0xFF
        if (offset + 2 + sLength > derSignature.size) {
            throw ValidationException.invalidInput(
                "derSignature",
                "Invalid DER signature format: truncated s component"
            )
        }

        var s = derSignature.copyOfRange(offset + 2, offset + 2 + sLength)

        // Strip leading 0x00 padding from s if present
        while (s.size > 1 && s[0] == 0x00.toByte()) {
            s = s.copyOfRange(1, s.size)
        }

        // Convert r and s to BigInteger for low-S normalization
        val rBigInt = bytesToUnsignedBigInteger(r)
        var sBigInt = bytesToUnsignedBigInteger(s)

        // Normalize s to low-S form
        // secp256r1 curve order: 0xffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551
        // Reference: https://github.com/stellar/stellar-protocol/discussions/1435#discussioncomment-8809175
        val curveOrder = BigInteger.parseString(
            "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551",
            16
        )

        val halfCurveOrder = curveOrder / BigInteger.TWO

        // If s > halfOrder, normalize: s = n - s
        if (sBigInt > halfCurveOrder) {
            sBigInt = curveOrder - sBigInt
        }

        // Convert back to byte arrays and pad to 32 bytes
        val rPadded = bigIntegerToUnsignedBytes(rBigInt, 32)
        val sPadded = bigIntegerToUnsignedBytes(sBigInt, 32)

        // Concatenate r || s (64 bytes total)
        return rPadded + sPadded
    }

    // MARK: - Public Key Extraction

    /**
     * Extracts the secp256r1 public key from WebAuthn attestation data.
     *
     * Searches for the COSE key structure in raw attestation data and extracts
     * the X and Y coordinates of the public key. The result is formatted as an
     * uncompressed public key with the 0x04 prefix.
     *
     * COSE key structure:
     * ```
     * Prefix: [0xa5, 0x01, 0x02, 0x03, 0x26, 0x20, 0x01, 0x21, 0x58, 0x20]
     * X coordinate: next 32 bytes
     * Skip: 3 bytes (0x22, 0x58, 0x20)
     * Y coordinate: next 32 bytes
     * Result: 0x04 || X (32 bytes) || Y (32 bytes) = 65 bytes total
     * ```
     *
     * @param attestationObject Raw attestation object data from WebAuthn registration
     * @return Uncompressed secp256r1 public key (65 bytes: 0x04 prefix + X + Y)
     * @throws ValidationException.InvalidInput if the COSE key structure is not found
     *         or if there is insufficient data after the prefix
     *
     * Example:
     * ```kotlin
     * val attestationData = ... // from WebAuthn registration
     * val publicKey = SmartAccountUtils.extractPublicKey(attestationData)
     * println("Public key: ${publicKey.toHexString()}")
     * ```
     */
    fun extractPublicKey(attestationObject: ByteArray): ByteArray {
        // COSE key prefix for secp256r1 public keys in WebAuthn attestation
        val prefix = byteArrayOf(
            0xa5.toByte(), 0x01, 0x02, 0x03, 0x26.toByte(), 0x20.toByte(),
            0x01, 0x21, 0x58, 0x20.toByte()
        )

        // Search for prefix in attestation object
        val prefixIndex = findSubarray(attestationObject, prefix)
        if (prefixIndex < 0) {
            throw ValidationException.invalidInput(
                "attestationObject",
                "COSE key prefix not found in attestation"
            )
        }

        val xStart = prefixIndex + prefix.size

        // Ensure we have enough data for X (32 bytes) + separator (3 bytes) + Y (32 bytes)
        val requiredLength = xStart + 32 + 3 + 32
        if (attestationObject.size < requiredLength) {
            throw ValidationException.invalidInput(
                "attestationObject",
                "Insufficient data after COSE key prefix"
            )
        }

        // Extract X coordinate (32 bytes after prefix)
        val x = attestationObject.copyOfRange(xStart, xStart + 32)

        // Skip 3 bytes (0x22, 0x58, 0x20) and extract Y coordinate (32 bytes)
        val yStart = xStart + 32 + 3
        val y = attestationObject.copyOfRange(yStart, yStart + 32)

        // Construct uncompressed public key: 0x04 || X || Y
        val publicKey = ByteArray(65)
        publicKey[0] = SmartAccountConstants.UNCOMPRESSED_PUBKEY_PREFIX
        x.copyInto(publicKey, 1)
        y.copyInto(publicKey, 33)

        return publicKey
    }

    // MARK: - Contract Salt

    /**
     * Computes the contract salt from a WebAuthn credential ID.
     *
     * The salt is used as part of the contract address derivation process to ensure
     * each credential ID results in a unique contract address. This is computed as
     * the SHA-256 hash of the credential ID.
     *
     * @param credentialId WebAuthn credential ID
     * @return SHA-256 hash of the credential ID (32 bytes)
     *
     * Example:
     * ```kotlin
     * val credentialId = ... // from WebAuthn registration
     * val salt = SmartAccountUtils.getContractSalt(credentialId)
     * ```
     */
    suspend fun getContractSalt(credentialId: ByteArray): ByteArray {
        return getSha256Crypto().hash(credentialId)
    }

    // MARK: - Contract Address Derivation

    /**
     * Derives the smart account contract address from a credential ID and deployer.
     *
     * Computes the deterministic contract address that will be created when deploying
     * a smart account contract with the given credential ID from the specified deployer
     * account on the specified network.
     *
     * Algorithm:
     * ```
     * salt = SHA256(credentialId)
     * deployerAddress = SCAddress::Account(deployerPublicKey)
     * networkId = SHA256(networkPassphrase as UTF-8)
     *
     * preimage = HashIdPreimage::ContractId {
     *   networkId: networkId,
     *   contractIdPreimage: ContractIdPreimage::FromAddress {
     *     address: deployerAddress,
     *     salt: Uint256(salt)
     *   }
     * }
     *
     * contractIdBytes = SHA256(XDR_encode(preimage))
     * contractId = StrKey.encodeContract(contractIdBytes)
     * ```
     *
     * @param credentialId WebAuthn credential ID used to generate the salt
     * @param deployerPublicKey Stellar account ID (G-address) of the deployer
     * @param networkPassphrase Network passphrase (e.g., "Test SDF Network ; September 2015")
     * @return Contract address as a C-address (StrKey encoded)
     * @throws ValidationException.InvalidAddress if the deployer public key is invalid
     * @throws TransactionException.SigningFailed if XDR encoding fails
     *
     * Example:
     * ```kotlin
     * val credentialId = ... // from WebAuthn registration
     * val deployerKey = "GBXYZ..." // deployer G-address
     * val networkPassphrase = "Test SDF Network ; September 2015"
     * val contractAddress = SmartAccountUtils.deriveContractAddress(
     *     credentialId,
     *     deployerKey,
     *     networkPassphrase
     * )
     * println("Contract will be deployed at: $contractAddress")
     * ```
     */
    suspend fun deriveContractAddress(
        credentialId: ByteArray,
        deployerPublicKey: String,
        networkPassphrase: String
    ): String {
        // Step 1: Compute contract salt from credential ID
        val contractSalt = getContractSalt(credentialId)

        // Step 2: Create deployer SCAddress from public key
        val deployerAddress: SCAddressXdr = try {
            val keyPair = KeyPair.fromAccountId(deployerPublicKey)
            SCAddressXdr.AccountId(keyPair.getXdrAccountId())
        } catch (e: Exception) {
            throw ValidationException.invalidAddress(
                deployerPublicKey,
                e
            )
        }

        // Step 3: Compute network ID (SHA-256 of network passphrase)
        val networkIdBytes = getSha256Crypto().hash(networkPassphrase.encodeToByteArray())
        val networkId = HashXdr(networkIdBytes)

        // Step 4: Construct ContractIDPreimage::FromAddress
        val contractIdPreimage = ContractIDPreimageXdr.FromAddress(
            ContractIDPreimageFromAddressXdr(
                address = deployerAddress,
                salt = Uint256Xdr(contractSalt)
            )
        )

        // Step 5: Construct HashIDPreimage::ContractID
        val hashIdPreimageContractId = HashIDPreimageContractIDXdr(
            networkId = networkId,
            contractIdPreimage = contractIdPreimage
        )

        val preimage = HashIDPreimageXdr.ContractID(hashIdPreimageContractId)

        // Step 6: XDR encode the preimage
        val encodedPreimage: ByteArray = try {
            val writer = XdrWriter()
            preimage.encode(writer)
            writer.toByteArray()
        } catch (e: Exception) {
            throw TransactionException.signingFailed(
                "Failed to XDR encode contract ID preimage",
                e
            )
        }

        // Step 7: Hash the encoded preimage
        val contractIdBytes = getSha256Crypto().hash(encodedPreimage)

        // Step 8: Encode as StrKey contract ID (C-address)
        return try {
            StrKey.encodeContract(contractIdBytes)
        } catch (e: Exception) {
            throw TransactionException.signingFailed(
                "Failed to encode contract ID",
                e
            )
        }
    }

    // MARK: - Private Helper Functions

    /**
     * Converts an unsigned byte array to BigInteger.
     *
     * This function interprets the byte array as an unsigned big-endian integer.
     * Leading zeros are significant for proper interpretation.
     *
     * @param bytes Unsigned byte array (big-endian)
     * @return BigInteger representation
     */
    private fun bytesToUnsignedBigInteger(bytes: ByteArray): BigInteger {
        // Convert bytes to hex string
        val hex = bytes.joinToString("") { byte ->
            (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
        }
        return BigInteger.parseString(hex, 16)
    }

    /**
     * Converts a BigInteger to unsigned byte array with padding.
     *
     * @param value BigInteger value (must be non-negative)
     * @param byteCount Target byte count (left-padded with zeros if needed)
     * @return Unsigned byte array of exact length byteCount
     */
    private fun bigIntegerToUnsignedBytes(value: BigInteger, byteCount: Int): ByteArray {
        require(value >= BigInteger.ZERO) {
            "Cannot convert negative BigInteger to unsigned bytes"
        }

        // Convert to hex string
        val hex = value.toString(16)

        // Ensure even number of hex digits
        val paddedHex = if (hex.length % 2 != 0) "0$hex" else hex

        // Convert hex string to byte array
        val bytes = ByteArray(paddedHex.length / 2) { i ->
            val index = i * 2
            paddedHex.substring(index, index + 2).toInt(16).toByte()
        }

        // Pad or trim to exact size
        return when {
            bytes.size == byteCount -> bytes
            bytes.size < byteCount -> {
                // Left-pad with zeros
                val padded = ByteArray(byteCount)
                bytes.copyInto(padded, byteCount - bytes.size)
                padded
            }
            else -> {
                // Trim from left (keep rightmost bytes)
                bytes.copyOfRange(bytes.size - byteCount, bytes.size)
            }
        }
    }

    /**
     * Finds the first occurrence of a subarray within a larger array.
     *
     * Uses a simple but efficient sliding window algorithm to search for
     * the subarray. Returns -1 if not found.
     *
     * @param array The array to search in
     * @param subarray The subarray to search for
     * @return Index of first occurrence, or -1 if not found
     */
    private fun findSubarray(array: ByteArray, subarray: ByteArray): Int {
        if (subarray.isEmpty() || array.size < subarray.size) {
            return -1
        }

        // Search for the subarray
        for (i in 0..(array.size - subarray.size)) {
            var found = true
            for (j in subarray.indices) {
                if (array[i + j] != subarray[j]) {
                    found = false
                    break
                }
            }
            if (found) {
                return i
            }
        }

        return -1
    }
}
