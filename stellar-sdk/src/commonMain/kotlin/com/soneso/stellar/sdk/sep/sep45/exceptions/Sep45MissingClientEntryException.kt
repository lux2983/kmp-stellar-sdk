// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when a required client entry is missing from the challenge.
 *
 * SEP-45 requires specific authorization entries for the authentication flow:
 *
 * 1. Client Entry (always required):
 *    - credentials.type = SOROBAN_CREDENTIALS_ADDRESS
 *    - credentials.addressCredentials.address matching the client contract ID (C...)
 *    - This entry must be signed by the contract's authorized signers
 *
 * 2. Client Domain Entry (required when client domain is provided):
 *    - credentials.type = SOROBAN_CREDENTIALS_ADDRESS
 *    - credentials.addressCredentials.address matching the client domain account (G...)
 *    - This entry must be signed by the client domain's signing key
 *
 * Common causes of this error:
 * - Server bug not including required entry
 * - Challenge was modified/stripped in transit
 * - Non-compliant SEP-45 implementation
 * - Client domain account not matching expected value
 *
 * This exception is used for both missing client entries and missing client domain
 * entries. The message will indicate which entry is missing.
 *
 * Example - Handle missing client entry:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, contractId, clientDomainAccount)
 * } catch (e: Sep45MissingClientEntryException) {
 *     if (e.message?.contains("client domain") == true) {
 *         logger.error("Client domain entry missing: ${e.message}")
 *     } else {
 *         logger.error("Client entry missing: ${e.message}")
 *     }
 *     // Request a new challenge from the server
 * }
 * ```
 *
 * @param message Description of what client entry is expected
 */
class Sep45MissingClientEntryException(message: String) : Sep45ChallengeValidationException(
    "Client authorization entry missing. $message"
)
