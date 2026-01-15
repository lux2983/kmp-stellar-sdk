// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Base exception for all SEP-45 Web Authentication for Contracts errors.
 *
 * All exceptions thrown by [com.soneso.stellar.sdk.sep.sep45.WebAuthForContracts]
 * extend this class, allowing client code to catch all SEP-45 errors with
 * a single catch block when needed.
 *
 * SEP-45 authentication is security-critical. Always handle exceptions
 * appropriately and never silently ignore authentication failures.
 *
 * SEP-45 complements SEP-10 by providing authentication for contract accounts
 * (C... addresses) instead of traditional Stellar accounts (G... and M... addresses).
 *
 * Example - Catch all SEP-45 errors:
 * ```kotlin
 * try {
 *     val webAuth = WebAuthForContracts.fromDomain("example.com", Network.PUBLIC)
 *     val token = webAuth.jwtToken(contractAccountId, signers)
 * } catch (e: Sep45Exception) {
 *     // Handle any SEP-45 authentication error
 *     logger.error("Contract authentication failed: ${e.message}")
 * }
 * ```
 *
 * See also:
 * - [Sep45ChallengeRequestException] for challenge request failures
 * - [Sep45ChallengeValidationException] for challenge validation failures
 * - [Sep45TokenSubmissionException] for token submission failures
 *
 * @property message The error message describing what went wrong
 * @property cause The underlying cause, if any
 */
open class Sep45Exception(message: String, cause: Throwable? = null) : Exception(message, cause)
