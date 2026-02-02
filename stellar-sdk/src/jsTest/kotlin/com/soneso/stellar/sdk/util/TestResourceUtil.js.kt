package com.soneso.stellar.sdk.util

/**
 * JavaScript implementation for reading test resources.
 *
 * Uses Node.js fs module to read files from the resources directory.
 */
actual object TestResourceUtil {
    /**
     * Reads a WASM file from the test resources directory.
     *
     * On JS, this implementation uses Node.js fs module to read files
     * from the resources/wasm directory.
     *
     * @param filename The name of the WASM file (e.g., "soroban_hello_world_contract.wasm")
     * @return The file contents as a ByteArray
     * @throws IllegalArgumentException if the file cannot be found or read
     */
    actual fun readWasmFile(filename: String): ByteArray {
        return try {
            val fs = js("require('fs')")
            val path = js("require('path')")

            // Build candidate paths:
            // 1. Relative to the JS bundle directory (__dirname/wasm/)
            //    This is where Gradle copies test resources in the JS test package.
            // 2. Fallback paths relative to CWD for different invocation contexts.
            val dirname = js("__dirname") as String
            val paths = arrayOf(
                path.resolve(dirname, "wasm", filename) as String,
                "kotlin/wasm/$filename",
                "src/commonTest/resources/wasm/$filename",
                "stellar-sdk/src/commonTest/resources/wasm/$filename"
            )

            for (p in paths) {
                try {
                    val buffer = fs.readFileSync(p)
                    val uint8Array = js("new Uint8Array(buffer)")
                    return ByteArray(uint8Array.length as Int) { i ->
                        uint8Array[i].unsafeCast<Byte>()
                    }
                } catch (e: dynamic) {
                    // Try next path
                }
            }

            throw IllegalArgumentException(
                "WASM file not found in any expected location: '$filename' " +
                "(searched from __dirname=$dirname)"
            )
        } catch (e: Throwable) {
            throw IllegalArgumentException("Failed to read WASM file '$filename': ${e.message}", e)
        }
    }
}
