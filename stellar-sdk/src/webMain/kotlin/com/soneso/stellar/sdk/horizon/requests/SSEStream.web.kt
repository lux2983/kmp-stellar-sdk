package com.soneso.stellar.sdk.horizon.requests

/**
 * Web platform (JS and wasmJs) shared code for SSE streaming.
 *
 * This file contains code that is shared between JS and wasmJs platforms.
 * Actual implementations are provided in:
 * - jsMain/kotlin/.../SSEStream.js.kt (classic JavaScript target)
 * - wasmJsMain/kotlin/.../SSEStream.wasmjs.kt (WebAssembly/JS target)
 *
 * Both implementations use the browser's EventSource API but with different
 * interop approaches due to wasmJs type safety requirements.
 */

// No shared web-specific code needed at this time.
// All platform-specific implementations are in jsMain and wasmJsMain.
