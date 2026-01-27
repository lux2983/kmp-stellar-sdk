//
//  OZCredentialManager.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.oz
import com.soneso.stellar.sdk.smartaccount.core.*

/**
 * Manages the lifecycle of smart account credentials.
 *
 * OZCredentialManager provides operations for creating, querying, updating, and deleting
 * stored credentials. It handles credential deployment state transitions and ensures
 * data integrity through validation and error handling.
 *
 * Credential State Machine:
 * ```
 * pending --[deploy success]--> credential DELETED from storage
 * pending --[deploy failure]--> failed (deploymentError set)
 * ```
 *
 * After successful deployment, credentials are deleted from storage. Reconnection works
 * via sessions or the indexer. Failed deployments can be retried by deleting the
 * credential and creating a new one.
 *
 * Thread Safety:
 * All operations delegate to the StorageAdapter, which is responsible for thread-safety.
 *
 * Example usage:
 * ```kotlin
 * val manager = kit.credentialManager
 *
 * // Create a pending credential
 * val credential = manager.createPendingCredential(
 *     credentialId = "base64url-id",
 *     publicKey = secp256r1PublicKey,
 *     contractId = "CBCD1234..."
 * )
 *
 * // If deployment fails, mark it
 * manager.markDeploymentFailed(
 *     credentialId = credential.credentialId,
 *     error = "Transaction failed: insufficient balance"
 * )
 *
 * // On successful deployment, delete the credential
 * manager.deleteCredential(credentialId = credential.credentialId)
 * ```
 */
