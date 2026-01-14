// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06.exceptions

/**
 * Base exception class for SEP-6 Deposit and Withdrawal API errors.
 *
 * All SEP-6-specific exceptions extend this class to enable unified error handling
 * while providing specific error types for different failure scenarios.
 *
 * This exception hierarchy allows applications to handle transfer errors at different levels:
 * - Catch Sep06Exception for general SEP-6 error handling
 * - Catch specific subclasses for precise error recovery
 *
 * Common error scenarios:
 * - [Sep06AuthenticationRequiredException]: JWT token missing or invalid (403)
 * - [Sep06CustomerInformationNeededException]: Additional KYC fields required (403)
 * - [Sep06CustomerInformationStatusException]: KYC status pending or denied (403)
 * - [Sep06InvalidRequestException]: Invalid parameters (400)
 * - [Sep06TransactionNotFoundException]: Transaction not found (404)
 * - [Sep06ServerErrorException]: Anchor server error (5xx)
 *
 * Example - General error handling:
 * ```kotlin
 * try {
 *     val response = sep06Service.deposit(request, jwt)
 * } catch (e: Sep06AuthenticationRequiredException) {
 *     // Re-authenticate via SEP-10
 *     println("Authentication required, please re-authenticate")
 * } catch (e: Sep06CustomerInformationNeededException) {
 *     // Collect additional KYC fields
 *     println("Additional fields required: ${e.fields.joinToString()}")
 * } catch (e: Sep06InvalidRequestException) {
 *     // Invalid request parameters
 *     println("Bad request: ${e.errorMessage}")
 * } catch (e: Sep06Exception) {
 *     // Other SEP-6 errors
 *     println("Transfer error: ${e.message}")
 * }
 * ```
 *
 * See also:
 * - [Sep06AuthenticationRequiredException] for 403 errors (authentication)
 * - [Sep06CustomerInformationNeededException] for 403 errors (KYC fields needed)
 * - [Sep06CustomerInformationStatusException] for 403 errors (KYC status)
 * - [Sep06InvalidRequestException] for 400 errors
 * - [Sep06TransactionNotFoundException] for 404 errors
 * - [Sep06ServerErrorException] for 5xx errors
 * - [SEP-0006 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md)
 *
 * @property message Human-readable error description
 * @property cause Optional underlying cause of the error
 */
open class Sep06Exception(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    override fun toString(): String {
        return "SEP-06 error: $message"
    }
}
