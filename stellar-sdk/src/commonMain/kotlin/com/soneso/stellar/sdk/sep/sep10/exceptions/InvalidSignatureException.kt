// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the server's signature on the challenge transaction is invalid.
 *
 * SEP-10 Security Requirement: The server's signature MUST be cryptographically
 * valid when verified against the server's signing key from stellar.toml.
 *
 * This is the MOST CRITICAL security check in SEP-10 validation. It protects against:
 * - Man-in-the-middle attacks (attacker intercepting and modifying challenges)
 * - Phishing attacks (fake server generating invalid challenges)
 * - Challenge tampering (modification of challenge after server signing)
 *
 * Signature verification process:
 * 1. Compute transaction hash for the network
 * 2. Extract signature bytes from the challenge
 * 3. Verify signature using server's public key (from stellar.toml SIGNING_KEY)
 * 4. If verification fails, throw this exception
 *
 * Security warning: NEVER sign a challenge with an invalid server signature.
 * An invalid signature means:
 * - The challenge did not come from the legitimate server
 * - The challenge may have been modified in transit
 * - You may be communicating with an attacker
 *
 * If you receive this error:
 * 1. Verify you're connecting to the correct domain (check HTTPS certificate)
 * 2. Verify the server signing key matches the stellar.toml
 * 3. Report the incident if it persists (possible security breach)
 *
 * @param serverSigningKey The server's public key used for verification
 */
class InvalidSignatureException(serverSigningKey: String) :
    ChallengeValidationException(
        "Challenge transaction signature is invalid. " +
                "The signature does not verify against the server signing key: $serverSigningKey. " +
                "This indicates a potential man-in-the-middle attack or server misconfiguration. " +
                "DO NOT sign this challenge."
    )
