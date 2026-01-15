// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the home_domain argument doesn't match the expected domain.
 *
 * SEP-45 Security Requirement: The home_domain argument in the authorization entry
 * MUST match the expected home domain (either the server's home domain or the
 * domain explicitly requested by the client).
 *
 * This check is critical for security because:
 * - It ties the authentication to a specific service domain
 * - It prevents domain substitution attacks
 * - Clients know exactly which service they're authenticating with
 *
 * The home_domain argument is extracted from the authorization entry's
 * rootInvocation.function.contractFn.args[0] map under the "home_domain" key.
 *
 * Attack scenario prevented:
 * Without this check, a malicious domain could generate challenges that appear
 * to be from a trusted domain, enabling phishing attacks where users think
 * they're authenticating with a legitimate service.
 *
 * Example - Handle home domain mismatch:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidHomeDomainException) {
 *     logger.error("Home domain mismatch!")
 *     logger.error("Expected: ${e.expected}")
 *     logger.error("Actual: ${e.actual}")
 *     // Do NOT proceed - possible domain spoofing attack
 * }
 * ```
 *
 * @property expected The expected home domain
 * @property actual The actual home_domain found in the authorization entry
 */
class Sep45InvalidHomeDomainException(
    val expected: String,
    val actual: String
) : Sep45ChallengeValidationException(
    "Authorization entry home_domain argument mismatch. Expected: $expected, but found: $actual"
)
