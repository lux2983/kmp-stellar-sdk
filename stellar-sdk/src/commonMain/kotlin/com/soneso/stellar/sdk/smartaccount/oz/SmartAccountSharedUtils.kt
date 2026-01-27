//
//  SmartAccountSharedUtils.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.oz

import com.soneso.stellar.sdk.smartaccount.core.*
import com.soneso.stellar.sdk.InvokeHostFunctionOperation
import com.soneso.stellar.sdk.Memo
import com.soneso.stellar.sdk.MemoNone
import com.soneso.stellar.sdk.Network
import com.soneso.stellar.sdk.TransactionBuilder
import com.soneso.stellar.sdk.xdr.HostFunctionXdr
import com.soneso.stellar.sdk.xdr.Int64Xdr
import com.soneso.stellar.sdk.xdr.Int128PartsXdr
import com.soneso.stellar.sdk.xdr.SCAddressXdr
import com.soneso.stellar.sdk.xdr.SCValXdr
import com.soneso.stellar.sdk.xdr.Uint64Xdr
import io.ktor.utils.io.core.toByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Shared utility functions for Smart Account operations.
 *
 * Provides reusable helpers used across multiple Smart Account components:
 * - Transaction simulation and result extraction
 * - Amount conversion (XLM to stroops)
 * - Stroops to I128 ScVal conversion
 * - Base64URL encoding/decoding
 * - Address string extraction from SCAddressXDR
 *
 * These utilities are extracted to eliminate duplication across
 * OZContextRuleManager, OZMultiSignerManager, OZTransactionOperations,
 * and OZWalletOperations.
 */
object SmartAccountSharedUtils {

    // MARK: - Transaction Simulation

    /**
     * Simulates a host function and extracts the return value.
     *
     * Performs the following steps:
     * 1. Fetches the deployer account
     * 2. Builds a transaction with the host function
     * 3. Simulates the transaction
     * 4. Extracts and returns the result value from simulation
     *
     * Used for query operations that don't require transaction submission.
     *
     * @param hostFunction The host function to simulate
     * @param kit The OZSmartAccountKit instance providing deployer and server access
     * @return The SCVal return value from the simulation
     * @throws SmartAccountException if simulation fails or result extraction fails
     */
    suspend fun simulateAndExtractResult(
        hostFunction: HostFunctionXdr,
        kit: OZSmartAccountKit
    ): SCValXdr {
        // Get deployer account
        val deployer = kit.getDeployer()
        val deployerAccount = kit.sorobanServer.getAccount(deployer.getAccountId())

        // Build operation
        val operation = InvokeHostFunctionOperation(hostFunction, emptyList())

        // Build transaction for simulation
        val transaction = TransactionBuilder(deployerAccount, Network(kit.config.networkPassphrase))
            .setBaseFee(100)
            .addOperation(operation)
            .addMemo(MemoNone)
            .setTimeout(300)
            .build()

        // Simulate transaction
        val simulation = kit.sorobanServer.simulateTransaction(transaction)

        // Check for simulation errors
        if (simulation.error != null) {
            throw TransactionException.simulationFailed("Simulation error: ${simulation.error}")
        }

        // Extract result
        val results = simulation.results
        if (results.isNullOrEmpty()) {
            throw TransactionException.simulationFailed("No results returned from simulation")
        }

        return results[0].parseXdr()
            ?: throw TransactionException.simulationFailed("No return value in simulation result")
    }

    // MARK: - Amount Conversion

    /**
     * Converts an XLM amount to stroops.
     *
     * Uses Double precision for arithmetic with proper rounding.
     * Validates that the resulting stroops value is positive and within Long range.
     *
     * @param amount The amount in XLM (must be positive)
     * @return The amount in stroops (1 XLM = 10,000,000 stroops)
     * @throws ValidationException.InvalidInput if conversion would overflow or result is invalid
     */
    fun amountToStroops(amount: Double): Long {
        val stroopsDouble = amount * SmartAccountConstants.STROOPS_PER_XLM

        // Round to nearest integer
        val stroops = stroopsDouble.toLong()

        // Validate range
        if (stroops <= 0 || stroops > Long.MAX_VALUE) {
            throw ValidationException.invalidInput(
                "amount",
                "Amount out of valid range, got: $amount"
            )
        }

        return stroops
    }

    /**
     * Converts stroops (Long) to I128 ScVal.
     *
     * For positive values within Long range, the high part is 0 and the low part
     * contains the value as ULong.
     *
     * @param stroops The amount in stroops
     * @return ScVal::I128 representation
     */
    fun stroopsToI128ScVal(stroops: Long): SCValXdr {
        val i128Parts = Int128PartsXdr(hi = Int64Xdr(0L), lo = Uint64Xdr(stroops.toULong()))
        return SCValXdr.I128(i128Parts)
    }

    // MARK: - Base64URL Encoding/Decoding

    /**
     * Encodes data to Base64URL format (RFC 4648, no padding).
     *
     * Converts standard Base64 characters to URL-safe equivalents:
     * - `+` becomes `-`
     * - `/` becomes `_`
     * - Padding `=` characters are removed
     *
     * @param data The data to encode
     * @return Base64URL-encoded string without padding
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun base64urlEncode(data: ByteArray): String {
        return Base64.encode(data)
            .replace("+", "-")
            .replace("/", "_")
            .replace("=", "")
    }

    /**
     * Decodes a Base64URL-encoded string to data.
     *
     * Converts URL-safe characters back to standard Base64:
     * - `-` becomes `+`
     * - `_` becomes `/`
     * - Adds padding `=` characters as needed
     *
     * @param string The Base64URL-encoded string
     * @return Decoded data, or null if decoding fails
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun base64urlDecode(string: String): ByteArray? {
        var base64 = string
            .replace("-", "+")
            .replace("_", "/")

        // Add padding
        while (base64.length % 4 != 0) {
            base64 += "="
        }

        return try {
            Base64.decode(base64)
        } catch (e: Exception) {
            null
        }
    }

    // MARK: - Address Extraction

    /**
     * Extracts a string address from an SCAddressXDR.
     *
     * Returns the G-address for account types or the C-address for contract types.
     *
     * @param address The SCAddressXDR to extract from
     * @return The string address, or null if extraction fails
     */
    fun extractAddressString(address: SCAddressXdr): String? {
        return when (address) {
            is SCAddressXdr.AccountId -> {
                // Account address: G-address
                try {
                    val publicKey = address.value.value
                    when (publicKey) {
                        is com.soneso.stellar.sdk.xdr.PublicKeyXdr.Ed25519 -> {
                            com.soneso.stellar.sdk.StrKey.encodeEd25519PublicKey(publicKey.value.value)
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }
            is SCAddressXdr.ContractId -> {
                // Contract address: C-address
                try {
                    com.soneso.stellar.sdk.StrKey.encodeContract(address.value.value.value)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }
}

/**
 * Encodes a contract ID hash to a C-address string.
 *
 * @receiver The contract ID hash (32 bytes)
 * @return The C-address string
 */
private fun ByteArray.encodeContractId(): String {
    return com.soneso.stellar.sdk.StrKey.encodeContract(this)
}
