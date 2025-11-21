// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Base exception for challenge validation errors.
 *
 * Challenge validation is the most critical security component of SEP-10.
 * Each validation check protects against specific attack vectors:
 *
 * - Sequence number must be 0 (prevents transaction replay)
 * - Time bounds must be recent (prevents replay after expiration)
 * - Operations must be ManageData (prevents destructive actions)
 * - Server signature must be valid (prevents man-in-the-middle attacks)
 * - Source accounts must match expected values (prevents account substitution)
 *
 * The SEP-10 specification defines 13 validation checks that MUST be performed
 * before signing any challenge transaction. Each check has a corresponding
 * exception subclass.
 *
 * Security warning: Never sign a challenge transaction without performing
 * ALL validation checks. Skipping validation can lead to:
 * - Account takeover
 * - Unauthorized operations
 * - Replay attacks
 * - Man-in-the-middle attacks
 *
 * Example - Handle validation failures:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(challengeXdr, accountId)
 * } catch (e: InvalidSignatureException) {
 *     // Server signature invalid - possible MITM attack
 *     logger.error("SECURITY: Invalid server signature")
 *     throw e
 * } catch (e: InvalidTimeBoundsException) {
 *     // Challenge expired or too far in future
 *     logger.warn("Challenge expired, requesting new one")
 *     // Request fresh challenge
 * } catch (e: ChallengeValidationException) {
 *     // Other validation failure
 *     logger.error("Challenge validation failed: ${e.message}")
 *     throw e
 * }
 * ```
 *
 * See also:
 * - [SEP-10 Security Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md#verification)
 *
 * @param message Description of the validation failure
 */
sealed class ChallengeValidationException(message: String) : WebAuthException(message)
