// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when attempting to use a memo parameter with a muxed account.
 *
 * Muxed accounts (M... addresses) have their ID embedded in the address itself,
 * so they cannot be used with a separate memo parameter. This is a validation
 * error that prevents conflicting identification methods.
 *
 * Context:
 * - Muxed accounts (M...) encode the account ID and a 64-bit memo ID in the address
 * - Traditional accounts (G...) can use separate memo parameters for sub-accounts
 * - These two approaches are mutually exclusive
 *
 * Example - Correct usage:
 * ```kotlin
 * // Option 1: Use muxed account (no memo parameter)
 * val challenge = webAuth.getChallenge(
 *     clientAccountId = "MABC...XYZ"  // Muxed account (M...)
 * )
 *
 * // Option 2: Use traditional account with memo
 * val challenge = webAuth.getChallenge(
 *     clientAccountId = "GABC...XYZ",  // Traditional account (G...)
 *     memo = 12345
 * )
 * ```
 *
 * Example - Incorrect usage (throws this exception):
 * ```kotlin
 * // WRONG: Cannot combine M... address with memo parameter
 * val challenge = webAuth.getChallenge(
 *     clientAccountId = "MABC...XYZ",  // Muxed account
 *     memo = 12345  // ERROR: Redundant/conflicting identification
 * )
 * ```
 *
 * See also:
 * - [SEP-23: Muxed Accounts](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0023.md)
 * - [com.soneso.stellar.sdk.StrKey.isValidMed25519PublicKey] for muxed account validation
 *
 * @param message Description of the validation error
 */
class NoMemoForMuxedAccountsException(
    message: String = "Muxed accounts (M...) cannot be used with a separate memo parameter. " +
            "The memo ID is already embedded in the muxed account address."
) : WebAuthException(message)
