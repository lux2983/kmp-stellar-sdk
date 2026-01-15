// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the account argument doesn't match the client's contract account.
 *
 * SEP-45 Security Requirement: The account argument in the authorization entry
 * MUST match the client's contract account ID (C... address) that was provided
 * when requesting the challenge.
 *
 * This check is critical for security because:
 * - It ensures the challenge is for the correct contract account
 * - It prevents account substitution attacks
 * - It verifies the server generated the challenge for the intended account
 *
 * Attack scenario prevented:
 * Without this check, a server could return a challenge for a different account,
 * potentially tricking a client into signing authorization for the wrong contract.
 * This could lead to unauthorized access or credential theft.
 *
 * The account argument is extracted from the authorization entry's
 * rootInvocation.function.contractFn.args[0] map under the "account" key.
 *
 * Example - Handle account mismatch:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, myContractId)
 * } catch (e: Sep45InvalidAccountException) {
 *     logger.error("Account mismatch!")
 *     logger.error("Expected: ${e.expected}")
 *     logger.error("Actual: ${e.actual}")
 *     // Do NOT proceed - challenge is for wrong account
 * }
 * ```
 *
 * @property expected The expected contract account ID (C...)
 * @property actual The actual account found in the authorization entry
 */
class Sep45InvalidAccountException(
    val expected: String,
    val actual: String
) : Sep45ChallengeValidationException(
    "Authorization entry account argument mismatch. Expected: $expected, but found: $actual"
)
