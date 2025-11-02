package com.soneso.stellar.sdk.crypto

/**
 * WebAssembly/JS implementation of Ed25519 cryptographic operations.
 *
 * ## Implementation Strategy
 *
 * This implementation uses libsodium.js for Ed25519 operations with proper
 * wasmJs-compatible external declarations:
 *
 * - **Library**: libsodium-wrappers-sumo (0.7.13 via npm)
 * - **Algorithm**: Ed25519 via `crypto_sign_*` functions
 * - **Security**: Audited C library compiled to WebAssembly
 * - **Compatibility**: Works in all browsers and Node.js
 * - **Initialization**: Lazy - happens on first crypto operation
 * - **Type Safety**: All JS interop uses JsAny and @JsFun wrappers
 *
 * All methods are suspend functions to properly handle libsodium initialization.
 *
 * @see <a href="https://github.com/jedisct1/libsodium.js">libsodium.js</a>
 */
internal class WasmJsEd25519Crypto : Ed25519Crypto {

    override val libraryName: String = "libsodium-wrappers-sumo"

    companion object {
        private const val SEED_BYTES = 32
        private const val PUBLIC_KEY_BYTES = 32
        private const val SECRET_KEY_BYTES = 64
        private const val SIGNATURE_BYTES = 64
    }

    /**
     * Generates a new random Ed25519 private key (32 bytes).
     *
     * Uses Web Crypto API's `crypto.getRandomValues()` for cryptographically
     * secure random number generation.
     */
    override suspend fun generatePrivateKey(): ByteArray {
        // We don't need libsodium for random generation - use Web Crypto API
        return try {
            val array = createUint8Array(SEED_BYTES)
            fillRandomValues(array)
            uint8ArrayToByteArray(array, SEED_BYTES)
        } catch (e: Throwable) {
            throw IllegalStateException("Failed to generate random private key: ${e.message}", e)
        }
    }

    /**
     * Derives the Ed25519 public key from a private key (seed).
     *
     * @param privateKey The 32-byte Ed25519 seed
     * @return The 32-byte Ed25519 public key
     */
    override suspend fun derivePublicKey(privateKey: ByteArray): ByteArray {
        require(privateKey.size == SEED_BYTES) { "Private key must be $SEED_BYTES bytes" }

        // Ensure libsodium is initialized
        LibsodiumInit.ensureInitialized()

        return try {
            val sodium = LibsodiumInit.getSodium() as LibsodiumModule
            val seedArray = byteArrayToUint8Array(privateKey)

            // Use libsodium to derive keypair from seed and extract public key
            val publicKeyArray = derivePublicKeyFromSeed(sodium, seedArray)

            uint8ArrayToByteArray(publicKeyArray, PUBLIC_KEY_BYTES)
        } catch (e: Throwable) {
            throw IllegalStateException("Failed to derive public key: ${e.message}", e)
        }
    }

    /**
     * Signs data using Ed25519.
     *
     * @param data The data to sign
     * @param privateKey The 32-byte Ed25519 seed
     * @return The 64-byte Ed25519 signature
     */
    override suspend fun sign(data: ByteArray, privateKey: ByteArray): ByteArray {
        require(privateKey.size == SEED_BYTES) { "Private key must be $SEED_BYTES bytes" }

        // Ensure libsodium is initialized
        LibsodiumInit.ensureInitialized()

        return try {
            val sodium = LibsodiumInit.getSodium() as LibsodiumModule
            val dataArray = byteArrayToUint8Array(data)
            val seedArray = byteArrayToUint8Array(privateKey)

            // Derive keypair and sign
            val signatureArray = signData(sodium, dataArray, seedArray)

            uint8ArrayToByteArray(signatureArray, SIGNATURE_BYTES)
        } catch (e: Throwable) {
            throw IllegalStateException("Failed to sign data: ${e.message}", e)
        }
    }

    /**
     * Verifies an Ed25519 signature.
     *
     * @param data The data that was signed
     * @param signature The 64-byte signature
     * @param publicKey The 32-byte Ed25519 public key
     * @return true if the signature is valid, false otherwise
     */
    override suspend fun verify(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean {
        require(publicKey.size == PUBLIC_KEY_BYTES) { "Public key must be $PUBLIC_KEY_BYTES bytes" }
        require(signature.size == SIGNATURE_BYTES) { "Signature must be $SIGNATURE_BYTES bytes" }

        // Ensure libsodium is initialized
        LibsodiumInit.ensureInitialized()

        return try {
            val sodium = LibsodiumInit.getSodium() as LibsodiumModule
            val dataArray = byteArrayToUint8Array(data)
            val signatureArray = byteArrayToUint8Array(signature)
            val publicKeyArray = byteArrayToUint8Array(publicKey)

            // Verify signature
            verifySignature(sodium, signatureArray, dataArray, publicKeyArray)
        } catch (e: Throwable) {
            false
        }
    }
}

/**
 * Get the platform-specific Ed25519 crypto implementation.
 *
 * Returns the WebAssembly/JS implementation using libsodium.js.
 */
actual fun getEd25519Crypto(): Ed25519Crypto = WasmJsEd25519Crypto()

// ========== External Functions (libsodium operations) ==========

/**
 * Derives the public key from a seed using libsodium.
 */
@JsFun("(sodium, seed) => sodium.crypto_sign_seed_keypair(seed).publicKey")
private external fun derivePublicKeyFromSeed(sodium: LibsodiumModule, seed: JsUint8Array): JsUint8Array

/**
 * Signs data using libsodium.
 */
@JsFun("(sodium, data, seed) => sodium.crypto_sign_detached(data, sodium.crypto_sign_seed_keypair(seed).privateKey)")
private external fun signData(sodium: LibsodiumModule, data: JsUint8Array, seed: JsUint8Array): JsUint8Array

/**
 * Verifies a signature using libsodium.
 */
@JsFun("""(sodium, signature, data, publicKey) => {
    try {
        return sodium.crypto_sign_verify_detached(signature, data, publicKey);
    } catch (e) {
        return false;
    }
}""")
private external fun verifySignature(
    sodium: LibsodiumModule,
    signature: JsUint8Array,
    data: JsUint8Array,
    publicKey: JsUint8Array
): Boolean
