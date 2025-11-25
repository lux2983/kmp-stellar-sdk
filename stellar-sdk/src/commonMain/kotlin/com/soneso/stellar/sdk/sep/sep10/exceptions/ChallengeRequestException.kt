// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when requesting a challenge transaction from the server fails.
 *
 * This can occur for several reasons:
 * - Network connectivity issues
 * - Server returns HTTP error (400, 401, 403, 500, etc.)
 * - Invalid response format from server
 * - Missing required response fields
 * - Configuration errors (missing WEB_AUTH_ENDPOINT, invalid stellar.toml)
 *
 * HTTP status codes and their meanings:
 * - 400 Bad Request: Invalid account ID or parameters
 * - 401 Unauthorized: Server authentication required
 * - 403 Forbidden: Account not allowed to authenticate
 * - 404 Not Found: WebAuth endpoint not available
 * - 500+ Server Error: Server-side issues
 *
 * Example - Handle challenge request errors:
 * ```kotlin
 * try {
 *     val challenge = webAuth.getChallenge(accountId)
 * } catch (e: ChallengeRequestException) {
 *     when (e.statusCode) {
 *         400 -> println("Invalid account ID: ${e.errorMessage}")
 *         403 -> println("Account not allowed: ${e.errorMessage}")
 *         else -> println("Server error: ${e.message}")
 *     }
 * }
 * ```
 *
 * Example - Network errors (statusCode = 0):
 * ```kotlin
 * try {
 *     val challenge = webAuth.getChallenge(accountId)
 * } catch (e: ChallengeRequestException) {
 *     if (e.statusCode == 0) {
 *         println("Network error: ${e.errorMessage}")
 *         // Retry logic
 *     }
 * }
 * ```
 *
 * @property statusCode HTTP status code (0 for network errors or configuration errors)
 * @property errorMessage Optional detailed error message from server or client
 * @param message Overall description of the failure
 * @param cause Underlying exception (network error, JSON parsing error, etc.)
 */
class ChallengeRequestException(
    val statusCode: Int = 0,
    val errorMessage: String? = null,
    message: String = "Challenge request failed" +
        (if (statusCode > 0) " (HTTP $statusCode)" else "") +
        (if (errorMessage != null) ": $errorMessage" else ""),
    cause: Throwable? = null
) : WebAuthException(message, cause) {

    /**
     * Convenience constructor for simple error messages without status code.
     */
    constructor(message: String, cause: Throwable? = null) : this(
        statusCode = 0,
        errorMessage = null,
        message = message,
        cause = cause
    )
}
