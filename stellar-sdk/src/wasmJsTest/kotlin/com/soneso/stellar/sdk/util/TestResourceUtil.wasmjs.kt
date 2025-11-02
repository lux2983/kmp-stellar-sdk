package com.soneso.stellar.sdk.util

/**
 * WebAssembly implementation for reading test resources.
 *
 * Uses Node.js fs module to read files from the resources directory.
 * This implementation uses proper wasmJs interop patterns with @JsFun and external interfaces.
 */

// External interface for Node.js Buffer
external interface JsBuffer : JsAny {
    val length: Int
}

// External interface for Uint8Array
external interface JsUint8Array : JsAny {
    val length: Int
}

// Try to read file using Node.js fs.readFileSync
// Returns null if file not found, otherwise returns the Buffer
@JsFun("(path) => { try { return require('fs').readFileSync(path); } catch (e) { return null; } }")
external fun tryReadFile(path: String): JsBuffer?

// Create Uint8Array from Buffer
@JsFun("(buffer) => new Uint8Array(buffer)")
external fun bufferToUint8Array(buffer: JsBuffer): JsUint8Array

// Get byte from Uint8Array
@JsFun("(arr, index) => arr[index]")
external fun getUint8(arr: JsUint8Array, index: Int): Byte

// Get current working directory using Node.js process.cwd()
@JsFun("() => process.cwd()")
external fun getCurrentWorkingDirectory(): String

actual object TestResourceUtil {
    /**
     * Reads a WASM file from the test resources directory.
     *
     * On wasmJs, this implementation uses Node.js fs module to read files
     * from the resources/wasm directory using proper wasmJs interop patterns.
     *
     * @param filename The name of the WASM file (e.g., "soroban_hello_world_contract.wasm")
     * @return The file contents as a ByteArray
     * @throws IllegalArgumentException if the file cannot be found or read
     */
    actual fun readWasmFile(filename: String): ByteArray {
        return try {
            val cwd = getCurrentWorkingDirectory()

            // Try multiple possible paths relative to different working directories
            val paths = arrayOf(
                // From build/wasm/packages/kmp-stellar-sdk-stellar-sdk-test (Kotlin/Wasm location)
                // Need to go up 4 levels to project root
                "../../../../stellar-sdk/src/commonTest/resources/wasm/$filename",

                // Relative to project root
                "stellar-sdk/src/commonTest/resources/wasm/$filename",

                // Relative to stellar-sdk directory
                "src/commonTest/resources/wasm/$filename",

                // One level up from current directory
                "../src/commonTest/resources/wasm/$filename",

                // Two levels up from current directory
                "../../src/commonTest/resources/wasm/$filename",

                // Three levels up (for deep build directories)
                "../../../src/commonTest/resources/wasm/$filename",

                // Five levels up (for very deep build directories)
                "../../../../../src/commonTest/resources/wasm/$filename",

                // Absolute-style from cwd
                "$cwd/stellar-sdk/src/commonTest/resources/wasm/$filename",
                "$cwd/src/commonTest/resources/wasm/$filename"
            )

            for (path in paths) {
                val buffer = tryReadFile(path)
                if (buffer != null) {
                    val uint8Array = bufferToUint8Array(buffer)
                    val length = uint8Array.length

                    // Convert to ByteArray
                    return ByteArray(length) { i ->
                        getUint8(uint8Array, i)
                    }
                }
            }

            // If not found, provide detailed error with working directory and all attempted paths
            throw IllegalArgumentException(
                "WASM file not found: '$filename'\n" +
                "Working directory: $cwd\n" +
                "Tried paths: ${paths.joinToString(", ")}"
            )
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Throwable) {
            throw IllegalArgumentException("Failed to read WASM file '$filename': ${e.message}", e)
        }
    }
}
