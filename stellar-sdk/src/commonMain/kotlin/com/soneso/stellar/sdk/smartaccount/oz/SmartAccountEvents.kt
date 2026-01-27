//
//  SmartAccountEvents.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.oz
import com.soneso.stellar.sdk.smartaccount.core.*

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Events emitted by the Smart Account Kit during wallet lifecycle operations.
 *
 * These events provide hooks for monitoring and responding to key operations:
 * - Wallet connection and creation
 * - Credential lifecycle (creation, deletion)
 * - Transaction lifecycle (signing, submission, confirmation)
 * - Session management (expiration)
 *
 * Example:
 * ```kotlin
 * kit.events.addListener { event ->
 *     when (event) {
 *         is SmartAccountEvent.WalletConnected ->
 *             println("Connected to ${event.contractId}")
 *         is SmartAccountEvent.TransactionSubmitted ->
 *             println("Transaction ${event.hash} submitted")
 *         else -> {}
 *     }
 * }
 * ```
 */
sealed class SmartAccountEvent {
    /**
     * Emitted when a wallet is connected.
     *
     * This event is fired when connecting to an existing wallet, either through
     * automatic session restoration or explicit connectWallet() call.
     *
     * @property contractId The smart account contract address (C-address)
     * @property credentialId The Base64URL-encoded credential ID
     */
    data class WalletConnected(
        val contractId: String,
        val credentialId: String
    ) : SmartAccountEvent()

    /**
     * Emitted when a wallet is disconnected.
     *
     * This event is fired when disconnect() is called. The session is cleared,
     * but stored credentials remain for future reconnection.
     *
     * @property contractId The smart account contract address that was disconnected
     */
    data class WalletDisconnected(
        val contractId: String
    ) : SmartAccountEvent()

    /**
     * Emitted when a new credential is created (passkey registered).
     *
     * This event is fired after successful WebAuthn registration, when the
     * credential is stored locally. Note that the wallet may not be deployed yet.
     *
     * @property credential The stored credential data
     */
    data class CredentialCreated(
        val credential: StoredCredential
    ) : SmartAccountEvent()

    /**
     * Emitted when a credential is deleted from storage.
     *
     * This event is fired when a credential is manually removed. If the credential
     * was connected, the wallet is automatically disconnected first.
     *
     * @property credentialId The Base64URL-encoded credential ID
     */
    data class CredentialDeleted(
        val credentialId: String
    ) : SmartAccountEvent()

    /**
     * Emitted when a session expires during connection attempt.
     *
     * This event is fired when attempting to restore a session that has expired.
     * The application should prompt the user to reconnect.
     *
     * @property contractId The smart account contract address
     * @property credentialId The Base64URL-encoded credential ID
     */
    data class SessionExpired(
        val contractId: String,
        val credentialId: String
    ) : SmartAccountEvent()

    /**
     * Emitted when a transaction is signed.
     *
     * This event is fired after successfully collecting all required signatures
     * for a transaction, before submission to the network.
     *
     * @property contractId The smart account contract address
     * @property credentialId The credential ID used for signing (null if only external signers)
     */
    data class TransactionSigned(
        val contractId: String,
        val credentialId: String?
    ) : SmartAccountEvent()

    /**
     * Emitted when a transaction is submitted to the network.
     *
     * This event is fired after sending the signed transaction to Soroban RPC
     * or the relayer service. The success flag indicates whether submission
     * succeeded (transaction accepted by network) or failed (network error).
     *
     * Note: A successful submission does not mean the transaction was included
     * in a ledger. Use TransactionConfirmed for final confirmation.
     *
     * @property hash The transaction hash
     * @property success True if submitted successfully, false if submission failed
     */
    data class TransactionSubmitted(
        val hash: String,
        val success: Boolean
    ) : SmartAccountEvent()
}

/**
 * Listener interface for Smart Account events.
 *
 * Implement this functional interface to receive event notifications.
 * The interface uses Kotlin's fun interface syntax, allowing lambda expressions.
 *
 * Example:
 * ```kotlin
 * val listener = SmartAccountEventListener { event ->
 *     println("Received event: $event")
 * }
 * kit.events.addListener(listener)
 * ```
 */
fun interface SmartAccountEventListener {
    /**
     * Called when an event is emitted.
     *
     * @param event The event that occurred
     */
    fun onEvent(event: SmartAccountEvent)
}

/**
 * Event emitter for Smart Account lifecycle events.
 *
 * This class manages event subscriptions and dispatches events to all registered
 * listeners. It provides thread-safe subscription management and error handling.
 *
 * Features:
 * - Thread-safe listener management with Mutex
 * - Multiple listeners per event type
 * - Error isolation (one failing listener does not affect others)
 * - Optional error handler for debugging listener failures
 *
 * Example:
 * ```kotlin
 * val emitter = SmartAccountEventEmitter()
 *
 * // Add a listener
 * val unsubscribe = emitter.on<SmartAccountEvent.WalletConnected> { event ->
 *     println("Connected to ${event.contractId}")
 * }
 *
 * // Add a one-time listener
 * emitter.once<SmartAccountEvent.TransactionSubmitted> { event ->
 *     println("First transaction: ${event.hash}")
 * }
 *
 * // Remove listener
 * unsubscribe()
 * ```
 */
class SmartAccountEventEmitter {
    /**
     * Map of event types to their registered listeners.
     */
    private val listeners = mutableMapOf<String, MutableSet<SmartAccountEventListener>>()

