// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06.exceptions

/**
 * Exception thrown when SEP-6 authentication is required but missing or invalid.
 *
 * Indicates that the request requires a valid SEP-10 JWT token that is either
 * missing, expired, or invalid. All SEP-6 endpoints except /info require
 * authentication. This error typically occurs when:
 * - JWT token is not provided in the Authorization header
 * - JWT token has expired
 * - JWT token is malformed or tampered with
 * - JWT token signature is invalid
 * - Token was issued for a different anchor
 *
 * Recovery actions:
 * - Authenticate via SEP-10 WebAuth to obtain a fresh JWT token
 * - Verify the token is being sent in the Authorization header
 * - Check that the token was issued by the correct anchor
 *
 * Example - Handle authentication failure:
 * ```kotlin
 * suspend fun initiateDeposit(
 *     sep06Service: Sep06Service,
 *     webAuth: WebAuth,
 *     assetCode: String,
 *     accountId: String,
 *     keyPair: KeyPair
 * ): Sep06DepositResponse? {
 *     var jwt = getCachedJwt()
 *
 *     try {
 *         return sep06Service.deposit(assetCode, jwt)
 *     } catch (e: Sep06AuthenticationRequiredException) {
 *         println("Authentication required, re-authenticating...")
 *
 *         // Obtain fresh token via SEP-10
 *         val authToken = webAuth.jwtToken(
 *             clientAccountId = accountId,
 *             signers = listOf(keyPair)
 *         )
 *         jwt = authToken.token
 *         cacheJwt(jwt)
 *
 *         // Retry with new token
 *         return sep06Service.deposit(assetCode, jwt)
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep06Exception] base class
 * - [WebAuth] for SEP-10 authentication
 * - [SEP-0010 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md)
 * - [SEP-0006 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md)
 */
class Sep06AuthenticationRequiredException : Sep06Exception(
    message = "Authentication required"
) {
    override fun toString(): String {
        return "SEP-06 authentication required"
    }
}
