//
//  OZStorageAdapter.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.oz
import com.soneso.stellar.sdk.smartaccount.core.*

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// MARK: - Credential Deployment Status

/**
 * The deployment status of a smart account credential.
 */
enum class CredentialDeploymentStatus {
    /**
     * The credential has been created but the smart account contract has not been deployed yet.
     */
    PENDING,

    /**
     * The deployment transaction failed.
     */
    FAILED
    // Note: No SUCCESS status - credential is deleted from storage on successful deployment
}

// MARK: - Stored Credential

/**
 * A stored smart account credential with deployment and usage metadata.
 *
 * Represents a WebAuthn credential (passkey) associated with a smart account.
 * Tracks the credential's deployment status, contract address, and usage history.
 *
 * Example:
 * ```kotlin
 * val credential = StoredCredential(
 *     credentialId = "base64url-encoded-id",
 *     publicKey = secp256r1PublicKeyData,
 *     contractId = "CBCD1234...",
 *     deploymentStatus = CredentialDeploymentStatus.PENDING,
 *     isPrimary = true
 * )
 * ```
 */
data class StoredCredential(
    /**
     * The WebAuthn credential ID (Base64URL encoded).
     *
     * This is the unique identifier returned by the browser during WebAuthn registration.
     */
    val credentialId: String,

    /**
     * The uncompressed secp256r1 public key (65 bytes starting with 0x04).
     *
     * This public key is used for signature verification in the WebAuthn verifier contract.
     */
    val publicKey: ByteArray,

    /**
     * The smart account contract address (C-address).
     *
     * Set during wallet creation via deriveContractAddress. Null if the contract
     * address has not been derived yet.
     */
    val contractId: String? = null,

    /**
     * The current deployment status of the smart account contract.
     */
    val deploymentStatus: CredentialDeploymentStatus = CredentialDeploymentStatus.PENDING,

    /**
     * Error message if deployment failed.
     *
     * Contains details about why the deployment transaction failed.
     */
    val deploymentError: String? = null,

    /**
     * Timestamp of when this credential was created.
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp of when this credential was last used for signing.
     *
     * Updated after successful transaction signatures.
     */
    val lastUsedAt: Long? = null,

    /**
     * Optional user-friendly nickname for this credential.
     *
     * Example: "MacBook Pro Touch ID", "YubiKey 5"
     */
    val nickname: String? = null,

    /**
     * Whether this is the primary credential for this smart account.
     *
     * The primary credential is used as the default for signing operations.
     */
    val isPrimary: Boolean = false
) {
    /**
     * Custom equals implementation that properly compares ByteArray.
     *
     * Standard data class equals would not correctly compare the ByteArray field
     * by content, so this override ensures proper content-based comparison using
     * contentEquals().
     *
     * @param other The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as StoredCredential

        if (credentialId != other.credentialId) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (contractId != other.contractId) return false
        if (deploymentStatus != other.deploymentStatus) return false
        if (deploymentError != other.deploymentError) return false
        if (createdAt != other.createdAt) return false
        if (lastUsedAt != other.lastUsedAt) return false
        if (nickname != other.nickname) return false
        if (isPrimary != other.isPrimary) return false

        return true
    }

    /**
     * Custom hashCode implementation that properly handles ByteArray.
     *
     * Standard data class hashCode would not correctly hash the ByteArray field
     * by content, so this override ensures proper content-based hashing using
     * contentHashCode().
     *
     * @return Hash code for this stored credential
     */
    override fun hashCode(): Int {
        var result = credentialId.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + (contractId?.hashCode() ?: 0)
        result = 31 * result + deploymentStatus.hashCode()
        result = 31 * result + (deploymentError?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (lastUsedAt?.hashCode() ?: 0)
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + isPrimary.hashCode()
        return result
    }
}

// MARK: - Stored Session

/**
 * A stored user session for silent reconnection.
 *
 * Sessions enable users to reconnect to their smart account wallet without
 * re-authentication, as long as the session has not expired.
 *
 * Example:
 * ```kotlin
 * val session = StoredSession(
 *     credentialId = "base64url-encoded-id",
 *     contractId = "CBCD1234...",
 *     connectedAt = System.currentTimeMillis(),
 *     expiresAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days
 * )
 *
 * if (!session.isExpired) {
 *     // Silently reconnect
 * }
 * ```
 */
data class StoredSession(
    /**
     * The credential ID associated with this session.
     */
    val credentialId: String,

    /**
     * The smart account contract address.
     */
    val contractId: String,

    /**
     * When the session was established (milliseconds since epoch).
     */
    val connectedAt: Long,

    /**
     * When the session expires (milliseconds since epoch).
     */
    val expiresAt: Long
) {
    /**
     * Whether the session has expired.
     */
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt
}

// MARK: - Stored Credential Update

/**
 * Partial updates for a stored credential.
 *
 * Only non-null fields are applied during an update operation.
 *
 * Example:
 * ```kotlin
 * val update = StoredCredentialUpdate(
 *     deploymentStatus = CredentialDeploymentStatus.FAILED,
 *     deploymentError = "Transaction failed: insufficient balance"
 * )
 * storage.update(credentialId = "abc123", updates = update)
 * ```
 */
data class StoredCredentialUpdate(
    /**
     * New deployment status.
     */
    val deploymentStatus: CredentialDeploymentStatus? = null,

    /**
     * New deployment error message.
     */
    val deploymentError: String? = null,

    /**
     * New contract ID.
     */
    val contractId: String? = null,

    /**
     * New last used timestamp.
     */
    val lastUsedAt: Long? = null,

    /**
     * New nickname.
     */
    val nickname: String? = null,

    /**
     * New primary flag.
     */
    val isPrimary: Boolean? = null
)

// MARK: - Storage Adapter Interface

/**
 * Protocol for persisting smart account credentials and sessions.
 *
 * Storage adapters provide a pluggable persistence layer for credentials and sessions.
 * Implementations must be thread-safe and support concurrent access.
 *
 * The default implementation is InMemoryStorageAdapter, which stores data in memory
 * only. Platform-specific implementations can provide persistent storage.
 */
interface StorageAdapter {
    /**
     * Saves a credential to storage.
     *
     * @param credential The credential to save
     * @throws CredentialException.AlreadyExists if the credential already exists
     * @throws StorageException.WriteFailed if saving fails
     */
    suspend fun save(credential: StoredCredential)

    /**
     * Retrieves a credential by its ID.
     *
     * @param credentialId The credential ID
     * @return The credential, or null if not found
     * @throws StorageException.ReadFailed if reading fails
     */
    suspend fun get(credentialId: String): StoredCredential?

    /**
     * Retrieves all credentials associated with a contract address.
     *
     * @param contractId The contract address
     * @return List of credentials (empty if none found)
     * @throws StorageException.ReadFailed if reading fails
     */
    suspend fun getByContract(contractId: String): List<StoredCredential>

    /**
     * Retrieves all stored credentials.
     *
     * @return List of all credentials
     * @throws StorageException.ReadFailed if reading fails
     */
    suspend fun getAll(): List<StoredCredential>

    /**
     * Deletes a credential by its ID.
     *
     * @param credentialId The credential ID to delete
     * @throws StorageException.WriteFailed if deletion fails
     */
    suspend fun delete(credentialId: String)

    /**
     * Updates a credential with partial changes.
     *
     * Only non-null fields in the update are applied.
     *
     * @param credentialId The credential ID to update
     * @param updates The partial updates to apply
     * @throws CredentialException.NotFound if the credential is not found
     * @throws StorageException.WriteFailed if updating fails
     */
    suspend fun update(credentialId: String, updates: StoredCredentialUpdate)

    /**
     * Clears all credentials from storage.
     *
     * @throws StorageException.WriteFailed if clearing fails
     */
    suspend fun clear()

    /**
     * Saves a session to storage.
     *
     * @param session The session to save
     * @throws StorageException.WriteFailed if saving fails
     */
    suspend fun saveSession(session: StoredSession)

    /**
     * Retrieves the current session.
     *
     * @return The session, or null if no session exists or if the session is expired
     * @throws StorageException.ReadFailed if reading fails
     */
    suspend fun getSession(): StoredSession?

    /**
     * Clears the current session.
     *
     * @throws StorageException.WriteFailed if clearing fails
     */
    suspend fun clearSession()
}

// MARK: - In-Memory Storage Adapter

/**
 * In-memory storage adapter for credentials and sessions.
 *
 * This implementation stores all data in memory and does not persist across application
 * restarts. It is thread-safe using mutex protection.
 *
 * Use platform-specific implementations for persistent storage (e.g., SharedPreferences
 * on Android, UserDefaults on iOS, localStorage on Web).
 *
 * Example:
 * ```kotlin
 * val storage = InMemoryStorageAdapter()
 * val credential = StoredCredential(...)
 * storage.save(credential)
 * ```
 */
class InMemoryStorageAdapter : StorageAdapter {
    private val credentials = mutableMapOf<String, StoredCredential>()
    private var session: StoredSession? = null
    private val mutex = Mutex()

    override suspend fun save(credential: StoredCredential): Unit = mutex.withLock {
        if (credentials.containsKey(credential.credentialId)) {
            throw CredentialException.alreadyExists(credential.credentialId)
        }
        credentials[credential.credentialId] = credential
    }

    override suspend fun get(credentialId: String): StoredCredential? = mutex.withLock {
        credentials[credentialId]
    }

    override suspend fun getByContract(contractId: String): List<StoredCredential> = mutex.withLock {
        credentials.values.filter { it.contractId == contractId }
    }

    override suspend fun getAll(): List<StoredCredential> = mutex.withLock {
        credentials.values.toList()
    }

    override suspend fun delete(credentialId: String): Unit = mutex.withLock {
        credentials.remove(credentialId)
        Unit
    }

    override suspend fun update(credentialId: String, updates: StoredCredentialUpdate): Unit = mutex.withLock {
        val credential = credentials[credentialId]
            ?: throw CredentialException.notFound(credentialId)

        val updated = credential.copy(
            deploymentStatus = updates.deploymentStatus ?: credential.deploymentStatus,
            deploymentError = updates.deploymentError ?: credential.deploymentError,
            contractId = updates.contractId ?: credential.contractId,
            lastUsedAt = updates.lastUsedAt ?: credential.lastUsedAt,
            nickname = updates.nickname ?: credential.nickname,
            isPrimary = updates.isPrimary ?: credential.isPrimary
        )

        credentials[credentialId] = updated
    }

    override suspend fun clear(): Unit = mutex.withLock {
        credentials.clear()
    }

    override suspend fun saveSession(session: StoredSession): Unit = mutex.withLock {
        this.session = session
    }

    override suspend fun getSession(): StoredSession? = mutex.withLock {
        val currentSession = session
        if (currentSession != null && currentSession.isExpired) {
            // Clear expired session
            session = null
            return null
        }
        currentSession
    }

    override suspend fun clearSession(): Unit = mutex.withLock {
        session = null
    }
}

// MARK: - External Wallet Adapter Interface

/**
 * Protocol for integrating external wallet adapters for multi-signer support.
 *
 * External wallet adapters enable signing with external wallets like Freighter or Albedo
 * for multi-signature smart accounts. They handle wallet connection, signature collection,
 * and wallet reconnection.
 *
 * Example implementation:
 * ```kotlin
 * class FreighterAdapter : ExternalWalletAdapter {
 *     override suspend fun connect() {
 *         // Request wallet connection via Freighter browser extension
 *     }
 *
 *     override suspend fun signAuthEntry(preimageXdr: String): String {
 *         // Request signature from Freighter
 *         return "base64-encoded-signature"
 *     }
 * }
 * ```
 */
interface ExternalWalletAdapter {
    /**
     * Connects to the external wallet.
     *
     * Prompts the user to authorize the connection via the wallet's UI.
     *
     * @throws WalletException if connection fails or is rejected
     */
    suspend fun connect()

    /**
     * Disconnects from the external wallet.
     *
     * @throws WalletException if disconnection fails
     */
    suspend fun disconnect()

    /**
     * Signs an authorization entry preimage with the external wallet.
     *
     * @param preimageXdr The base64-encoded XDR of the auth entry preimage
     * @return The base64-encoded signature
     * @throws TransactionException.SigningFailed if signing fails or is rejected
     */
    suspend fun signAuthEntry(preimageXdr: String): String

    /**
     * Gets the connected wallet addresses.
     *
     * @return List of connected Stellar addresses (G-addresses)
     * @throws WalletException if retrieval fails
     */
    suspend fun getConnectedWallets(): List<String>

    /**
     * Checks if the wallet can sign for a specific address.
     *
     * @param address The Stellar address to check
     * @return True if the wallet can sign for this address
     * @throws WalletException if the check fails
     */
    suspend fun canSignFor(address: String): Boolean
}
