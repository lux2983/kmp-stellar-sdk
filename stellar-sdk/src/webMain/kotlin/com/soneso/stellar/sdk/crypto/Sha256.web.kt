package com.soneso.stellar.sdk.crypto

/**
 * Web platform (JS and wasmJs) implementation of SHA-256.
 *
 * ## Platform-Specific Implementations
 *
 * - **JS (jsMain)**: Uses dynamic types, Uint8Array from org.khronos.webgl, and inline js() blocks
 * - **wasmJs (wasmJsMain)**: Uses JsAny types, external interfaces, and @JsFun wrappers
 *
 * Both implementations use libsodium-wrappers-sumo for SHA-256 hashing.
 *
 * ## Why Different Implementations?
 *
 * - **JS**: Can use dynamic types and more flexible JS interop
 * - **wasmJs**: Requires strict type safety (JsAny) and top-level external declarations
 *
 * This expect/actual pattern allows both platforms to use the same interface
 * while using platform-appropriate interop mechanisms.
 *
 * @see <a href="https://github.com/jedisct1/libsodium.js">libsodium.js</a>
 */
