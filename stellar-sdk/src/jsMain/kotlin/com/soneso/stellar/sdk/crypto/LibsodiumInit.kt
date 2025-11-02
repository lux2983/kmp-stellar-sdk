package com.soneso.stellar.sdk.crypto

import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * JavaScript implementation of libsodium initialization using dynamic types and JS Promise API.
 *
 * @see <a href="https://github.com/jedisct1/libsodium.js">libsodium.js</a>
 */
internal actual object LibsodiumInit {

    /**
     * Tracks whether libsodium has been successfully initialized.
     */
    private var initialized = false

    /**
     * Cached reference to the initialized sodium instance.
     */
    private var sodiumInstance: dynamic = null

    /**
     * Promise for ongoing initialization (to handle concurrent calls).
     */
    private var initPromise: Promise<Unit>? = null

    /**
     * Ensures libsodium is initialized and ready for use.
     *
     * This method is idempotent - it can be called multiple times safely.
     * Subsequent calls will return immediately if already initialized.
     *
     * ## Concurrency Handling
     *
     * If multiple coroutines call this simultaneously, they will all await
     * the same initialization promise, ensuring initialization happens exactly once.
     *
     * @throws IllegalStateException if libsodium fails to initialize
     */
    actual suspend fun ensureInitialized() {
        // Fast path: already initialized
        if (initialized) {
            return
        }

        // Check if initialization is in progress
        initPromise?.let { promise ->
            promise.await()
            return
        }

        // Start initialization
        val promise = initializeLibsodium()
        initPromise = promise

        try {
            promise.await()
        } catch (e: Throwable) {
            initPromise = null // Allow retry on failure
            throw IllegalStateException("Failed to initialize libsodium: ${e.message}", e)
        }
    }

    /**
     * Performs the actual initialization of libsodium.
     *
     * @return Promise that completes when initialization is done
     */
    private fun initializeLibsodium(): Promise<Unit> {
        return Promise { resolve, reject ->
            try {
                // Import libsodium-wrappers-sumo (includes SHA-256 and all other functions)
                // This works with webpack bundling automatically
                val sodium = js("require('libsodium-wrappers-sumo')")

                // Wait for sodium.ready promise
                val readyPromise = js("sodium.ready").unsafeCast<Promise<Unit>>()

                readyPromise.then(
                    onFulfilled = {
                        sodiumInstance = sodium
                        initialized = true
                        resolve(Unit)
                    },
                    onRejected = { error ->
                        reject(error as? Throwable ?: Exception(error.toString()))
                    }
                )
            } catch (e: Throwable) {
                reject(e)
            }
        }
    }

    /**
     * Returns the initialized sodium instance.
     *
     * **Important**: You must call [ensureInitialized] before calling this method.
     *
     * @return The libsodium instance (dynamic type)
     * @throws IllegalStateException if libsodium is not initialized
     */
    actual fun getSodium(): Any {
        if (!initialized || sodiumInstance == null) {
            throw IllegalStateException(
                "libsodium is not initialized. Call ensureInitialized() first."
            )
        }
        return sodiumInstance
    }

    /**
     * Checks if libsodium is currently initialized.
     *
     * @return true if initialized, false otherwise
     */
    actual fun isInitialized(): Boolean = initialized

    /**
     * Resets the initialization state (for testing purposes only).
     *
     * **Warning**: This should only be used in test scenarios.
     */
    actual fun resetForTesting() {
        initialized = false
        sodiumInstance = null
        initPromise = null
    }
}