class OZCredentialManager internal constructor(
    private val kit: OZSmartAccountKit
) {
    /**
     * Storage adapter for credential persistence.
     */
    private val storage: StorageAdapter
        get() = kit.getStorage()

    // MARK: - Public API

    /**
     * Creates a new pending credential in storage.
     *
     * The credential is created with:
     * - deploymentStatus: PENDING
     * - isPrimary: true (first credential is the primary credential)
     * - createdAt: current timestamp
     *
     * Validation:
     * - Public key must be exactly 65 bytes (uncompressed secp256r1 format)
     * - Credential ID must not be empty
     * - Credential ID must be unique (no existing credential with same ID)
     *
     * @param credentialId The Base64URL-encoded credential ID (must be unique and non-empty)
     * @param publicKey The uncompressed secp256r1 public key (must be 65 bytes)
     * @param contractId The smart account contract address (C-address)
     * @return The newly created credential
     * @throws ValidationException.InvalidInput if validation fails
     * @throws CredentialException.AlreadyExists if a credential with the same ID exists
     * @throws StorageException.WriteFailed if saving fails
     *
     * Example:
     * ```kotlin
     * val credential = manager.createPendingCredential(
     *     credentialId = "abc123",
     *     publicKey = publicKeyData,
     *     contractId = "CBCD1234..."
     * )
     * println("Created credential: ${credential.credentialId}")
     * ```
     */
    suspend fun createPendingCredential(
        credentialId: String,
        publicKey: ByteArray,
        contractId: String
    ): StoredCredential {
        // Validate public key size
        if (publicKey.size != SmartAccountConstants.SECP256R1_PUBLIC_KEY_SIZE) {
            throw ValidationException.invalidInput(
                field = "publicKey",
                reason = "Expected ${SmartAccountConstants.SECP256R1_PUBLIC_KEY_SIZE} bytes, got ${publicKey.size}"
            )
        }

        // Validate credential ID is not empty
        if (credentialId.isEmpty()) {
            throw ValidationException.invalidInput(
                field = "credentialId",
                reason = "Credential ID cannot be empty"
            )
        }

        // Check for existing credential with same ID
        val existing = storage.get(credentialId)
        if (existing != null) {
            throw CredentialException.alreadyExists(credentialId)
        }

        // Create the credential
        val credential = StoredCredential(
            credentialId = credentialId,
            publicKey = publicKey,
            contractId = contractId,
            deploymentStatus = CredentialDeploymentStatus.PENDING,
            isPrimary = true,
            createdAt = System.currentTimeMillis()
        )

        // Save to storage
        try {
            storage.save(credential)
        } catch (e: CredentialException) {
            throw e
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.writeFailed(
                key = credentialId,
                cause = e
            )
        }

        return credential
    }

    /**
     * Marks a credential as failed deployment.
     *
     * Updates the credential's deployment status to FAILED and sets the deployment
     * error message. The credential can be retried by deleting it and creating a new one.
     *
     * @param credentialId The ID of the credential that failed deployment
     * @param error The error message describing why deployment failed
     * @throws CredentialException.NotFound if the credential does not exist
     * @throws StorageException.WriteFailed if the update fails
     *
     * Example:
     * ```kotlin
     * manager.markDeploymentFailed(
     *     credentialId = "abc123",
     *     error = "Transaction failed: insufficient balance"
     * )
     * ```
     */
    suspend fun markDeploymentFailed(
        credentialId: String,
        error: String
    ) {
        // Verify credential exists
        val existing = storage.get(credentialId)
            ?: throw CredentialException.notFound(credentialId)

        // Update deployment status
        val update = StoredCredentialUpdate(
            deploymentStatus = CredentialDeploymentStatus.FAILED,
            deploymentError = error
        )

        try {
            storage.update(credentialId, update)
        } catch (e: CredentialException) {
            throw e
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.writeFailed(
                key = credentialId,
                cause = e
            )
        }
    }

    /**
     * Deletes a credential from storage.
     *
     * Called after successful deployment. Credentials are not persisted after deployment
     * because reconnection works via sessions or the indexer.
     *
     * This method does not throw if the credential does not exist (deletion is idempotent).
     *
     * @param credentialId The ID of the credential to delete
     * @throws StorageException.WriteFailed if deletion fails
     *
     * Example:
     * ```kotlin
     * // After successful deployment
     * manager.deleteCredential(credentialId = "abc123")
     * ```
     */
    suspend fun deleteCredential(credentialId: String) {
        try {
            storage.delete(credentialId)
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.writeFailed(
                key = credentialId,
                cause = e
            )
        }

        // Emit credential deleted event
        kit.events.emit(SmartAccountEvent.CredentialDeleted(credentialId = credentialId))
    }

    /**
     * Retrieves a credential by its ID.
     *
     * @param credentialId The credential ID to look up
     * @return The stored credential, or null if not found
     * @throws StorageException.ReadFailed if reading fails
     *
     * Example:
     * ```kotlin
     * val credential = manager.getCredential(credentialId = "abc123")
     * if (credential != null) {
     *     println("Found credential for contract: ${credential.contractId ?: "unknown"}")
     * } else {
     *     println("Credential not found")
     * }
     * ```
     */
    suspend fun getCredential(credentialId: String): StoredCredential? {
        return try {
            storage.get(credentialId)
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.readFailed(
                key = credentialId,
                cause = e
            )
        }
    }

    /**
     * Retrieves all credentials associated with a specific contract.
     *
     * Returns credentials where the contractId matches the provided contract address.
     * Useful for finding all credentials (including failed deployments) for a wallet.
     *
     * @param contractId The contract address to filter by
     * @return List of credentials for this contract (empty if none found)
     * @throws StorageException.ReadFailed if reading fails
     *
     * Example:
     * ```kotlin
     * val credentials = manager.getCredentialsByContract(contractId = "CBCD1234...")
     * println("Found ${credentials.size} credential(s) for this contract")
     * ```
     */
    suspend fun getCredentialsByContract(contractId: String): List<StoredCredential> {
        return try {
            storage.getByContract(contractId)
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.readFailed(
                key = "contract:$contractId",
                cause = e
            )
        }
    }

    /**
     * Retrieves all stored credentials.
     *
     * Returns all credentials regardless of deployment status or contract address.
     * Useful for displaying all wallets or performing batch operations.
     *
     * @return List of all stored credentials (empty if none exist)
     * @throws StorageException.ReadFailed if reading fails
     *
     * Example:
     * ```kotlin
     * val allCredentials = manager.getAllCredentials()
     * println("Total credentials: ${allCredentials.size}")
     * ```
     */
    suspend fun getAllCredentials(): List<StoredCredential> {
        return try {
            storage.getAll()
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.readFailed(
                key = "all",
                cause = e
            )
        }
    }

    /**
     * Updates a credential with partial changes.
     *
     * Only non-null fields in the update are applied. The credential must exist
     * in storage before updating.
     *
     * @param credentialId The ID of the credential to update
     * @param updates The partial updates to apply
     * @throws CredentialException.NotFound if the credential does not exist
     * @throws StorageException.WriteFailed if the update fails
     *
     * Example:
     * ```kotlin
     * val update = StoredCredentialUpdate(
     *     nickname = "MacBook Pro",
     *     lastUsedAt = System.currentTimeMillis()
     * )
     * manager.updateCredential(credentialId = "abc123", updates = update)
     * ```
     */
    suspend fun updateCredential(credentialId: String, updates: StoredCredentialUpdate) {
        // Verify credential exists
        val existing = storage.get(credentialId)
            ?: throw CredentialException.notFound(credentialId)

        // Apply update
        try {
            storage.update(credentialId, updates)
        } catch (e: CredentialException) {
            throw e
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.writeFailed(
                key = credentialId,
                cause = e
            )
        }
    }

    /**
     * Updates the last used timestamp for a credential.
     *
     * @param credentialId The credential ID to update
     * @throws CredentialException.NotFound if the credential does not exist
     * @throws StorageException.WriteFailed if the update fails
     *
     * Example:
     * ```kotlin
     * manager.updateLastUsed(credentialId = "abc123")
     * ```
     */
    suspend fun updateLastUsed(credentialId: String) {
        val update = StoredCredentialUpdate(
            lastUsedAt = System.currentTimeMillis()
        )
        updateCredential(credentialId, update)
    }

    /**
     * Updates the nickname of a credential.
     *
     * @param credentialId The credential ID to update
     * @param nickname The new nickname (null to clear)
     * @throws CredentialException.NotFound if the credential does not exist
     * @throws StorageException.WriteFailed if the update fails
     *
     * Example:
     * ```kotlin
     * manager.updateNickname(credentialId = "abc123", nickname = "MacBook Pro Touch ID")
     * ```
     */
    suspend fun updateNickname(credentialId: String, nickname: String?) {
        val update = StoredCredentialUpdate(nickname = nickname)
        updateCredential(credentialId, update)
    }

    /**
     * Sets a credential as the primary credential.
     *
     * First unsets any existing primary credential for the same contract,
     * then sets this credential as primary.
     *
     * @param credentialId The credential ID to set as primary
     * @throws CredentialException.NotFound if the credential does not exist
     * @throws StorageException.WriteFailed if the update fails
     *
     * Example:
     * ```kotlin
     * manager.setPrimary(credentialId = "abc123")
     * ```
     */
    suspend fun setPrimary(credentialId: String) {
        // Verify credential exists
        val credential = storage.get(credentialId)
            ?: throw CredentialException.notFound(credentialId)

        // First, unset any existing primary credentials for the same contract
        val contractId = credential.contractId
        if (contractId != null) {
            val allCredentials = storage.getByContract(contractId)
            for (cred in allCredentials) {
                if (cred.isPrimary && cred.credentialId != credentialId) {
                    try {
                        storage.update(
                            cred.credentialId,
                            StoredCredentialUpdate(isPrimary = false)
                        )
                    } catch (e: Exception) {
                        // Continue even if unsetting fails
                    }
                }
            }
        } else {
            // No contract ID - unset all primary credentials
            val allCredentials = storage.getAll()
            for (cred in allCredentials) {
                if (cred.isPrimary && cred.credentialId != credentialId) {
                    try {
                        storage.update(
                            cred.credentialId,
                            StoredCredentialUpdate(isPrimary = false)
                        )
                    } catch (e: Exception) {
                        // Continue even if unsetting fails
                    }
                }
            }
        }

        // Set this credential as primary
        val update = StoredCredentialUpdate(isPrimary = true)
        try {
            storage.update(credentialId, update)
        } catch (e: CredentialException) {
            throw e
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.writeFailed(
                key = credentialId,
                cause = e
            )
        }
    }

    /**
     * Clears all credentials from storage.
     *
     * This operation is irreversible. Use with caution.
     *
     * @throws StorageException.WriteFailed if clearing fails
     *
     * Example:
     * ```kotlin
     * // Clear all credentials (e.g., on account deletion or reset)
     * manager.clearAll()
     * ```
     */
    suspend fun clearAll() {
        try {
            storage.clear()
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException.writeFailed(
                key = "all",
                cause = e
            )
        }
    }
}
