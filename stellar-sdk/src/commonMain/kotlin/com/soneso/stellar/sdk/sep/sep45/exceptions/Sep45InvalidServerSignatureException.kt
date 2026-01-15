// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the server's signature on an authorization entry is invalid.
 *
 * SEP-45 Security Requirement: The server's authorization entry MUST contain a valid
 * Ed25519 signature that can be verified against the server's signing key from stellar.toml.
 *
 * This is the MOST CRITICAL security check in SEP-45 validation. It protects against:
 * - Man-in-the-middle attacks (attacker intercepting and modifying challenges)
 * - Phishing attacks (fake server generating invalid challenges)
 * - Challenge tampering (modification of challenge after server signing)
 *
 * Signature verification process:
 * 1. Build HashIDPreimageSorobanAuthorization from the entry's values:
 *    - networkId (hash of network passphrase)
 *    - nonce (from entry's credentials)
 *    - signatureExpirationLedger (from entry's credentials)
 *    - invocation (rootInvocation)
 * 2. Hash the preimage with SHA-256
 * 3. Extract public_key and signature from credentials.addressCredentials.signature
 * 4. Verify public_key matches serverSigningKey
 * 5. Verify signature using Ed25519
 *
 * Security warning: NEVER sign authorization entries with an invalid server signature.
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
 * Example - Handle invalid server signature:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidServerSignatureException) {
 *     logger.error("CRITICAL SECURITY: Invalid server signature!")
 *     logger.error(e.message)
 *     // DO NOT proceed - possible MITM attack
 *     // Alert security team if in production
 * }
 * ```
 *
 * @param message Description of the signature verification failure
 */
class Sep45InvalidServerSignatureException(message: String) : Sep45ChallengeValidationException(
    "Server signature verification failed. $message DO NOT sign this challenge."
)
