// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when a client_domain operation has an incorrect source account.
 *
 * SEP-10 Security Requirement: If a "client_domain" ManageData operation is present,
 * its source account MUST be the account that will sign on behalf of the client domain.
 *
 * Client domain verification allows a client application (like a wallet) to prove
 * ownership of its domain to the authentication server. This enables:
 * - Server allowlists/denylists based on client application
 * - Enhanced security through mutual authentication
 * - Attribution of authentication requests to specific applications
 *
 * The verification process:
 * 1. Client requests challenge with clientDomain parameter
 * 2. Server adds "client_domain" operation with source = client domain's signing key
 * 3. Client fetches client domain's stellar.toml to get SIGNING_KEY
 * 4. Client signs with both user's key AND client domain's key
 * 5. Server verifies both signatures
 *
 * This check ensures the source account matches the account the client will
 * use for signing, preventing source account substitution attacks.
 *
 * @param expected The expected client domain signing account (from stellar.toml)
 * @param actual The actual source account found in the operation
 */
class InvalidClientDomainSourceException(expected: String?, actual: String?) :
    ChallengeValidationException(
        "Challenge transaction's client_domain operation source account is invalid. " +
                "Expected: $expected, but found: $actual. " +
                "The source must be the signing key from the client domain's stellar.toml."
    )
