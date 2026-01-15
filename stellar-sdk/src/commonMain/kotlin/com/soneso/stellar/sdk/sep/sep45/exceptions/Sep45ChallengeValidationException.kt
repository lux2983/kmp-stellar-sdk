// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Base exception for SEP-45 challenge validation errors.
 *
 * Challenge validation is the most critical security component of SEP-45.
 * Each validation check protects against specific attack vectors:
 *
 * - Contract address must match WEB_AUTH_CONTRACT_ID (prevents contract substitution)
 * - Function name must be "web_auth_verify" (prevents unauthorized function calls)
 * - No sub-invocations allowed (prevents nested attack vectors)
 * - Home domain must match expected value (prevents domain substitution attacks)
 * - Web auth domain must match auth endpoint host (prevents URL confusion attacks)
 * - Account must match client account ID (prevents account substitution)
 * - Nonce must be present and consistent (prevents replay attacks)
 * - Server signature must be valid (prevents man-in-the-middle attacks)
 *
 * The SEP-45 specification defines 12 validation checks that MUST be performed
 * before signing any authorization entry. Each check has a corresponding
 * exception subclass.
 *
 * Security warning: Never sign authorization entries without performing
 * ALL validation checks. Skipping validation can lead to:
 * - Contract account takeover
 * - Unauthorized operations
 * - Replay attacks
 * - Man-in-the-middle attacks
 *
 * Example - Handle validation failures:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidServerSignatureException) {
 *     // Server signature invalid - possible MITM attack
 *     logger.error("SECURITY: Invalid server signature")
 *     throw e
 * } catch (e: Sep45InvalidContractAddressException) {
 *     // Contract address mismatch
 *     logger.error("Contract address mismatch: expected ${e.expected}, got ${e.actual}")
 *     throw e
 * } catch (e: Sep45ChallengeValidationException) {
 *     // Other validation failure
 *     logger.error("Challenge validation failed: ${e.message}")
 *     throw e
 * }
 * ```
 *
 * See also:
 * - [SEP-45 Security Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0045.md)
 *
 * @param message Description of the validation failure
 */
sealed class Sep45ChallengeValidationException(message: String) : Sep45Exception(message)
