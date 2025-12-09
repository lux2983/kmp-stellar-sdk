// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12.exceptions

/**
 * Base exception class for SEP-12 KYC API errors.
 *
 * All SEP-12-specific exceptions extend this class to enable unified error handling
 * while providing specific error types for different failure scenarios.
 *
 * This exception hierarchy allows applications to handle KYC errors at different levels:
 * - Catch KYCException for general KYC error handling
 * - Catch specific subclasses for precise error recovery
 *
 * Common error scenarios:
 * - [CustomerNotFoundException]: Customer not found (404)
 * - [UnauthorizedException]: Authentication failed (401)
 * - [FileTooLargeException]: Uploaded file exceeds size limit (413)
 * - [CustomerAlreadyExistsException]: Duplicate customer (409)
 * - [InvalidFieldException]: Invalid field data (400)
 *
 * Example - General error handling:
 * ```kotlin
 * try {
 *     val response = kycService.getCustomerInfo(request)
 * } catch (e: CustomerNotFoundException) {
 *     // Customer doesn't exist, need to register
 *     println("Customer not found: ${e.accountId}")
 * } catch (e: UnauthorizedException) {
 *     // JWT token invalid or expired
 *     println("Authentication failed, please log in again")
 * } catch (e: KYCException) {
 *     // Other KYC errors
 *     println("KYC error: ${e.message}")
 * }
 * ```
 *
 * Example - Specific error recovery:
 * ```kotlin
 * try {
 *     kycService.postCustomerFile(largeFile, jwt)
 * } catch (e: FileTooLargeException) {
 *     println("File too large: ${e.fileSize} bytes")
 *     println("Please compress or resize the file")
 * } catch (e: InvalidFieldException) {
 *     println("Invalid field: ${e.fieldName} - ${e.fieldError}")
 * }
 * ```
 *
 * See also:
 * - [CustomerNotFoundException] for 404 errors
 * - [UnauthorizedException] for 401 errors
 * - [FileTooLargeException] for 413 errors
 * - [CustomerAlreadyExistsException] for 409 errors
 * - [InvalidFieldException] for 400 field validation errors
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property message Human-readable error description
 * @property cause Optional underlying cause of the error
 */
open class KYCException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
