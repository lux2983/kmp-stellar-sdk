package com.soneso.stellar.sdk.crypto

/**
 * Platform-agnostic libsodium initialization manager for web platforms (JS and wasmJs).
 *
 * This class ensures that the libsodium-wrappers library is loaded and initialized
 * exactly once before any cryptographic operations are performed.
 *
 * ## Purpose
 *
 * Abstracts away platform-specific differences in libsodium initialization:
 * - **JS**: Uses dynamic types and Promise-based initialization
 * - **wasmJs**: Uses JsAny types and proper external declarations
 *
 * ## Usage
 *
 * ```kotlin
 * suspend fun someCryptoOperation() {
 *     LibsodiumInit.ensureInitialized() // Ensures libsodium is ready
 *     val sodium = LibsodiumInit.getSodium()
 *     // Use sodium APIs...
 * }
 * ```
 *
 * @see <a href="https://github.com/jedisct1/libsodium.js">libsodium.js</a>
 */
internal expect object LibsodiumInit {
    /**
     * Ensures libsodium is initialized and ready for use.
     *
     * This method is idempotent - it can be called multiple times safely.
     * Subsequent calls will return immediately if already initialized.
     *
     * @throws IllegalStateException if libsodium fails to initialize
     */
    suspend fun ensureInitialized()

    /**
     * Returns the initialized sodium instance.
     *
     * **Important**: You must call [ensureInitialized] before calling this method.
     *
     * @return The libsodium instance (platform-specific type)
     * @throws IllegalStateException if libsodium is not initialized
     */
    fun getSodium(): Any // Return Any since actual implementations use different types (dynamic vs JsAny)

    /**
     * Checks if libsodium is currently initialized.
     *
     * @return true if initialized, false otherwise
     */
    fun isInitialized(): Boolean

    /**
     * Resets the initialization state (for testing purposes only).
     *
     * **Warning**: This should only be used in test scenarios.
     */
    fun resetForTesting()
}
