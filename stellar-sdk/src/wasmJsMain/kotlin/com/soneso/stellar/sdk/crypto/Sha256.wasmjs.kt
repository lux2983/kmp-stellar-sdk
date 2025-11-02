package com.soneso.stellar.sdk.crypto

/**
 * WebAssembly/JS implementation of SHA-256.
 *
 * Uses libsodium-wrappers-sumo for SHA-256 hashing with proper async initialization.
 * The sumo build is required because crypto_hash_sha256 is not included in the standard build.
 *
 * This implementation uses wasmJs-compatible external declarations with JsAny types.
 *
 * @see <a href="https://github.com/jedisct1/libsodium.js">libsodium.js</a>
 */
internal class Sha256CryptoWasmJs : Sha256Crypto {
    override val libraryName: String = "libsodium-wrappers-sumo (crypto_hash_sha256)"

    override suspend fun hash(data: ByteArray): ByteArray {
        // Ensure libsodium is initialized before use
        LibsodiumInit.ensureInitialized()

        return try {
            val sodium = LibsodiumInit.getSodium() as LibsodiumModule
            val dataArray = byteArrayToUint8Array(data)

            // Call crypto_hash_sha256 via external function
            val resultArray = sha256Hash(sodium, dataArray)

            // SHA-256 produces 32-byte hash
            uint8ArrayToByteArray(resultArray, 32)
        } catch (e: Throwable) {
            throw IllegalStateException("Failed to compute SHA-256 hash: ${e.message}", e)
        }
    }
}

/**
 * Get the platform-specific SHA-256 crypto implementation.
 */
actual fun getSha256Crypto(): Sha256Crypto = Sha256CryptoWasmJs()

// ========== External Functions (libsodium operations) ==========

/**
 * Computes SHA-256 hash using libsodium.
 */
@JsFun("(sodium, data) => sodium.crypto_hash_sha256(data)")
private external fun sha256Hash(sodium: LibsodiumModule, data: JsUint8Array): JsUint8Array
