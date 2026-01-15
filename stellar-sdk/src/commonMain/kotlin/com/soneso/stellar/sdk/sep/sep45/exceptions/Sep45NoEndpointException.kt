// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the domain's stellar.toml is missing the WEB_AUTH_FOR_CONTRACTS_ENDPOINT.
 *
 * SEP-45 requires the server's stellar.toml to contain a WEB_AUTH_FOR_CONTRACTS_ENDPOINT
 * field that specifies the URL for contract authentication. This endpoint is different
 * from SEP-10's WEB_AUTH_ENDPOINT which handles traditional account authentication.
 *
 * To resolve this error:
 * 1. Verify the domain supports SEP-45 contract authentication
 * 2. Check the stellar.toml at https://{domain}/.well-known/stellar.toml
 * 3. Look for WEB_AUTH_FOR_CONTRACTS_ENDPOINT field
 * 4. Contact the service provider if the endpoint should be available
 *
 * Note: SEP-45 support is optional. Many services only implement SEP-10 for
 * traditional accounts. If you need to authenticate a contract account (C...),
 * you must use a service that explicitly supports SEP-45.
 *
 * Example - Handle missing endpoint:
 * ```kotlin
 * try {
 *     val webAuth = WebAuthForContracts.fromDomain("example.com", Network.PUBLIC)
 * } catch (e: Sep45NoEndpointException) {
 *     println("Domain ${e.domain} does not support SEP-45 contract authentication")
 *     // Fall back to alternative authentication or inform user
 * }
 * ```
 *
 * @property domain The domain that is missing the endpoint configuration
 */
class Sep45NoEndpointException(val domain: String) : Sep45Exception(
    "stellar.toml at domain '$domain' does not contain WEB_AUTH_FOR_CONTRACTS_ENDPOINT"
)
