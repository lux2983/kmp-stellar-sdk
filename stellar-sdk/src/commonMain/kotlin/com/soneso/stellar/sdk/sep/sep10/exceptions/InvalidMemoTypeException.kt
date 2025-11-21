// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the challenge transaction has an invalid memo type.
 *
 * SEP-10 Security Requirement: If a memo is present, it MUST be of type MEMO_ID.
 *
 * Only MEMO_ID is allowed because:
 * - It represents an unsigned 64-bit integer for account sub-identifiers
 * - It cannot contain executable code or injection attacks
 * - It has a predictable, fixed format for validation
 *
 * MEMO_TEXT, MEMO_HASH, and MEMO_RETURN are not allowed in SEP-10 challenges
 * as they introduce unnecessary complexity and potential security risks.
 *
 * @param memoType The actual memo type found (e.g., "MEMO_TEXT", "MEMO_HASH")
 */
class InvalidMemoTypeException(memoType: String) :
    ChallengeValidationException(
        "Challenge transaction memo must be of type MEMO_ID if present, but found: $memoType. " +
                "Only MEMO_ID is allowed for SEP-10 authentication."
    )