    /**
     * Mutex for thread-safe listener management.
     */
    private val listenerLock = Mutex()

    /**
     * Optional error handler for listener errors.
     *
     * By default, listener errors are silently caught to prevent one failing
     * listener from affecting others. Set this handler to receive error notifications.
     *
     * Example:
     * ```kotlin
     * emitter.setErrorHandler { event, error ->
     *     println("Listener error for ${event::class.simpleName}: $error")
     * }
     * ```
     */
    private var errorHandler: ((event: SmartAccountEvent, error: Throwable) -> Unit)? = null

    /**
     * Sets the error handler for listener errors.
     *
     * The error handler receives the event that was being dispatched and the
     * exception thrown by the failing listener.
     *
     * @param handler Error handler function, or null to disable
     */
    fun setErrorHandler(handler: ((event: SmartAccountEvent, error: Throwable) -> Unit)?) {
        errorHandler = handler
    }

    /**
     * Subscribes to events of a specific type.
     *
     * The listener will be called whenever an event of the specified type is emitted.
     * The returned unsubscribe function can be called to remove the listener.
     *
     * This method is type-safe: the listener parameter is constrained to match
     * the event type through Kotlin's reified type parameter.
     *
     * @param T The event type to subscribe to
     * @param listener The callback function to invoke on events
     * @return A function that unsubscribes the listener when called
     *
     * Example:
     * ```kotlin
     * val unsubscribe = emitter.on<SmartAccountEvent.WalletConnected> { event ->
     *     println("Wallet ${event.contractId} connected")
     * }
     * // Later: unsubscribe()
     * ```
     */
    inline fun <reified T : SmartAccountEvent> on(
        crossinline listener: (T) -> Unit
    ): () -> Unit {
        val eventType = T::class.simpleName ?: "Unknown"
        val wrapper = SmartAccountEventListener { event ->
            if (event is T) {
                listener(event)
            }
        }

        addListenerInternal(eventType, wrapper)

        return {
            removeListenerInternal(eventType, wrapper)
        }
    }

    /**
     * Subscribes to an event, but only triggers once.
     *
     * The listener will be automatically unsubscribed after the first event.
     * Useful for waiting for a single occurrence of an event.
     *
     * @param T The event type to subscribe to
     * @param listener The callback function to invoke once
     * @return A function that unsubscribes the listener if called before the event fires
     *
     * Example:
     * ```kotlin
     * emitter.once<SmartAccountEvent.TransactionSubmitted> { event ->
     *     println("First transaction submitted: ${event.hash}")
     * }
     * ```
     */
    inline fun <reified T : SmartAccountEvent> once(
        crossinline listener: (T) -> Unit
    ): () -> Unit {
        lateinit var unsubscribe: () -> Unit
        unsubscribe = on<T> { event ->
            unsubscribe()
            listener(event)
        }
        return unsubscribe
    }

    /**
     * Removes all listeners for a specific event type, or all listeners if no type is specified.
     *
     * @param eventType The event class name, or null to remove all listeners
     *
     * Example:
     * ```kotlin
     * // Remove all WalletConnected listeners
     * emitter.removeAllListeners("WalletConnected")
     *
     * // Remove all listeners for all event types
     * emitter.removeAllListeners()
     * ```
     */
    suspend fun removeAllListeners(eventType: String? = null) {
        listenerLock.withLock {
            if (eventType != null) {
                listeners.remove(eventType)
            } else {
                listeners.clear()
            }
        }
    }

    /**
     * Returns the number of listeners for a specific event type.
     *
     * @param eventType The event class name
     * @return The number of registered listeners
     *
     * Example:
     * ```kotlin
     * val count = emitter.listenerCount("WalletConnected")
     * println("$count listeners for WalletConnected")
     * ```
     */
    suspend fun listenerCount(eventType: String): Int {
        return listenerLock.withLock {
            listeners[eventType]?.size ?: 0
        }
    }

    /**
     * Emits an event to all registered listeners.
     *
     * This method dispatches the event to all listeners registered for the event's type.
     * Listener errors are caught and optionally passed to the error handler to prevent
     * one failing listener from affecting others.
     *
     * This is an internal method called by the kit's operation modules.
     *
     * @param event The event to emit
     *
     * Example (internal usage):
     * ```kotlin
     * kit.events.emit(SmartAccountEvent.WalletConnected(
     *     contractId = "CABC123...",
     *     credentialId = "cred123"
     * ))
     * ```
     */
    internal suspend fun emit(event: SmartAccountEvent) {
        val eventType = event::class.simpleName ?: return
        val currentListeners = listenerLock.withLock {
            listeners[eventType]?.toList() ?: emptyList()
        }

        currentListeners.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (err: Throwable) {
                // Call error handler if provided, otherwise silently catch
                // to prevent one failing listener from affecting others
                errorHandler?.invoke(event, err)
            }
        }
    }

    /**
     * Internal helper to add a listener with synchronization.
     */
    @PublishedApi
    internal fun addListenerInternal(eventType: String, listener: SmartAccountEventListener) {
        // Note: We can't use suspend here due to inline/crossinline constraints
        // So we use synchronized instead for this specific case
        synchronized(listeners) {
            listeners.getOrPut(eventType) { mutableSetOf() }.add(listener)
        }
    }

    /**
     * Internal helper to remove a listener with synchronization.
     */
    @PublishedApi
    internal fun removeListenerInternal(eventType: String, listener: SmartAccountEventListener) {
        synchronized(listeners) {
            listeners[eventType]?.remove(listener)
        }
    }
}
