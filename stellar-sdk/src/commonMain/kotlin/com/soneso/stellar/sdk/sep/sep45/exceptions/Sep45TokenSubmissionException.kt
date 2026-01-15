// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when submitting signed authorization entries to obtain a JWT token fails.
 *
 * This can occur for several reasons:
 * - Network connectivity issues
 * - Server returns HTTP error (400, 401, 403, 500, etc.)
 * - Invalid response format from server
 * - Missing JWT token in response
 * - Server-side simulation/verification failed
 * - Authorization entries signature verification failed
 *
 * HTTP status codes and their meanings:
 * - 400 Bad Request: Invalid authorization entries XDR or malformed request
 * - 401 Unauthorized: Signature verification failed, insufficient signatures, or contract __check_auth rejected
 * - 403 Forbidden: Contract account not allowed to authenticate
 * - 404 Not Found: WebAuth endpoint not available
 * - 500+ Server Error: Server-side issues
 *
 * Common causes of 401 errors:
 * - Missing required signatures (contract's __check_auth expects specific signers)
 * - Invalid signatures (wrong keypairs used)
 * - Authorization entries were modified after signing
 * - Signature expiration ledger has passed
 * - Contract's __check_auth implementation rejected the authentication
 *
 * Example - Handle submission errors:
 * ```kotlin
 * try {
 *     val token = webAuth.sendSignedChallenge(signedEntries)
 * } catch (e: Sep45TokenSubmissionException) {
 *     when (e.statusCode) {
 *         401 -> {
 *             println("Signature verification failed - check signers")
 *             println("Error: ${e.errorMessage}")
 *         }
 *         400 -> println("Invalid authorization entries format")
 *         else -> println("Server error: ${e.message}")
 *     }
 * }
 * ```
 *
 * @property statusCode HTTP status code (null for network errors)
 * @property errorMessage Optional detailed error message from server
 */
class Sep45TokenSubmissionException(
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
