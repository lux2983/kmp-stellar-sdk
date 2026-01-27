//
//  OZSignerManager.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.oz
import com.soneso.stellar.sdk.smartaccount.core.*

import com.soneso.stellar.sdk.Address
import com.soneso.stellar.sdk.xdr.HostFunctionXdr
import com.soneso.stellar.sdk.xdr.InvokeContractArgsXdr
import com.soneso.stellar.sdk.xdr.SCAddressXdr
import com.soneso.stellar.sdk.xdr.SCSymbolXdr
import com.soneso.stellar.sdk.xdr.SCValXdr
import com.soneso.stellar.sdk.xdr.Uint32Xdr

/**
 * Manager for smart account signer operations.
 *
 * OZSignerManager provides high-level operations for managing signers on a smart account.
 * It handles adding and removing different types of signers (passkeys, delegated accounts,
 * Ed25519 keys) to context rules, with automatic validation and transaction building.
 *
 * Signer types supported:
 * - WebAuthn passkeys: secp256r1 signature verification via WebAuthn verifier contract
 * - Delegated signers: Stellar accounts or contracts using built-in require_auth verification
 * - Ed25519 signers: Traditional Ed25519 keys via Ed25519 verifier contract
 *
 * Each context rule can have up to 15 signers. Signers are identified by their on-chain
 * representation (address for delegated, verifier+key for external).
 *
 * Example usage:
 * ```kotlin
 * val kit = OZSmartAccountKit.create(config)
 * val signerManager = kit.signerManager
 *
 * // Add a passkey signer to the Default context rule
 * val result = signerManager.addPasskey(
 *     contextRuleId = 0u,
 *     publicKey = secp256r1PublicKey,
 *     credentialId = webAuthnCredentialId
 * )
 * println("Signer added: ${result.success}")
 *
 * // Add a delegated account signer
 * val delegatedResult = signerManager.addDelegated(
 *     contextRuleId = 0u,
 *     address = "GA7QYNF7..."
 * )
 * ```
 *
 * Thread Safety:
 * This class is thread-safe. All operations are async and can be safely called from any coroutine context.
 *
 * @property kit Reference to the parent OZSmartAccountKit instance
 */
