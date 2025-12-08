// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12.exceptions

/**
 * Exception thrown when authentication fails (HTTP 401).
 *
 * Indicates that the JWT token is invalid, expired, or missing. Authentication
 * is required for all SEP-12 KYC operations. This error typically occurs when:
 * - JWT token has expired
 * - JWT token is malformed or tampered with
 * - JWT token signature is invalid
 * - Token was issued for a different account
 * - SEP-10 authentication was not performed
 *
 * Recovery actions:
 * - Re-authenticate via SEP-10 WebAuth to obtain a fresh JWT token
 * - Verify the token is being sent in the correct format
 * - Check that the token matches the account being accessed
 *
 * Example - Handle expired token:
 * ```kotlin
 * suspend fun getCustomerInfoWithRetry(
 *     kycService: KYCService,
 *     webAuth: WebAuth,
 *     accountId: String,
 *     keyPair: KeyPair
 * ): GetCustomerInfoResponse {
 *     var jwt = getCurrentJwt() // Get cached token
 *
 *     try {
 *         return kycService.getCustomerInfo(
 *             GetCustomerInfoRequest(jwt = jwt, account = accountId)
 *         )
 *     } catch (e: UnauthorizedException) {
 *         println("Token expired, re-authenticating...")
 *
 *         // Re-authenticate to get fresh token
 *         val authToken = webAuth.jwtToken(
 *             clientAccountId = accountId,
 *             signers = listOf(keyPair)
 *         )
 *         jwt = authToken.token
 *         saveJwt(jwt) // Cache new token
 *
 *         // Retry with new token
 *         return kycService.getCustomerInfo(
 *             GetCustomerInfoRequest(jwt = jwt, account = accountId)
 *         )
 *     }
 * }
 * ```
 *
 * Example - Check token before use:
 * ```kotlin
 * fun isTokenValid(token: String): Boolean {
 *     return try {
 *         val authToken = AuthToken.parse(token)
 *         val now = Clock.System.now().toEpochMilliseconds() / 1000
 *         authToken.exp > now
 *     } catch (e: Exception) {
 *         false
 *     }
 * }
 *
 * suspend fun getCustomerInfo(request: GetCustomerInfoRequest) {
 *     if (!isTokenValid(request.jwt)) {
 *         println("Token expired, please re-authenticate")
 *         throw UnauthorizedException()
 *     }
 *
 *     try {
 *         kycService.getCustomerInfo(request)
 *     } catch (e: UnauthorizedException) {
 *         println("Authentication failed, check token")
 *     }
 * }
 * ```
 *
 * Example - Refresh token proactively:
 * ```kotlin
 * class KYCClient(
 *     private val kycService: KYCService,
 *     private val webAuth: WebAuth
 * ) {
 *     private var currentToken: String? = null
 *     private var tokenExpiry: Long = 0
 *
 *     suspend fun ensureValidToken(accountId: String, keyPair: KeyPair): String {
 *         val now = Clock.System.now().toEpochMilliseconds() / 1000
 *
 *         // Refresh if token expires in less than 5 minutes
 *         if (currentToken == null || tokenExpiry - now < 300) {
 *             val authToken = webAuth.jwtToken(
 *                 clientAccountId = accountId,
 *                 signers = listOf(keyPair)
 *             )
 *             currentToken = authToken.token
 *             tokenExpiry = authToken.exp
 *         }
 *
 *         return currentToken!!
 *     }
 *
 *     suspend fun getCustomerInfo(accountId: String, keyPair: KeyPair) {
 *         val jwt = ensureValidToken(accountId, keyPair)
 *         return kycService.getCustomerInfo(
 *             GetCustomerInfoRequest(jwt = jwt, account = accountId)
 *         )
 *     }
 * }
 * ```
 *
 * See also:
 * - [KYCException] base class
 * - [WebAuth] for SEP-10 authentication
 * - [AuthToken] for parsing JWT tokens
 * - [SEP-0010 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md)
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 */
class UnauthorizedException : KYCException(
    message = "Authentication failed (401 Unauthorized). " +
            "JWT token is invalid, expired, or missing. " +
            "Please re-authenticate via SEP-10 WebAuth to obtain a fresh token."
)
