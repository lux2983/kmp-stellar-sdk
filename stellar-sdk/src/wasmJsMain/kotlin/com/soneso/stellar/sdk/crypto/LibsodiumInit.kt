package com.soneso.stellar.sdk.crypto

import kotlin.js.Promise
import kotlinx.coroutines.await

/**
 * WebAssembly/JS implementation of libsodium initialization using proper JsAny types.
 *
 * Unlike the pure JS implementation which uses dynamic types, this implementation
 * uses wasmJs-compatible external declarations and JsAny subtypes.
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
    private var sodiumInstance: LibsodiumModule? = null

    /**
     * Promise for ongoing initialization (to handle concurrent calls).
     */
    private var initPromise: Promise<JsAny?>? = null

    /**
     * Ensures libsodium is initialized and ready for use.
     */
    actual suspend fun ensureInitialized() {
        // Fast path: already initialized
        if (initialized) {
            return
        }

        // Check if initialization is in progress
        initPromise?.let { promise ->
            promise.await<JsAny?>()
            return
        }

        // Start initialization
        val promise = initializeLibsodium()
        initPromise = promise

        try {
            promise.await<JsAny?>()
        } catch (e: Throwable) {
            initPromise = null // Allow retry on failure
            throw IllegalStateException("Failed to initialize libsodium: ${e.message}", e)
        }
    }

    /**
     * Performs the actual initialization of libsodium.
     */
    private fun initializeLibsodium(): Promise<JsAny?> {
        return createInitPromise(
            onSuccess = { sodium ->
                sodiumInstance = sodium
                initialized = true
            }
        )
    }

    /**
     * Returns the initialized sodium instance.
     */
    actual fun getSodium(): Any {
        if (!initialized || sodiumInstance == null) {
            throw IllegalStateException(
                "libsodium is not initialized. Call ensureInitialized() first."
            )
        }
        return sodiumInstance!!
    }

    /**
     * Checks if libsodium is currently initialized.
     */
    actual fun isInitialized(): Boolean = initialized

    /**
     * Resets the initialization state (for testing purposes only).
     */
    actual fun resetForTesting() {
        initialized = false
        sodiumInstance = null
        initPromise = null
    }
}

/**
 * External function to require libsodium module.
 * Must be top-level to satisfy wasmJs js() restrictions.
 */
@JsFun("() => require('libsodium-wrappers-sumo')")
private external fun requireLibsodiumModule(): LibsodiumModule

/**
 * External function to get sodium.ready promise.
 * Must be top-level to satisfy wasmJs js() restrictions.
 */
@JsFun("(sodium) => sodium.ready")
private external fun getSodiumReadyPromise(sodium: LibsodiumModule): Promise<JsAny?>

/**
 * Creates a Promise that initializes libsodium and handles callbacks.
 *
 * This external function wraps the Promise constructor to properly handle
 * JsAny types in the resolve/reject callbacks.
 */
@JsFun("""(onSuccess) => {
    return new Promise((resolve, reject) => {
        try {
            const sodium = require('libsodium-wrappers-sumo');
            sodium.ready.then(() => {
                onSuccess(sodium);
                resolve(null);
            }).catch((error) => {
                reject(new Error(error.toString()));
            });
        } catch (e) {
            reject(new Error(e.toString()));
        }
    });
}""")
private external fun createInitPromise(onSuccess: (LibsodiumModule) -> Unit): Promise<JsAny?>

/**
 * External interface representing the libsodium module.
 * This provides type-safe access to libsodium functions in wasmJs.
 */
external interface LibsodiumModule : JsAny {
    // Crypto operations are accessed via wrapper functions
    // No need to declare every function here - we'll use @JsFun wrappers
}
