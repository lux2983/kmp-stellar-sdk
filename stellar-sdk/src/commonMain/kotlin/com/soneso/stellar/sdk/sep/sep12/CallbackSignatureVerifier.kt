// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import com.soneso.stellar.sdk.KeyPair
import io.ktor.utils.io.core.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime

/**
 * Verifies callback signatures from SEP-12 anchor servers.
 *
 * When anchors send webhook notifications about customer KYC status changes,
 * they sign the request with their SIGNING_KEY to prove authenticity. This
 * object provides utilities to verify these signatures and prevent spoofing.
 *
 * Signature format (Signature or X-Stellar-Signature header):
 * ```
 * t=<timestamp>, s=<base64_signature>
 * ```
 *
 * Payload construction (signed data):
 * ```
 * <timestamp>.<host>.<body>
 * ```
 *
 * Where:
 * - timestamp: Unix timestamp when signature was created
 * - host: Expected callback host from the callback URL
 * - body: Raw JSON request body
 *
 * Security considerations:
 * - Always verify callback signatures before processing
 * - Check timestamp is within acceptable time window (default: 5 minutes)
 * - Use anchor's SIGNING_KEY from their stellar.toml
 * - Reject callbacks with expired timestamps
 * - Verify host matches your registered callback URL
 *
 * Example - Verify callback in webhook handler:
 * ```kotlin
 * suspend fun handleKYCCallback(
 *     signatureHeader: String,
 *     requestBody: String,
 *     callbackHost: String,
 *     anchorSigningKey: String
 * ) {
 *     val isValid = CallbackSignatureVerifier.verify(
 *         signatureHeader = signatureHeader,
 *         requestBody = requestBody,
 *         expectedHost = callbackHost,
 *         anchorSigningKey = anchorSigningKey,
 *         maxAgeSeconds = 300 // 5 minutes
 *     )
 *
 *     if (!isValid) {
 *         println("SECURITY WARNING: Invalid callback signature!")
 *         throw SecurityException("Callback signature verification failed")
 *     }
 *
 *     // Signature valid, process callback
 *     val callback = Json.decodeFromString<CustomerCallback>(requestBody)
 *     processCustomerUpdate(callback)
 * }
 * ```
 *
 * Example - Ktor webhook endpoint:
 * ```kotlin
 * routing {
 *     post("/kyc-callback") {
 *         // Get signature from header (try both standard and deprecated)
 *         val signatureHeader = call.request.headers["Signature"]
 *             ?: call.request.headers["X-Stellar-Signature"]
 *             ?: run {
 *                 call.respond(HttpStatusCode.Unauthorized, "Missing signature header")
 *                 return@post
 *             }
 *
 *         // Read request body
 *         val requestBody = call.receiveText()
 *
 *         // Get anchor's signing key from stellar.toml
 *         val stellarToml = StellarToml.fromDomain(anchorDomain)
 *         val signingKey = stellarToml.generalInformation.signingKey
 *             ?: run {
 *                 call.respond(HttpStatusCode.InternalServerError, "Anchor signing key not found")
 *                 return@post
 *             }
 *
 *         // Verify signature
 *         val isValid = CallbackSignatureVerifier.verify(
 *             signatureHeader = signatureHeader,
 *             requestBody = requestBody,
 *             expectedHost = "myapp.com",
 *             anchorSigningKey = signingKey
 *         )
 *
 *         if (!isValid) {
 *             call.respond(HttpStatusCode.Unauthorized, "Invalid signature")
 *             return@post
 *         }
 *
 *         // Process callback
 *         val callback = Json.decodeFromString<CustomerCallback>(requestBody)
 *         handleCustomerStatusChange(callback)
 *
 *         call.respond(HttpStatusCode.OK)
 *     }
 * }
 * ```
 *
 * Example - Handle timestamp expiry:
 * ```kotlin
 * suspend fun verifyCallbackWithLogging(
 *     signatureHeader: String,
 *     requestBody: String,
 *     expectedHost: String,
 *     anchorSigningKey: String
 * ): Boolean {
 *     try {
 *         val isValid = CallbackSignatureVerifier.verify(
 *             signatureHeader = signatureHeader,
 *             requestBody = requestBody,
 *             expectedHost = expectedHost,
 *             anchorSigningKey = anchorSigningKey,
 *             maxAgeSeconds = 300
 *         )
 *
 *         if (!isValid) {
 *             // Parse to get more details
 *             val (timestamp, signature) = CallbackSignatureVerifier.parseSignatureHeader(signatureHeader)
 *             val currentTime = Clock.System.now().epochSeconds
 *             val age = currentTime - timestamp
 *
 *             if (age > 300) {
 *                 println("Callback signature expired (age: ${age}s)")
 *             } else {
 *                 println("Callback signature invalid (not expired)")
 *             }
 *         }
 *
 *         return isValid
 *     } catch (e: Exception) {
 *         println("Signature verification error: ${e.message}")
 *         return false
 *     }
 * }
 * ```
 *
 * Example - Custom time window:
 * ```kotlin
 * // Allow older callbacks (e.g., 10 minutes) for testing
 * val isValid = CallbackSignatureVerifier.verify(
 *     signatureHeader = signatureHeader,
 *     requestBody = requestBody,
 *     expectedHost = "localhost:8080",
 *     anchorSigningKey = testAnchorKey,
 *     maxAgeSeconds = 600 // 10 minutes for testing
 * )
 * ```
 *
 * See also:
 * - [PutCustomerCallbackRequest] for registering callback URLs
 * - [StellarToml] for retrieving anchor signing keys
 * - [KeyPair] for signature verification
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md#customer-callback-put)
 */
@OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
object CallbackSignatureVerifier {

    /**
     * Verifies a callback signature from a SEP-12 anchor.
     *
     * Validates that:
     * 1. Signature header is properly formatted
     * 2. Timestamp is within acceptable age
     * 3. Signature is valid for the payload
     *
     * The payload is constructed as: `<timestamp>.<host>.<body>`
     *
     * @param signatureHeader The Signature or X-Stellar-Signature header value (format: "t=<timestamp>, s=<signature>")
     * @param requestBody The raw request body (JSON string)
     * @param expectedHost The expected host from the callback URL (e.g., "myapp.com")
     * @param anchorSigningKey The anchor's SIGNING_KEY from stellar.toml (G... address)
     * @param maxAgeSeconds Maximum age of signature in seconds (default: 300 = 5 minutes)
     * @return true if signature is valid and not expired, false otherwise
     *
     * Example:
     * ```kotlin
     * val isValid = CallbackSignatureVerifier.verify(
     *     signatureHeader = "t=1234567890, s=SGVsbG8gV29ybGQh",
     *     requestBody = """{"id":"123","status":"ACCEPTED"}""",
     *     expectedHost = "myapp.com",
     *     anchorSigningKey = "GBWMCCC3NHSKLAOJDBKKYW7SSH2PFTTNVFKWSGLWGDLEBKLOVP5JLBBP"
     * )
     * ```
     */
    suspend fun verify(
        signatureHeader: String,
        requestBody: String,
        expectedHost: String,
        anchorSigningKey: String,
        maxAgeSeconds: Long = 300
    ): Boolean {
        return try {
            // 1. Parse signature header: t=<timestamp>, s=<signature>
            val (timestamp, signature) = parseSignatureHeader(signatureHeader)

            // 2. Check timestamp is within acceptable range
            val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds() / 1000
            if (currentTime - timestamp > maxAgeSeconds) {
                return false // Signature too old
            }

            // 3. Construct payload: <timestamp>.<host>.<body>
            val payload = "$timestamp.$expectedHost.$requestBody"

            // 4. Verify signature using anchor's signing key
            val keyPair = KeyPair.fromAccountId(anchorSigningKey)
            val signatureBytes = Base64.decode(signature)
            val payloadBytes = payload.encodeToByteArray()

            keyPair.verify(payloadBytes, signatureBytes)
        } catch (e: Exception) {
            // Invalid signature format, invalid key, or verification failed
            false
        }
    }

    /**
     * Parses the signature header into timestamp and signature components.
     *
     * Header format: "t=<timestamp>, s=<signature>"
     *
     * @param header The Signature or X-Stellar-Signature header value
     * @return Pair of (timestamp, base64_signature)
     * @throws IllegalArgumentException if header format is invalid
     *
     * Example:
     * ```kotlin
     * val (timestamp, signature) = CallbackSignatureVerifier.parseSignatureHeader(
     *     "t=1234567890, s=SGVsbG8gV29ybGQh"
     * )
     * // timestamp = 1234567890
     * // signature = "SGVsbG8gV29ybGQh"
     * ```
     */
    fun parseSignatureHeader(header: String): Pair<Long, String> {
        // Parse "t=1600000000, s=base64signature=="
        val parts = header.split(",").associate { part ->
            val trimmed = part.trim()
            val (key, value) = trimmed.split("=", limit = 2)
            key to value
        }

        val timestamp = parts["t"]?.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid or missing timestamp in signature header: $header")

        val signature = parts["s"]
            ?: throw IllegalArgumentException("Missing signature in header: $header")

        return Pair(timestamp, signature)
    }
}
