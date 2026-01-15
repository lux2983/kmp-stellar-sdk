// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when requesting authorization entries from the server fails.
 *
 * This can occur for several reasons:
 * - Network connectivity issues
 * - Server returns HTTP error (400, 401, 403, 500, etc.)
 * - Invalid response format from server
 * - Missing required response fields (authorization_entries)
 * - Configuration errors (missing WEB_AUTH_FOR_CONTRACTS_ENDPOINT, invalid stellar.toml)
 *
 * HTTP status codes and their meanings:
 * - 400 Bad Request: Invalid contract ID, home_domain, or parameters
 * - 401 Unauthorized: Server authentication required
 * - 403 Forbidden: Contract account not allowed to authenticate
 * - 404 Not Found: WebAuth for Contracts endpoint not available
 * - 500+ Server Error: Server-side issues
 *
 * Example - Handle challenge request errors:
 * ```kotlin
 * try {
 *     val challenge = webAuth.getChallenge(contractAccountId)
 * } catch (e: Sep45ChallengeRequestException) {
 *     when (e.statusCode) {
 *         400 -> println("Invalid contract ID: ${e.errorMessage}")
 *         403 -> println("Contract not allowed: ${e.errorMessage}")
 *         else -> println("Server error: ${e.message}")
 *     }
 * }
 * ```
 *
 * Example - Network errors (statusCode = null):
 * ```kotlin
 * try {
 *     val challenge = webAuth.getChallenge(contractAccountId)
 * } catch (e: Sep45ChallengeRequestException) {
 *     if (e.statusCode == null) {
 *         println("Network error: ${e.errorMessage}")
 *         // Retry logic
 *     }
 * }
 * ```
 *
 * @property statusCode HTTP status code (null for network errors or configuration errors)
 * @property errorMessage Optional detailed error message from server or client
 */
class Sep45ChallengeRequestException(
    message: String,
    val statusCode: Int? = null,
    val errorMessage: String? = null
) : Sep45Exception(
    buildString {
        append(message)
        if (statusCode != null) {
            append(" (HTTP $statusCode)")
        }
        if (errorMessage != null) {
            append(": $errorMessage")
        }
    }
)
