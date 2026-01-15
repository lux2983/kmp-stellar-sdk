// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the web_auth_domain argument doesn't match the auth endpoint host.
 *
 * SEP-45 Security Requirement: The web_auth_domain argument in the authorization entry
 * MUST match the host (and port if non-standard) of the authentication endpoint URL.
 *
 * This check provides additional protection against URL confusion attacks:
 * - It explicitly declares which domain is performing the authentication
 * - It must match the host of the WEB_AUTH_FOR_CONTRACTS_ENDPOINT
 * - It prevents authentication servers from impersonating other domains
 *
 * Port handling:
 * - Standard ports (80, 443) are not included in the expected value
 * - Non-standard ports must be included (e.g., "localhost:8080")
 *
 * Examples:
 * - Endpoint "https://api.example.com/auth" -> web_auth_domain = "api.example.com"
 * - Endpoint "http://localhost:8080/auth" -> web_auth_domain = "localhost:8080"
 *
 * The web_auth_domain argument is extracted from the authorization entry's
 * rootInvocation.function.contractFn.args[0] map under the "web_auth_domain" key.
 *
 * Attack scenario prevented:
 * Without this check, a compromised or malicious server could serve challenges
 * claiming to be from a different domain, enabling sophisticated phishing attacks.
 *
 * Example - Handle web auth domain mismatch:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidWebAuthDomainException) {
 *     logger.error("Web auth domain mismatch!")
 *     logger.error("Expected: ${e.expected}")
 *     logger.error("Actual: ${e.actual}")
 *     // Do NOT proceed - possible URL confusion attack
 * }
 * ```
 *
 * @property expected The expected web auth domain (from auth endpoint host)
 * @property actual The actual web_auth_domain found in the authorization entry
 */
class Sep45InvalidWebAuthDomainException(
    val expected: String,
    val actual: String
) : Sep45ChallengeValidationException(
    "Authorization entry web_auth_domain argument mismatch. Expected: $expected, but found: $actual"
)
