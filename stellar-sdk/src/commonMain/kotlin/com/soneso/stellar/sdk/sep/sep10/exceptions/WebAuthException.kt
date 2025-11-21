// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Base exception for all SEP-10 Web Authentication errors.
 *
 * All exceptions thrown by [com.soneso.stellar.sdk.sep.sep10.WebAuth]
 * extend this class, allowing client code to catch all SEP-10 errors with
 * a single catch block when needed.
 *
 * SEP-10 authentication is security-critical. Always handle exceptions
 * appropriately and never silently ignore authentication failures.
 *
 * Example - Catch all SEP-10 errors:
 * ```kotlin
 * try {
 *     val webAuth = WebAuth.fromDomain("example.com", Network.PUBLIC)
 *     val token = webAuth.jwtToken(accountId, signers)
 * } catch (e: WebAuthException) {
 *     // Handle any SEP-10 authentication error
 *     logger.error("Authentication failed: ${e.message}")
 * }
 * ```
 *
 * See also:
 * - [ChallengeRequestException] for challenge request failures
 * - [ChallengeValidationException] for challenge validation failures
 * - [TokenSubmissionException] for token submission failures
 *
 * @property message The error message describing what went wrong
 * @property cause The underlying cause, if any
 */
sealed class WebAuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
