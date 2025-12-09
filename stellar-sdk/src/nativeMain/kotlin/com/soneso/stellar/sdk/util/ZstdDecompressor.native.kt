package com.soneso.stellar.sdk.util

import com.squareup.zstd.ZstdDecompressor as SquareZstdDecompressor
import com.squareup.zstd.zstdDecompressor

/**
 * Native (iOS/macOS) implementation of Zstandard decompressor using Square's zstd-kmp.
 *
 * Uses native zstd bindings for native performance.
 * Includes safety limits to prevent decompression bombs.
 */
actual class ZstdDecompressor actual constructor() {
    private val decompressor: SquareZstdDecompressor = zstdDecompressor()

    actual fun decompress(compressed: ByteArray): ByteArray {
        // Estimate output size - zstd frames often have size in header
        // Start with 4x input size, grow if needed
        var outputSize = compressed.size * 4
        if (outputSize < 4096) outputSize = 4096

        var iterations = 0

        while (true) {
            // Safety check: prevent infinite loops
            require(iterations < MAX_ITERATIONS) {
                "Decompression exceeded maximum iterations ($MAX_ITERATIONS)"
            }

            // Safety check: prevent excessive memory allocation
            require(outputSize <= MAX_DECOMPRESSED_SIZE) {
                "Decompressed size $outputSize exceeds maximum allowed size $MAX_DECOMPRESSED_SIZE bytes"
            }

            val output = ByteArray(outputSize)
            var inputPos = 0
            var outputPos = 0

            while (inputPos < compressed.size) {
                decompressor.decompressStream(
                    outputByteArray = output,
                    outputStart = outputPos,
                    outputEnd = output.size,
                    inputByteArray = compressed,
                    inputStart = inputPos,
                    inputEnd = compressed.size
                )

                inputPos += decompressor.inputBytesProcessed
                outputPos += decompressor.outputBytesProcessed

                // If output buffer is full but input remains, need larger buffer
                if (outputPos >= output.size && inputPos < compressed.size) {
                    outputSize *= 2
                    iterations++
                    break // Restart with larger buffer
                }

                // If no progress was made on input, we're done or there's an error
                if (decompressor.inputBytesProcessed == 0 && decompressor.outputBytesProcessed == 0) {
                    break
                }
            }

            // If we consumed all input, return the result
            if (inputPos >= compressed.size) {
                return output.copyOf(outputPos)
            }
        }
    }

    actual fun close() {
        decompressor.close()
    }

    companion object {
        // 100 MB safety limit to prevent decompression bombs
        private const val MAX_DECOMPRESSED_SIZE = 100 * 1024 * 1024

        // Maximum number of buffer resize iterations to prevent infinite loops
        // Starting at 4x input size and doubling, this allows growth from 4x to ~16384x
        private const val MAX_ITERATIONS = 12
    }
}