class OZSignerManager internal constructor(
    private val kit: OZSmartAccountKit
) {
    // MARK: - Add Signers

    /**
     * Adds a WebAuthn passkey signer to a context rule.
     *
     * Creates an external signer with WebAuthn verification and adds it to the specified
     * context rule on the smart account contract. The public key must be an uncompressed
     * secp256r1 key (65 bytes starting with 0x04), and the credential ID must be non-empty.
     *
     * The transaction requires authorization from an existing signer on the specified
     * context rule. The user will be prompted for biometric authentication if the current
     * passkey is the authorizing signer.
     *
     * Contract call: `smart_account.add_signer(context_rule_id, signer)`
     *
     * @param contextRuleId The context rule ID to add the signer to (e.g., 0 for Default)
     * @param publicKey The uncompressed secp256r1 public key (65 bytes, starting with 0x04)
     * @param credentialId The WebAuthn credential identifier
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if validation fails or transaction fails
     *
     * Example:
     * ```kotlin
     * val result = signerManager.addPasskey(
     *     contextRuleId = 0u,
     *     publicKey = secp256r1PublicKey,
     *     credentialId = credentialIdData
     * )
     *
     * if (result.success) {
     *     println("Passkey signer added successfully")
     * } else {
     *     println("Failed to add signer: ${result.error ?: ""}")
     * }
     * ```
     */
    suspend fun addPasskey(
        contextRuleId: UInt,
        publicKey: ByteArray,
        credentialId: ByteArray
    ): TransactionResult {
        // Validate inputs
        kit.requireConnected()

        // Validate public key
        if (publicKey.size != SmartAccountConstants.SECP256R1_PUBLIC_KEY_SIZE) {
            throw ValidationException.invalidInput(
                "publicKey",
                "Public key must be ${SmartAccountConstants.SECP256R1_PUBLIC_KEY_SIZE} bytes, got: ${publicKey.size}"
            )
        }

        if (publicKey[0] != SmartAccountConstants.UNCOMPRESSED_PUBKEY_PREFIX) {
            throw ValidationException.invalidInput(
                "publicKey",
                "Public key must start with 0x04 (uncompressed format), got: 0x${publicKey[0].toString(16).padStart(2, '0')}"
            )
        }

        if (credentialId.isEmpty()) {
            throw ValidationException.invalidInput("credentialId", "Credential ID cannot be empty")
        }

        // Build WebAuthn external signer
        val signer = ExternalSigner.webAuthn(
            verifierAddress = kit.config.webauthnVerifierAddress,
            publicKey = publicKey,
            credentialId = credentialId
        )

        // Add signer via contract invocation
        return addSigner(contextRuleId = contextRuleId, signer = signer)
    }

    /**
     * Adds a delegated signer to a context rule.
     *
     * Creates a delegated signer that uses built-in Soroban require_auth verification
     * and adds it to the specified context rule. The address can be either a Stellar
     * account (G-address) or a smart contract (C-address).
     *
     * Delegated signers authorize transactions using the native Soroban authorization
     * mechanism, which calls `require_auth_for_args()` on the signer's address.
     *
     * The transaction requires authorization from an existing signer on the specified
     * context rule.
     *
     * Contract call: `smart_account.add_signer(context_rule_id, signer)`
     *
     * @param contextRuleId The context rule ID to add the signer to (e.g., 0 for Default)
     * @param address The Stellar address (G-address for accounts, C-address for contracts)
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if validation fails or transaction fails
     *
     * Example:
     * ```kotlin
     * // Add an account signer
     * val result = signerManager.addDelegated(
     *     contextRuleId = 0u,
     *     address = "GA7QYNF7SOWQ..."
     * )
     *
     * // Add a contract signer
     * val contractResult = signerManager.addDelegated(
     *     contextRuleId = 1u,
     *     address = "CBCD1234..."
     * )
     * ```
     */
    suspend fun addDelegated(
        contextRuleId: UInt,
        address: String
    ): TransactionResult {
        // Validate inputs
        kit.requireConnected()

        // Build delegated signer (validation happens in initializer)
        val signer = DelegatedSigner(address = address)

        // Add signer via contract invocation
        return addSigner(contextRuleId = contextRuleId, signer = signer)
    }

    /**
     * Adds an Ed25519 signer to a context rule.
     *
     * Creates an external signer with Ed25519 signature verification and adds it to the
     * specified context rule on the smart account contract. The public key must be a
     * 32-byte Ed25519 public key.
     *
     * Ed25519 signers use the traditional Stellar signing algorithm. The verifier contract
     * validates signatures against the provided public key.
     *
     * The transaction requires authorization from an existing signer on the specified
     * context rule.
     *
     * Contract call: `smart_account.add_signer(context_rule_id, signer)`
     *
     * @param contextRuleId The context rule ID to add the signer to (e.g., 0 for Default)
     * @param verifierAddress The Ed25519 verifier contract address (C-address)
     * @param publicKey The Ed25519 public key (32 bytes)
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if validation fails or transaction fails
     *
     * Example:
     * ```kotlin
     * val result = signerManager.addEd25519(
     *     contextRuleId = 0u,
     *     verifierAddress = "CDEF5678...",
     *     publicKey = ed25519PublicKey
     * )
     *
     * if (result.success) {
     *     println("Ed25519 signer added successfully")
     * }
     * ```
     */
    suspend fun addEd25519(
        contextRuleId: UInt,
        verifierAddress: String,
        publicKey: ByteArray
    ): TransactionResult {
        // Validate inputs
        kit.requireConnected()

        // Build Ed25519 external signer (validation happens in factory method)
        val signer = ExternalSigner.ed25519(
            verifierAddress = verifierAddress,
            publicKey = publicKey
        )

        // Add signer via contract invocation
        return addSigner(contextRuleId = contextRuleId, signer = signer)
    }

    // MARK: - Remove Signer

    /**
     * Removes a signer from a context rule.
     *
     * Removes the specified signer from the given context rule on the smart account contract.
     * The signer is identified by its on-chain representation (address for delegated signers,
     * verifier+key for external signers).
     *
     * The transaction requires authorization from an existing signer on the specified
     * context rule.
     *
     * IMPORTANT: You cannot remove the last signer from a context rule unless the rule
     * has policies that provide authorization. The contract will throw error 3004
     * if you attempt to remove the last signer with no policies configured.
     *
     * Contract call: `smart_account.remove_signer(context_rule_id, signer)`
     *
     * @param contextRuleId The context rule ID to remove the signer from
     * @param signer The signer to remove (must match an existing signer exactly)
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if validation fails or transaction fails
     *
     * Example:
     * ```kotlin
     * // Remove a delegated signer
     * val delegatedSigner = DelegatedSigner(address = "GA7QYNF7...")
     * val result = signerManager.removeSigner(
     *     contextRuleId = 0u,
     *     signer = delegatedSigner
     * )
     *
     * // Remove a passkey signer
     * val passkeySignerToRemove = ExternalSigner.webAuthn(
     *     verifierAddress = "CBCD1234...",
     *     publicKey = publicKey,
     *     credentialId = credentialId
     * )
     * val removeResult = signerManager.removeSigner(
     *     contextRuleId = 0u,
     *     signer = passkeySignerToRemove
     * )
     *
     * if (!result.success) {
     *     println("Failed to remove signer: ${result.error ?: ""}")
     * }
     * ```
     */
    suspend fun removeSigner(
        contextRuleId: UInt,
        signer: SmartAccountSigner
    ): TransactionResult {
        // Validate inputs
        val (_, contractId) = kit.requireConnected()

        // Build contract invocation for remove_signer
        val signerScVal = signer.toScVal()

        val functionArgs = listOf(
            SCValXdr.U32(Uint32Xdr(contextRuleId)),
            signerScVal
        )

        val invokeArgs = InvokeContractArgsXdr(
            contractAddress = Address(contractId).toSCAddress(),
            functionName = SCSymbolXdr("remove_signer"),
            args = functionArgs
        )

        val hostFunction = HostFunctionXdr.InvokeContract(invokeArgs)

        // Submit via transaction operations (handles simulation, signing, submission)
        return kit.transactionOperations.submit(hostFunction = hostFunction, auth = emptyList())
    }

    // MARK: - Private Helpers

    /**
     * Internal helper to add a signer to a context rule.
     *
     * Builds the contract invocation for add_signer and submits it via transaction operations.
     * The submit method handles simulation, authorization entry signing, and transaction submission.
     *
     * @param contextRuleId The context rule ID
     * @param signer The signer to add
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if the operation fails
     */
    private suspend fun addSigner(
        contextRuleId: UInt,
        signer: SmartAccountSigner
    ): TransactionResult {
        val (_, contractId) = kit.requireConnected()

        // Build contract invocation for add_signer
        val signerScVal = signer.toScVal()

        val functionArgs = listOf(
            SCValXdr.U32(Uint32Xdr(contextRuleId)),
            signerScVal
        )

        val invokeArgs = InvokeContractArgsXdr(
            contractAddress = Address(contractId).toSCAddress(),
            functionName = SCSymbolXdr("add_signer"),
            args = functionArgs
        )

        val hostFunction = HostFunctionXdr.InvokeContract(invokeArgs)

        // Submit via transaction operations (handles simulation, signing, submission)
        return kit.transactionOperations.submit(hostFunction = hostFunction, auth = emptyList())
    }
}
