// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38.exceptions

/**
 * Base exception class for SEP-38 Quote API errors.
 *
 * All SEP-38-specific exceptions extend this class to enable unified error handling
 * while providing specific error types for different failure scenarios.
 *
 * This exception hierarchy allows applications to handle quote API errors at different levels:
 * - Catch Sep38Exception for general quote API error handling
 * - Catch specific subclasses for precise error recovery
 *
 * Common error scenarios:
 * - [Sep38BadRequestException]: Invalid parameters (400)
 * - [Sep38PermissionDeniedException]: Authentication failed (403)
 * - [Sep38NotFoundException]: Quote not found (404)
 * - [Sep38UnknownResponseException]: Unexpected HTTP status
 *
 * Example - General error handling:
 * ```kotlin
 * try {
 *     val quote = quoteService.postQuote(request, jwt)
 * } catch (e: Sep38BadRequestException) {
 *     // Invalid request parameters
 *     println("Bad request: ${e.message}")
 * } catch (e: Sep38PermissionDeniedException) {
 *     // Authentication failed
 *     println("Permission denied, please re-authenticate")
 * } catch (e: Sep38Exception) {
 *     // Other quote API errors
 *     println("Quote error: ${e.message}")
 * }
 * ```
 *
 * Example - Specific error recovery:
 * ```kotlin
 * try {
 *     val quote = quoteService.getQuote(quoteId, jwt)
 * } catch (e: Sep38NotFoundException) {
 *     println("Quote not found, it may have expired")
 *     // Request new quote
 * } catch (e: Sep38PermissionDeniedException) {
 *     println("Invalid token, re-authenticating...")
 *     // Re-authenticate via SEP-10
 * }
 * ```
 *
 * See also:
 * - [Sep38BadRequestException] for 400 errors
 * - [Sep38PermissionDeniedException] for 403 errors
 * - [Sep38NotFoundException] for 404 errors
 * - [Sep38UnknownResponseException] for unexpected HTTP status codes
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property message Human-readable error description
 * @property cause Optional underlying cause of the error
 */
open class Sep38Exception(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    override fun toString(): String {
        return "SEP-38 error: $message"
    }
}
