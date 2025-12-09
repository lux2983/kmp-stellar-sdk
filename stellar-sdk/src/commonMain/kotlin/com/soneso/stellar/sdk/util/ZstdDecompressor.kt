package com.soneso.stellar.sdk.util

/**
 * Cross-platform Zstandard decompressor for Stellar Data Lake integration.
 *
 * ## Platform Implementations
 *
 * ### JVM
 * - **Library**: zstd-jni (com.github.luben:zstd-jni)
 * - **Implementation**: Native JNI bindings to libzstd
 * - **Performance**: Fast native decompression
 * - **Features**: Size validation, safety limits
 *
 * ### Native (iOS/macOS)
 * - **Library**: kzstd (com.nicholaspjohnson:kzstd)
 * - **Implementation**: Kotlin/Native bindings to libzstd
 * - **Performance**: Native performance
 * - **Distribution**: Embedded in framework
 *
 * ### JavaScript (Browser & Node.js)
 * - **Library**: fflate (npm package)
 * - **Implementation**: Pure JavaScript/WASM implementation
 * - **Performance**: Good performance for web contexts
 * - **Compatibility**: Universal browser and Node.js support
 *
 * @see <a href="https://github.com/facebook/zstd">Zstandard Compression</a>
 * @see <a href="https://developers.stellar.org/docs/data/overview">Stellar Data Lake</a>
 */
expect class ZstdDecompressor() {
    /**
     * Decompress zstd-compressed data.
     *
     * @param compressed The compressed byte array
     * @return The decompressed byte array
     * @throws IllegalArgumentException if decompression fails or data is invalid
     */
    fun decompress(compressed: ByteArray): ByteArray

    /**
     * Release any native resources.
     *
     * On platforms that manage resources automatically (JVM, JS), this is a no-op.
     * On native platforms, this may release allocated memory or contexts.
     */
    fun close()
}
