package com.soneso.stellar.sdk.util

import com.github.luben.zstd.Zstd
import com.github.luben.zstd.ZstdInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * JVM implementation of Zstandard decompressor using zstd-jni.
 *
 * Uses native JNI bindings to libzstd for high-performance decompression.
 * Includes safety limits to prevent decompression bombs.
 *
 * Supports both:
 * - Frames with content size in header (uses direct decompression)
 * - Frames without content size (uses streaming decompression)
 */
actual class ZstdDecompressor actual constructor() {

    actual fun decompress(compressed: ByteArray): ByteArray {
        // Try to get the expected decompressed size from the frame header
        val decompressedSize = Zstd.getFrameContentSize(compressed)

        return if (decompressedSize > 0) {
            // Size is known - use direct decompression (faster)
            require(decompressedSize <= MAX_DECOMPRESSED_SIZE) {
                "Decompressed size $decompressedSize exceeds maximum allowed size $MAX_DECOMPRESSED_SIZE bytes"
            }
            try {
                Zstd.decompress(compressed, decompressedSize.toInt())
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to decompress zstd data: ${e.message}", e)
            }
        } else {
            // Size is unknown (-1) or error - use streaming decompression
            decompressStreaming(compressed)
        }
    }

    /**
     * Streaming decompression for frames without content size in header.
     */
    private fun decompressStreaming(compressed: ByteArray): ByteArray {
        val input = ByteArrayInputStream(compressed)
        val output = ByteArrayOutputStream()
        var totalBytes = 0L

        ZstdInputStream(input).use { zstdInput ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int

            while (zstdInput.read(buffer).also { bytesRead = it } != -1) {
                totalBytes += bytesRead
                require(totalBytes <= MAX_DECOMPRESSED_SIZE) {
                    "Decompressed size exceeds maximum allowed size $MAX_DECOMPRESSED_SIZE bytes"
                }
                output.write(buffer, 0, bytesRead)
            }
        }

        return output.toByteArray()
    }

    actual fun close() {
        // No resources to release for JVM implementation
        // zstd-jni manages native memory automatically
    }

    companion object {
        // 100 MB safety limit to prevent decompression bombs
        private const val MAX_DECOMPRESSED_SIZE = 100 * 1024 * 1024L
        // Buffer size for streaming decompression
        private const val BUFFER_SIZE = 64 * 1024 // 64 KB
    }
}
