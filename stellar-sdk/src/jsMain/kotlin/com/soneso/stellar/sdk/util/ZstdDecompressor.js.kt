package com.soneso.stellar.sdk.util

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

/**
 * External declaration for fflate npm package.
 * fflate provides high-performance compression/decompression in JavaScript.
 */
@JsModule("fflate")
@JsNonModule
external object Fflate {
    /**
     * Synchronously decompress zstd-compressed data.
     *
     * @param data The compressed data as Uint8Array
     * @return The decompressed data as Uint8Array
     */
    fun unzstdSync(data: Uint8Array): Uint8Array
}

/**
 * JavaScript implementation of Zstandard decompressor using fflate.
 *
 * Uses the fflate npm package which provides pure JavaScript/WASM
 * implementation with good performance and universal compatibility.
 * Includes safety limits to prevent decompression bombs.
 */
actual class ZstdDecompressor actual constructor() {

    actual fun decompress(compressed: ByteArray): ByteArray {
        return try {
            val input = compressed.toUint8Array()
            val output = Fflate.unzstdSync(input)

            // Safety check: prevent excessive memory allocation
            require(output.length <= MAX_DECOMPRESSED_SIZE) {
                "Decompressed size ${output.length} exceeds maximum allowed size $MAX_DECOMPRESSED_SIZE bytes"
            }

            output.toByteArray()
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decompress zstd data: ${e.message}", e)
        }
    }

    actual fun close() {
        // No resources to release for JS implementation
        // JavaScript manages memory automatically
    }

    companion object {
        // 100 MB safety limit to prevent decompression bombs
        private const val MAX_DECOMPRESSED_SIZE = 100 * 1024 * 1024
    }
}

/**
 * Convert ByteArray to JavaScript Uint8Array.
 */
private fun ByteArray.toUint8Array(): Uint8Array {
    val result = Uint8Array(size)
    for (i in indices) {
        result[i] = this[i]
    }
    return result
}

/**
 * Convert JavaScript Uint8Array to ByteArray.
 */
private fun Uint8Array.toByteArray(): ByteArray {
    val result = ByteArray(length)
    for (i in 0 until length) {
        result[i] = this[i]
    }
    return result
}
