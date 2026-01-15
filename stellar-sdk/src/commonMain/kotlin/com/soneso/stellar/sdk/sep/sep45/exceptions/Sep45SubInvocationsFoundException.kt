// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when an authorization entry contains sub-invocations.
 *
 * SEP-45 Security Requirement: Authorization entries MUST NOT contain any
 * sub-invocations. The rootInvocation.subInvocations array must be empty.
 *
 * This check is critical for security because:
 * - Sub-invocations could authorize additional contract calls beyond authentication
 * - A malicious server could embed hidden contract calls within the auth flow
 * - Sub-invocations add complexity that could hide malicious behavior
 *
 * The web_auth_verify function is a simple verification function that should
 * not call any other contracts. Any sub-invocations indicate:
 * - Server misconfiguration
 * - Malicious server attempting to authorize additional actions
 * - Non-compliant SEP-45 implementation
 *
 * Attack scenario prevented:
 * Without this check, a server could embed sub-invocations that transfer tokens,
 * modify contract state, or perform other unauthorized actions while appearing
 * to be a simple authentication request.
 *
 * Example - Handle sub-invocations:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45SubInvocationsFoundException) {
 *     logger.error("SECURITY: Authorization entry contains sub-invocations!")
 *     logger.error(e.message)
 *     // Do NOT proceed - possible embedded malicious calls
 * }
 * ```
 *
 * @param message Description of the sub-invocations found
 */
class Sep45SubInvocationsFoundException(message: String) : Sep45ChallengeValidationException(
    "Authorization entry contains sub-invocations which is not allowed. $message"
)
