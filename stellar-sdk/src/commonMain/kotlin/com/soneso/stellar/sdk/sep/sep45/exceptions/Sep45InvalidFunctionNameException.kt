// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the authorization entry calls an unexpected function.
 *
 * SEP-45 Security Requirement: The authorization entry's function name MUST be
 * "web_auth_verify". This is the standard function name defined in the SEP-45
 * specification for authentication.
 *
 * This check is critical for security because:
 * - It ensures the authorization entry is for authentication only
 * - It prevents authorization of arbitrary contract function calls
 * - A malicious server could try to authorize other functions that
 *   transfer assets or modify contract state
 *
 * Attack scenario prevented:
 * Without this check, a server could return authorization entries that call
 * dangerous functions like "transfer", "approve", or other state-modifying
 * functions, tricking the client into authorizing unintended actions.
 *
 * The function name is extracted from the authorization entry's
 * rootInvocation.function.contractFn.functionName field.
 *
 * Example - Handle function name mismatch:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidFunctionNameException) {
 *     logger.error("SECURITY: Invalid function name!")
 *     logger.error("Expected: ${e.expected}")
 *     logger.error("Actual: ${e.actual}")
 *     // Do NOT proceed - possible attack attempting unauthorized action
 * }
 * ```
 *
 * @property expected The expected function name ("web_auth_verify")
 * @property actual The actual function name found in the authorization entry
 */
class Sep45InvalidFunctionNameException(
    val expected: String,
    val actual: String
) : Sep45ChallengeValidationException(
    "Authorization entry calls wrong function. Expected: $expected, but found: $actual"
)
