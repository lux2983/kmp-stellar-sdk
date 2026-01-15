// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the server returns an unexpected or unparseable response.
 *
 * This can occur when:
 * - Server returns a non-JSON response (HTML error page, proxy error, etc.)
 * - Response JSON is malformed or doesn't match expected schema
 * - Server returns an unexpected HTTP status code
 * - Response is missing required fields
 * - Content-Type header is unexpected
 *
 * This exception provides the raw response body for debugging purposes.
 * The body may contain useful information about what went wrong on the server side.
 *
 * Common causes:
 * - Server misconfiguration returning wrong content type
 * - Proxy or CDN intercepting requests
 * - Server-side error generating invalid JSON
 * - API version mismatch between client and server
 *
 * Example - Debug unknown response:
 * ```kotlin
 * try {
 *     val token = webAuth.jwtToken(contractAccountId, signers)
 * } catch (e: Sep45UnknownResponseException) {
 *     logger.error("Unexpected response from server")
 *     logger.error("HTTP Status: ${e.code}")
 *     logger.error("Response body: ${e.body}")
 *     // May be HTML error page or invalid JSON
 * }
 * ```
 *
 * @property code The HTTP status code received
 * @property body The raw response body (may be truncated for large responses)
 */
class Sep45UnknownResponseException(
    message: String,
    val code: Int,
    val body: String
) : Sep45Exception("$message (HTTP $code): $body")
