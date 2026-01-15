// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when client domain authentication is misconfigured.
 *
 * Client domain authentication in SEP-45 requires proper configuration:
 * - If `clientDomain` is provided, either `clientDomainAccountKeyPair` or
 *   `clientDomainSigningDelegate` must also be provided
 * - The client domain's stellar.toml must contain a SIGNING_KEY for verification
 * - Cannot use both keypair and delegate simultaneously
 *
 * Client domain verification allows services to identify which wallet or
 * application is facilitating the authentication. This is useful for:
 * - Analytics and tracking which wallets users prefer
 * - Applying wallet-specific policies or features
 * - Partner integrations and referral tracking
 *
 * Common causes of this error:
 * - Providing clientDomain without a signing mechanism
 * - Providing both keypair and delegate (use only one)
 * - Client domain stellar.toml missing SIGNING_KEY
 *
 * Example - Correct client domain configuration:
 * ```kotlin
 * // Option 1: Local signing with keypair
 * val token = webAuth.jwtToken(
 *     clientAccountId = contractId,
 *     signers = listOf(signerKeyPair),
 *     clientDomain = "mywallet.com",
 *     clientDomainAccountKeyPair = walletKeyPair
 * )
 *
 * // Option 2: Remote signing with delegate
 * val token = webAuth.jwtToken(
 *     clientAccountId = contractId,
 *     signers = listOf(signerKeyPair),
 *     clientDomain = "mywallet.com",
 *     clientDomainSigningDelegate = remoteSigningDelegate
 * )
 * ```
 *
 * @param message Description of the configuration error
 */
class Sep45MissingClientDomainException(message: String) : Sep45Exception(message)
