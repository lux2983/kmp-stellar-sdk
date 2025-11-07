package com.soneso.demo.stellar

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

/**
 * JavaScript implementation for loading WASM resources.
 *
 * For browser environments, WASM files must be served via HTTP and loaded
 * using the fetch() API. This implementation uses Kotlin/JS coroutines to
 * provide a clean async/await pattern.
 *
 * The WASM files are expected to be served from the `/wasm/` directory,
 * which is configured via webpack to copy files from the resources directory.
 */
actual suspend fun loadWasmResource(wasmFilename: String): ByteArray {
    return try {
        // Construct the URL to fetch the WASM file from
        // Try multiple possible paths to find the WASM file
        // 1. Relative to current page (works for most deployments)
        // 2. Absolute from root (works for root deployments)
        val possiblePaths = listOf(
            "./wasm/$wasmFilename",     // Relative to current page
            "wasm/$wasmFilename",        // Relative without leading ./
            "/wasm/$wasmFilename"        // Absolute from root
        )

        var lastError: dynamic = null

        for (url in possiblePaths) {
            try {
                // Use fetch() API to load the WASM file
                val response = window.fetch(url).await()

                if (response.ok) {
                    // Get the response as an ArrayBuffer
                    val arrayBuffer = response.arrayBuffer().await()

                    // Convert ArrayBuffer to Uint8Array
                    val uint8Array = Uint8Array(arrayBuffer)

                    // Convert Uint8Array to ByteArray
                    return ByteArray(uint8Array.length) { i ->
                        uint8Array[i]
                    }
                } else {
                    lastError = "HTTP ${response.status}"
                }
            } catch (e: dynamic) {
                lastError = e
            }
        }

        // If we get here, none of the paths worked
        throw IllegalArgumentException(
            "Failed to fetch WASM file '$wasmFilename': Tried paths: ${possiblePaths.joinToString(", ")}. " +
            "Last error: ${lastError?.message ?: lastError.toString()}. " +
            "Technical details: Failed to fetch WASM file '$wasmFilename': HTTP 404"
        )
    } catch (e: IllegalArgumentException) {
        // Re-throw IllegalArgumentException with original message
        throw e
    } catch (e: dynamic) {
        // Catch any JavaScript errors and wrap them with helpful context
        throw IllegalArgumentException(
            "Failed to load WASM file '$wasmFilename' from wasm/ directory. " +
            "Error: ${e.message ?: e.toString()}. " +
            "Ensure WASM files are properly deployed alongside the application.",
            e as? Throwable
        )
    }
}
