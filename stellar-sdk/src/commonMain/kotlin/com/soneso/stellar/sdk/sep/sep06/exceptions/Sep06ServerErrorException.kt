// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06.exceptions

/**
 * Exception thrown when the SEP-6 anchor returns a server error (HTTP 5xx).
 *
 * Indicates that the anchor experienced an internal error while processing
 * the request. This is typically a temporary condition that may be resolved
 * by retrying the request. Common causes:
 * - Anchor service temporarily unavailable (503)
 * - Internal server error (500)
 * - Bad gateway (502)
 * - Gateway timeout (504)
 * - Anchor maintenance mode
 * - Database or backend service issues
 *
 * Recovery actions:
 * - Retry the request after a delay
 * - Use exponential backoff for retries
 * - Check anchor status page if available
 * - Contact anchor support if the problem persists
 *
 * Example - Handle server error with retry:
 * ```kotlin
 * suspend fun depositWithRetry(
 *     sep06Service: Sep06Service,
 *     assetCode: String,
 *     jwt: String,
 *     maxRetries: Int = 3
 * ): Sep06DepositResponse? {
 *     var delay = 1000L
 *
 *     repeat(maxRetries) { attempt ->
 *         try {
 *             return sep06Service.deposit(assetCode, jwt)
 *         } catch (e: Sep06ServerErrorException) {
 *             println("Server error (${e.statusCode}): ${e.errorMessage ?: "Unknown error"}")
 *
 *             if (attempt < maxRetries - 1) {
 *                 println("Retrying in ${delay}ms...")
 *                 delay(delay)
 *                 delay *= 2 // Exponential backoff
 *             }
 *         }
 *     }
 *
 *     println("Max retries reached")
 *     return null
 * }
 * ```
 *
 * Example - Handle different status codes:
 * ```kotlin
 * suspend fun handleServerError(
 *     sep06Service: Sep06Service,
 *     assetCode: String,
 *     jwt: String
 * ): Sep06DepositResponse? {
 *     try {
 *         return sep06Service.deposit(assetCode, jwt)
 *     } catch (e: Sep06ServerErrorException) {
 *         when (e.statusCode) {
 *             503 -> {
 *                 println("Service temporarily unavailable, try again later")
 *             }
 *             504 -> {
 *                 println("Gateway timeout, the anchor may be overloaded")
 *             }
 *             else -> {
 *                 println("Server error (${e.statusCode}): ${e.errorMessage ?: "Unknown error"}")
 *             }
 *         }
 *         return null
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep06Exception] base class
 * - [SEP-0006 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md)
 *
 * @property statusCode HTTP status code returned by the anchor (5xx)
 * @property errorMessage Optional error message from the anchor, if available
 */
class Sep06ServerErrorException(
    val statusCode: Int,
    val errorMessage: String? = null
) : Sep06Exception("Server error ($statusCode)${errorMessage?.let { ": $it" } ?: ""}") {
    override fun toString(): String {
        return "SEP-06 server error ($statusCode)${errorMessage?.let { ": $it" } ?: ""}"
    }
}
