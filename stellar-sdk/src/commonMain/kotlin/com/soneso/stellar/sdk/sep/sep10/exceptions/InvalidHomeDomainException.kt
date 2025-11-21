// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the first operation's data key doesn't match the expected home domain format.
 *
 * SEP-10 Security Requirement: The first ManageData operation MUST have a data key
 * in the format "{domain} auth" where {domain} is the server's home domain.
 *
 * This requirement ensures:
 * - The challenge is tied to the specific service domain
 * - Protection against domain substitution attacks
 * - Clients know which service they're authenticating with
 *
 * The data key format "{domain} auth" clearly identifies which domain is requesting
 * authentication. For example:
 * - "example.com auth" for authentication with example.com
 * - "anchor.stellar.org auth" for authentication with anchor.stellar.org
 *
 * Attack scenario prevented:
 * Without this check, a malicious domain could generate challenges that appear
 * to be from a trusted domain, enabling phishing attacks.
 *
 * The data value is typically a base64-encoded random nonce to prevent replay attacks.
 *
 * @param expected The expected home domain
 * @param actual The actual data key found in the first operation
 */
class InvalidHomeDomainException(expected: String, actual: String?) :
    ChallengeValidationException(
        "Challenge transaction's first operation data key must be '{domain} auth'. " +
                "Expected: '$expected auth', but found: '$actual'"
    )
