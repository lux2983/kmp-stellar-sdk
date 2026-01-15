// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the authorization entry targets a different contract than expected.
 *
 * SEP-45 Security Requirement: The authorization entry's contract address MUST match
 * the WEB_AUTH_CONTRACT_ID from the server's stellar.toml.
 *
 * This check is critical for security because:
 * - It ensures you're authenticating with the correct contract
 * - It prevents contract substitution attacks where a malicious server
 *   directs authentication to a different contract
 * - The WEB_AUTH_CONTRACT_ID identifies the trusted authentication contract
 *
 * Attack scenario prevented:
 * Without this check, a compromised server could return authorization entries
 * targeting a malicious contract that could steal credentials or perform
 * unauthorized actions.
 *
 * The contract address is extracted from the authorization entry's
 * rootInvocation.function.contractFn.contractAddress field.
 *
 * Example - Handle contract address mismatch:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidContractAddressException) {
 *     logger.error("Contract address mismatch!")
 *     logger.error("Expected: ${e.expected}")
 *     logger.error("Actual: ${e.actual}")
 *     // Do NOT proceed - possible attack
 * }
 * ```
 *
 * @property expected The expected contract address (C...) from WEB_AUTH_CONTRACT_ID
 * @property actual The actual contract address found in the authorization entry
 */
class Sep45InvalidContractAddressException(
    val expected: String,
    val actual: String
) : Sep45ChallengeValidationException(
    "Authorization entry targets wrong contract. Expected: $expected, but found: $actual"
)
