package com.soneso.stellar.sdk.crypto

/**
 * Shared utilities for wasmJs interop with JavaScript typed arrays.
 *
 * This file provides common external declarations and helper functions
 * used across multiple crypto implementations (Ed25519, SHA-256, etc.).
 *
 * ## Design Rationale
 *
 * - **JsAny compatibility**: All external interfaces extend JsAny for wasmJs type safety
 * - **@JsFun wrappers**: All JS interop uses top-level @JsFun external functions
 * - **Shared code**: Eliminates duplication across crypto implementations
 *
 * @see <a href="https://kotlinlang.org/docs/wasm-js-interop.html">Kotlin/Wasm JS Interop</a>
 */

// ========== External Interfaces ==========

/**
 * External interface for JavaScript Uint8Array.
 *
 * Provides a type-safe representation of JS typed arrays in wasmJs.
 */
external interface JsUint8Array : JsAny {
    val length: Int
}

// ========== External Functions (Uint8Array Operations) ==========

/**
 * Creates a new Uint8Array with the specified size.
 */
@JsFun("(size) => new Uint8Array(size)")
external fun createUint8Array(size: Int): JsUint8Array

/**
 * Gets a byte value from a Uint8Array at the specified index.
 */
@JsFun("(arr, idx) => arr[idx]")
external fun getUint8(arr: JsUint8Array, idx: Int): Byte

/**
 * Sets a byte value in a Uint8Array at the specified index.
 */
@JsFun("(arr, idx, value) => { arr[idx] = value; }")
external fun setUint8(arr: JsUint8Array, idx: Int, value: Byte)

/**
 * Fills a Uint8Array with cryptographically secure random values.
 *
 * Uses Web Crypto API's `crypto.getRandomValues()`.
 */
@JsFun("(arr) => crypto.getRandomValues(arr)")
external fun fillRandomValues(arr: JsUint8Array)

// ========== Helper Functions ==========

/**
 * Converts a Kotlin ByteArray to a JS Uint8Array.
 *
 * This function creates a new Uint8Array and copies all bytes from the
 * Kotlin ByteArray into it.
 *
 * @param bytes The Kotlin ByteArray to convert
 * @return A new JS Uint8Array containing the same data
 */
internal fun byteArrayToUint8Array(bytes: ByteArray): JsUint8Array {
    val array = createUint8Array(bytes.size)
    for (i in bytes.indices) {
        setUint8(array, i, bytes[i])
    }
    return array
}

/**
 * Converts a JS Uint8Array to a Kotlin ByteArray.
 *
 * This function creates a new Kotlin ByteArray and copies all bytes from the
 * JS Uint8Array into it.
 *
 * @param array The JS Uint8Array to convert
 * @param size The number of bytes to copy
 * @return A new Kotlin ByteArray containing the same data
 */
internal fun uint8ArrayToByteArray(array: JsUint8Array, size: Int): ByteArray {
    return ByteArray(size) { i -> getUint8(array, i) }
}
