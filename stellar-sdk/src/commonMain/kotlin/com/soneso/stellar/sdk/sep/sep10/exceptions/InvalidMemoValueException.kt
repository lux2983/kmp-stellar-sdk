// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the challenge transaction memo value doesn't match expected value.
 *
 * SEP-10 Requirement: If the client provided a memo when requesting the challenge,
 * the returned challenge transaction MUST contain that same memo value.
 *
 * This validation ensures:
 * - The server correctly preserved the requested memo
 * - The challenge is for the correct sub-account
 * - Protection against memo substitution attacks
 *
 * Memo values are used to identify sub-accounts in scenarios where multiple
 * users share a single Stellar account (common for custodial services).
 *
 * @param expected The memo value the client requested
 * @param actual The memo value found in the challenge transaction
 */
class InvalidMemoValueException(expected: Long, actual: Long?) :
    ChallengeValidationException(
        "Challenge transaction memo value must match the requested value. " +
                "Expected: $expected, but found: $actual"
    )
