// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when submitting a signed challenge to obtain a JWT token fails.
 *
 * This can occur for several reasons:
 * - Network connectivity issues
 * - Server returns HTTP error (400, 401, 403, 500, etc.)
 * - Invalid response format from server
 * - Missing JWT token in response
 * - Signature verification failed on server side
 *
 * HTTP status codes and their meanings:
 * - 400 Bad Request: Invalid transaction XDR or malformed request
 * - 401 Unauthorized: Signature verification failed or insufficient signatures
 * - 403 Forbidden: Account not allowed to authenticate
 * - 404 Not Found: WebAuth endpoint not available
 * - 500+ Server Error: Server-side issues
 *
 * Common causes of 401 errors:
 * - Missing required signatures (e.g., multi-sig account needs all signers)
 * - Invalid signatures (wrong keypairs used)
 * - Transaction was modified after signing
 * - Challenge expired before submission
 *
 * Example - Handle submission errors:
 * ```kotlin
 * try {
 *     val token = webAuth.sendSignedChallenge(signedChallengeXdr)
 * } catch (e: TokenSubmissionException) {
 *     when {
 *         e.message?.contains("401") == true -> {
 *             println("Signature verification failed - check signers")
 *         }
 *         e.message?.contains("400") == true -> {
 *             println("Invalid transaction format")
 *         }
 *         else -> {
 *             println("Server error: ${e.message}")
 *         }
 *     }
 * }
 * ```
 *
 * @param message Description of the failure
 * @param cause Underlying exception (network error, JSON parsing error, etc.)
 */
class TokenSubmissionException(message: String, cause: Throwable? = null) :
    WebAuthException(message, cause)
