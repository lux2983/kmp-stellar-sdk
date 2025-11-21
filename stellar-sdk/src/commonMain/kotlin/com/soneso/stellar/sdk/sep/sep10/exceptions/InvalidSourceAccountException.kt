// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the first operation's source account doesn't match the client account.
 *
 * SEP-10 Security Requirement: The first ManageData operation MUST have its
 * source account set to the client's account ID (the account being authenticated).
 *
 * This validation ensures:
 * - The challenge is specifically for the correct account
 * - Protection against account substitution attacks
 * - The server generated the challenge for the requested client
 *
 * The first operation contains the home domain signature and MUST be tied to
 * the client's account to prove ownership of that specific account.
 *
 * Attack scenario prevented:
 * Without this check, a malicious server could generate a challenge for a
 * different account, potentially allowing authentication as the wrong user.
 *
 * @param expected The client account ID that was requested
 * @param actual The source account found in the first operation
 */
class InvalidSourceAccountException(expected: String, actual: String?) :
    ChallengeValidationException(
        "Challenge transaction's first operation source account must be the client account. " +
                "Expected: $expected, but found: $actual"
    )
