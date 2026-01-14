// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep24.exceptions

/**
 * Base exception class for SEP-24 Interactive Deposit/Withdrawal errors.
 *
 * All SEP-24-specific exceptions extend this class to enable unified error handling
 * while providing specific error types for different failure scenarios.
 *
 * This exception hierarchy allows applications to handle interactive transfer errors at different levels:
 * - Catch Sep24Exception for general SEP-24 error handling
 * - Catch specific subclasses for precise error recovery
 *
 * Common error scenarios:
 * - [Sep24AuthenticationRequiredException]: JWT token missing or invalid (401/403)
 * - [Sep24InvalidRequestException]: Invalid parameters (400)
 * - [Sep24TransactionNotFoundException]: Transaction not found (404)
 * - [Sep24ServerErrorException]: Anchor server error (5xx)
 *
 * Example - General error handling:
 * ```kotlin
 * try {
 *     val response = sep24Service.deposit(request, jwt)
 * } catch (e: Sep24AuthenticationRequiredException) {
 *     // Re-authenticate via SEP-10
 *     println("Authentication required, please re-authenticate")
 * } catch (e: Sep24InvalidRequestException) {
 *     // Invalid request parameters
 *     println("Bad request: ${e.message}")
 * } catch (e: Sep24Exception) {
 *     // Other SEP-24 errors
 *     println("Transfer error: ${e.message}")
 * }
 * ```
 *
 * See also:
 * - [Sep24AuthenticationRequiredException] for 401/403 errors
 * - [Sep24InvalidRequestException] for 400 errors
 * - [Sep24TransactionNotFoundException] for 404 errors
 * - [Sep24ServerErrorException] for 5xx errors
 * - [SEP-0024 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0024.md)
 *
 * @property message Human-readable error description
 * @property cause Optional underlying cause of the error
 */
open class Sep24Exception(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    override fun toString(): String {
        return "SEP-24 error: $message"
    }
}
