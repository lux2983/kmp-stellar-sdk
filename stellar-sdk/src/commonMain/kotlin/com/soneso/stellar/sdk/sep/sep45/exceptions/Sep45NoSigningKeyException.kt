// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the domain's stellar.toml is missing the SIGNING_KEY.
 *
 * SEP-45 requires the server's stellar.toml to contain a SIGNING_KEY field
 * that specifies the server's Ed25519 public key (G... address). This key
 * is used to:
 * - Sign the server's authorization entry in the challenge
 * - Allow clients to verify the challenge came from the legitimate server
 *
 * The SIGNING_KEY is critical for security:
 * - Clients verify the server's signature on the challenge entries
 * - Without verification, man-in-the-middle attacks would be possible
 * - The same SIGNING_KEY is used for both SEP-10 and SEP-45
 *
 * This exception is also thrown when requesting client domain authentication
 * and the client domain's stellar.toml is missing SIGNING_KEY.
 *
 * To resolve this error:
 * 1. Check the stellar.toml at https://{domain}/.well-known/stellar.toml
 * 2. Look for SIGNING_KEY field (should be a G... address)
 * 3. Contact the service provider if the signing key should be available
 *
 * Example - Handle missing signing key:
 * ```kotlin
 * try {
 *     val webAuth = WebAuthForContracts.fromDomain("example.com", Network.PUBLIC)
 * } catch (e: Sep45NoSigningKeyException) {
 *     println("Domain ${e.domain} does not have SIGNING_KEY configured")
 *     // Cannot proceed with authentication - contact service provider
 * }
 * ```
 *
 * @property domain The domain that is missing the signing key configuration
 */
class Sep45NoSigningKeyException(val domain: String) : Sep45Exception(
    "stellar.toml at domain '$domain' does not contain SIGNING_KEY"
)
