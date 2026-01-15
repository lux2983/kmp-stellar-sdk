// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the nonce argument is missing or inconsistent.
 *
 * SEP-45 Security Requirement: Each authorization entry MUST contain a nonce
 * argument, and the nonce MUST be consistent across all entries in the challenge.
 *
 * The nonce serves critical security purposes:
 * - Prevents replay attacks (each challenge is unique)
 * - Links all authorization entries in a challenge together
 * - Ensures entries cannot be mixed from different challenges
 *
 * Validation rules:
 * - Every authorization entry must have a "nonce" argument
 * - The nonce must be a non-empty string
 * - All entries in a challenge must have the same nonce value
 *
 * Common causes of this error:
 * - Server bug generating challenges without nonce
 * - Entries from different challenges mixed together
 * - Corrupted or tampered authorization entries
 *
 * Attack scenario prevented:
 * Without nonce validation, an attacker could:
 * - Replay old authorization entries
 * - Mix entries from different challenges
 * - Reuse captured authentication requests
 *
 * Example - Handle nonce issues:
 * ```kotlin
 * try {
 *     webAuth.validateChallenge(authEntries, accountId)
 * } catch (e: Sep45InvalidNonceException) {
 *     logger.error("Nonce validation failed: ${e.message}")
 *     // Request a fresh challenge
 * }
 * ```
 *
 * @param message Description of the nonce issue
 */
class Sep45InvalidNonceException(message: String) : Sep45ChallengeValidationException(
    "Authorization entry nonce validation failed. $message"
)
