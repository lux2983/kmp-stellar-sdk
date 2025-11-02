package com.soneso.stellar.sdk.horizon.requests

import com.soneso.stellar.sdk.Util
import com.soneso.stellar.sdk.horizon.responses.Response
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * WebAssembly/JS implementation of SSE request handling.
 *
 * ## Implementation Strategy
 *
 * This implementation uses the browser's EventSource API with proper
 * wasmJs-compatible external declarations:
 *
 * - **API**: EventSource (Server-Sent Events)
 * - **Type Safety**: All JS interop uses JsAny and @JsFun wrappers
 * - **Compatibility**: Works in all browsers and Node.js
 * - **Error Handling**: Comprehensive error detection and reconnection
 * - **Resource Management**: Proper cleanup via cancellation
 *
 * All external interfaces extend JsAny for wasmJs type safety.
 * All JS interop uses @JsFun wrappers to avoid inline js() blocks.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventSource">EventSource API</a>
 */
internal actual suspend fun <T : Response> sseRequest(
    httpClient: HttpClient,
    url: Url,
    lastEventId: String?,
    serializer: KSerializer<T>,
    onEvent: (eventId: String?, data: String) -> Unit,
    onFailure: (error: Throwable, statusCode: Int?) -> Unit,
    onClose: () -> Unit
) {
    suspendCancellableCoroutine<Unit> { continuation ->
        val fullUrl = addClientIdentification(url)

        // Create EventSource using @JsFun wrapper
        val eventSource = createEventSource(fullUrl)

        // Handle incoming messages
        setOnMessage(eventSource) { data, eventId ->
            try {
                val dataStr = jsStringToKotlinString(data)
                if (dataStr != null) {
                    val eventIdStr = jsStringToKotlinString(eventId)?.takeIf { it.isNotEmpty() }
                    onEvent(eventIdStr, dataStr)
                }
            } catch (e: Exception) {
                onFailure(e, null)
            }
        }

        // Handle errors
        setOnError(eventSource) {
            val error = SSEException("EventSource error")
            onFailure(error, null)
        }

        // Handle open
        setOnOpen(eventSource) {
            // Connection opened successfully
        }

        // Cleanup when coroutine is cancelled
        continuation.invokeOnCancellation {
            closeEventSource(eventSource)
            onClose()
        }

        // The coroutine will remain suspended until cancelled
    }
}

/**
 * Adds client identification query parameters to the URL.
 */
private fun addClientIdentification(url: Url): String {
    return URLBuilder(url).apply {
        parameters.append("X-Client-Name", "kotlin-stellar-sdk")
        parameters.append("X-Client-Version", Util.getSdkVersion())
    }.buildString()
}

/**
 * wasmJs implementation of currentTimeMillis.
 */
internal actual fun currentTimeMillis(): Long {
    return getCurrentTimeMillis()
}

/**
 * wasmJs implementation of JSON deserialization.
 */
internal actual fun <T> deserializeJson(json: String, serializer: KSerializer<T>): T {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }
    return jsonConfig.decodeFromString(serializer, json)
}

/**
 * wasmJs implementation of network error detection.
 */
internal actual fun isNetworkError(error: Throwable): Boolean {
    // In wasmJs, most network errors are generic exceptions
    return error is SSEException || error.message?.contains("network", ignoreCase = true) == true
}

/**
 * Exception for SSE errors.
 */
private class SSEException(message: String) : Exception(message)

// ========== External Interfaces (EventSource API) ==========

/**
 * External interface for JavaScript EventSource.
 *
 * Provides a type-safe representation of the browser's EventSource API.
 */
external interface JsEventSource : JsAny {
    val url: JsString
    val readyState: Int
    val withCredentials: Boolean
}

/**
 * External interface for JavaScript String.
 */
external interface JsString : JsAny

/**
 * External interface for JavaScript MessageEvent data.
 */
external interface JsMessageEvent : JsAny {
    val data: JsString?
    val origin: JsString?
    val lastEventId: JsString?
}

// ========== External Functions (EventSource Operations) ==========

/**
 * Creates a new EventSource instance.
 *
 * @param url The URL to connect to for SSE
 * @return A new JsEventSource instance
 */
@JsFun("(url) => new EventSource(url)")
external fun createEventSource(url: String): JsEventSource

/**
 * Sets the onmessage handler for the EventSource.
 *
 * @param es The EventSource instance
 * @param callback The callback to invoke when a message is received
 */
@JsFun("""(es, callback) => {
    es.onmessage = (e) => callback(e.data, e.lastEventId || '');
}""")
external fun setOnMessage(
    es: JsEventSource,
    callback: (data: JsString?, eventId: JsString?) -> Unit
)

/**
 * Sets the onerror handler for the EventSource.
 *
 * @param es The EventSource instance
 * @param callback The callback to invoke when an error occurs
 */
@JsFun("""(es, callback) => {
    es.onerror = () => callback();
}""")
external fun setOnError(es: JsEventSource, callback: () -> Unit)

/**
 * Sets the onopen handler for the EventSource.
 *
 * @param es The EventSource instance
 * @param callback The callback to invoke when the connection opens
 */
@JsFun("""(es, callback) => {
    es.onopen = () => callback();
}""")
external fun setOnOpen(es: JsEventSource, callback: () -> Unit)

/**
 * Closes the EventSource connection.
 *
 * @param es The EventSource instance to close
 */
@JsFun("(es) => es.close()")
external fun closeEventSource(es: JsEventSource)

/**
 * Gets the current time in milliseconds since Unix epoch.
 *
 * Uses JavaScript's Date.now() function.
 *
 * @return The current time in milliseconds
 */
@JsFun("() => Date.now()")
external fun getCurrentTimeMillis(): Long

// ========== String Conversion Helpers ==========

/**
 * Converts a JavaScript String to a Kotlin String.
 *
 * @param jsStr The JavaScript String to convert (may be null)
 * @return A Kotlin String representation, or null if input is null
 */
@JsFun("(jsStr) => jsStr")
external fun jsStringToKotlinString(jsStr: JsString?): String?
