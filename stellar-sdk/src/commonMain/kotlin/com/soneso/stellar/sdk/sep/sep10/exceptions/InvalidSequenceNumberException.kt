// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the challenge transaction has an invalid sequence number.
 *
 * SEP-10 Security Requirement: The sequence number MUST be exactly 0.
 *
 * This requirement prevents transaction replay attacks. A sequence number of 0
 * ensures the transaction can never be submitted to the Stellar network, as all
 * real accounts have sequence numbers starting at 0 and incrementing with each
 * operation.
 *
 * Attack scenario prevented:
 * If a challenge used a real sequence number, an attacker who intercepts the
 * signed challenge could potentially submit it to the network, executing
 * unintended operations on the user's account.
 *
 * @param sequenceNumber The actual sequence number found in the transaction
 */
class InvalidSequenceNumberException(sequenceNumber: Long) :
    ChallengeValidationException(
        "Challenge transaction sequence number must be 0, but found: $sequenceNumber. " +
                "This is a security requirement to prevent replay attacks."
    )
