// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when a web_auth_domain operation has an invalid value.
 *
 * SEP-10 Security Requirement: If a "web_auth_domain" ManageData operation is present,
 * its value MUST match the host part of the authentication endpoint URL.
 *
 * The web_auth_domain operation provides additional protection against URL confusion attacks:
 * - It explicitly declares which domain is performing the authentication
 * - It must match the host of the WEB_AUTH_ENDPOINT from stellar.toml
 * - It prevents authentication servers from impersonating other domains
 *
 * Example:
 * - If WEB_AUTH_ENDPOINT is "https://api.example.com/auth"
 * - Then web_auth_domain value MUST be "api.example.com"
 *
 * Attack scenario prevented:
 * Without this check, a compromised or malicious server could serve challenges
 * claiming to be from a different domain, enabling sophisticated phishing attacks.
 *
 * The web_auth_domain operation is optional but recommended. When present, it MUST
 * be validated to ensure it matches the authentication endpoint's host.
 *
 * @param expected The expected domain (from auth endpoint host)
 * @param actual The actual web_auth_domain value found in the operation
 */
class InvalidWebAuthDomainException(expected: String, actual: String?) :
    ChallengeValidationException(
        "Challenge transaction's web_auth_domain operation value must match the auth endpoint host. " +
                "Expected: $expected, but found: $actual"
    )
