// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when a challenge has both a memo and a muxed account (M... address).
 *
 * SEP-10 Security Requirement: A transaction CANNOT have both a MEMO_ID and a
 * muxed account (M... address), as they serve the same purpose.
 *
 * Both mechanisms identify sub-accounts:
 * - MEMO_ID: Traditional approach using G... address + memo
 * - Muxed Account (M...): Modern approach encoding the ID in the address itself
 *
 * Having both creates ambiguity about which identifier should be used, potentially
 * allowing an attacker to exploit the confusion for account substitution attacks.
 *
 * Correct usage patterns:
 * - Use G... address + MEMO_ID for sub-accounts (traditional)
 * - Use M... address with no memo (modern, recommended)
 * - Use G... address with no memo (single account)
 *
 * @param muxedAccount The muxed account (M... address) found
 * @param memoValue The memo value that was also present
 */
class MemoWithMuxedAccountException(muxedAccount: String, memoValue: Long) :
    ChallengeValidationException(
        "Challenge transaction cannot have both a muxed account and a memo. " +
                "Found muxed account: $muxedAccount and memo: $memoValue. " +
                "Use either a muxed account (M...) OR a memo with a regular account (G...), not both."
    )
