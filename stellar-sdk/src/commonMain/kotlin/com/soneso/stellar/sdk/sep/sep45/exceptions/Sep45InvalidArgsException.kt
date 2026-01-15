// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when authorization entry arguments are invalid or malformed.
 *
 * SEP-45 requires authorization entries to contain specific arguments in a
 * Map<Symbol, String> format. This exception is thrown when:
 * - Arguments are not in the expected format (not a map)
 * - Required arguments are missing
 * - Arguments have invalid types
 * - Arguments structure is corrupted
 *
 * Required arguments in web_auth_verify:
 * - account: Client contract address (C...)
 * - home_domain: Target home domain
 * - web_auth_domain: Server's domain (with port if non-standard)
 * - web_auth_domain_account: Server signing key (G...)
 * - nonce: Unique replay prevention value
 *
 * Optional arguments:
 * - client_domain: Client domain (if client domain auth is used)
 * - client_domain_account: Client domain account (G...)
 *
 * For specific argument validation failures, more specific exceptions are thrown:
 * - Sep45InvalidAccountException for account mismatch
 * - Sep45InvalidHomeDomainException for home_domain mismatch
 * - Sep45InvalidWebAuthDomainException for web_auth_domain mismatch
 * - Sep45InvalidNonceException for nonce issues
 *
 * This exception covers structural issues with the arguments.
 *
 * Example - Handle argument parsing errors:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidArgsException) {
 *     logger.error("Invalid authorization entry arguments: ${e.message}")
 *     // Server may have sent malformed challenge
 * }
 * ```
 *
 * @param message Description of the argument validation failure
 */
class Sep45InvalidArgsException(message: String) : Sep45ChallengeValidationException(
    "Authorization entry arguments validation failed. $message"
)
