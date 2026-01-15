// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the domain's stellar.toml is missing the WEB_AUTH_CONTRACT_ID.
 *
 * SEP-45 requires the server's stellar.toml to contain a WEB_AUTH_CONTRACT_ID
 * field that specifies the contract address (C...) used for authentication.
 * This contract implements the `web_auth_verify` function that validates
 * authentication requests.
 *
 * The WEB_AUTH_CONTRACT_ID is critical for security validation:
 * - Clients verify the challenge targets this specific contract
 * - Prevents attacks where malicious servers direct auth to wrong contracts
 * - Ensures the authentication flow uses the intended contract implementation
 *
 * To resolve this error:
 * 1. Check the stellar.toml at https://{domain}/.well-known/stellar.toml
 * 2. Look for WEB_AUTH_CONTRACT_ID field (should be a C... address)
 * 3. Verify the WEB_AUTH_FOR_CONTRACTS_ENDPOINT is also present
 * 4. Contact the service provider if the contract ID should be available
 *
 * Example - Handle missing contract ID:
 * ```kotlin
 * try {
 *     val webAuth = WebAuthForContracts.fromDomain("example.com", Network.PUBLIC)
 * } catch (e: Sep45NoContractIdException) {
 *     println("Domain ${e.domain} does not have WEB_AUTH_CONTRACT_ID configured")
 *     // Contact service provider or check configuration
 * }
 * ```
 *
 * @property domain The domain that is missing the contract ID configuration
 */
class Sep45NoContractIdException(val domain: String) : Sep45Exception(
    "stellar.toml at domain '$domain' does not contain WEB_AUTH_CONTRACT_ID"
)
