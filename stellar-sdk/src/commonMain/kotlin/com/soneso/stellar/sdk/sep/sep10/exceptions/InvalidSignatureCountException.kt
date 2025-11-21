// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the challenge transaction doesn't have exactly one signature.
 *
 * SEP-10 Security Requirement: When validating an unsigned challenge from the server,
 * it MUST contain exactly 1 signature - the server's signature.
 *
 * This validation ensures:
 * - The server has signed the challenge (proving authenticity)
 * - No client signatures are present yet (preventing pre-signed challenges)
 * - No extra signatures from unknown parties
 *
 * Challenge signature lifecycle:
 * 1. Server generates challenge and signs it (1 signature)
 * 2. Client validates challenge has exactly 1 signature (this check)
 * 3. Client adds their signature(s) (1 + n signatures)
 * 4. Client submits to server with all signatures
 *
 * Attack scenarios prevented:
 * - 0 signatures: Unsigned challenge could be from anyone (no server authentication)
 * - 2+ signatures: Extra signatures could indicate tampering or pre-signed challenges
 *
 * Note: This validation applies to the challenge BEFORE client signing.
 * After signing, the transaction will have 1 + n signatures (server + client signers).
 *
 * @param signatureCount The actual number of signatures found
 */
class InvalidSignatureCountException(signatureCount: Int) :
    ChallengeValidationException(
        "Challenge transaction must have exactly 1 signature (the server's signature), " +
                "but found $signatureCount signature(s). " +
                "This validation ensures the challenge hasn't been tampered with."
    )
