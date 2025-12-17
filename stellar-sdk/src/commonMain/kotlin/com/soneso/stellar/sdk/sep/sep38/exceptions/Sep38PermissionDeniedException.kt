// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38.exceptions

/**
 * Exception thrown when authentication fails (HTTP 403).
 *
 * Indicates that the JWT token is invalid, expired, missing, or that the authenticated
 * user lacks necessary permissions. Authentication is required for firm quote operations
 * (postQuote, getQuote). This error typically occurs when:
 * - JWT token has expired
 * - JWT token is malformed or tampered with
 * - JWT token signature is invalid
 * - Token was issued for a different account
 * - SEP-10 authentication was not performed
 * - User lacks required KYC status via SEP-12
 *
 * Recovery actions:
 * - Re-authenticate via SEP-10 WebAuth to obtain a fresh JWT token
 * - Verify the token is being sent in the correct format
 * - Check that the token matches the account being accessed
 * - Ensure user has completed required KYC verification
 *
 * Example - Handle expired token with retry:
 * ```kotlin
 * suspend fun postQuoteWithRetry(
 *     quoteService: Sep38QuoteService,
 *     webAuth: WebAuth,
 *     request: Sep38PostQuoteRequest,
 *     accountId: String,
 *     keyPair: KeyPair
 * ): Sep38QuoteResponse? {
 *     var jwt = getCurrentJwt() // Get cached token
 *
 *     try {
 *         return quoteService.postQuote(request, jwt)
 *     } catch (e: Sep38PermissionDeniedException) {
 *         println("Token expired or invalid, re-authenticating...")
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
 *         return try {
 *             quoteService.postQuote(request, jwt)
 *         } catch (e: Sep38PermissionDeniedException) {
 *             println("Permission denied after re-authentication: ${e.error}")
 *             null
 *         }
 *     }
 * }
 * ```
 *
 * Example - Proactive token refresh:
 * ```kotlin
 * class Sep38Client(
 *     private val quoteService: Sep38QuoteService,
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
 *     suspend fun postQuote(
 *         request: Sep38PostQuoteRequest,
 *         accountId: String,
 *         keyPair: KeyPair
 *     ): Sep38QuoteResponse {
 *         val jwt = ensureValidToken(accountId, keyPair)
 *         return quoteService.postQuote(request, jwt)
 *     }
 *
 *     suspend fun getQuote(
 *         quoteId: String,
 *         accountId: String,
 *         keyPair: KeyPair
 *     ): Sep38QuoteResponse {
 *         val jwt = ensureValidToken(accountId, keyPair)
 *         return quoteService.getQuote(quoteId, jwt)
 *     }
 * }
 * ```
 *
 * Example - Handle KYC requirements:
 * ```kotlin
 * suspend fun postQuoteWithKYCCheck(
 *     quoteService: Sep38QuoteService,
 *     kycService: KYCService,
 *     request: Sep38PostQuoteRequest,
 *     jwt: String,
 *     accountId: String
 * ): Sep38QuoteResponse? {
 *     try {
 *         return quoteService.postQuote(request, jwt)
 *     } catch (e: Sep38PermissionDeniedException) {
 *         println("Permission denied: ${e.error}")
 *
 *         // Check KYC status
 *         try {
 *             val customerInfo = kycService.getCustomerInfo(
 *                 GetCustomerInfoRequest(jwt = jwt, account = accountId)
 *             )
 *
 *             if (customerInfo.status != "ACCEPTED") {
 *                 println("KYC not approved. Status: ${customerInfo.status}")
 *                 println("Please complete KYC verification first")
 *                 return null
 *             }
 *         } catch (kycError: Exception) {
 *             println("Failed to check KYC status: ${kycError.message}")
 *         }
 *
 *         return null
 *     }
 * }
 * ```
 *
 * Example - Check token validity before use:
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
 * suspend fun getQuote(
 *     quoteService: Sep38QuoteService,
 *     quoteId: String,
 *     jwt: String
 * ): Sep38QuoteResponse? {
 *     if (!isTokenValid(jwt)) {
 *         println("Token expired, please re-authenticate")
 *         return null
 *     }
 *
 *     try {
 *         return quoteService.getQuote(quoteId, jwt)
 *     } catch (e: Sep38PermissionDeniedException) {
 *         println("Permission denied: ${e.error}")
 *         return null
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep38Exception] base class
 * - [WebAuth] for SEP-10 authentication
 * - [AuthToken] for parsing JWT tokens
 * - [SEP-0010 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md)
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property error Error message from the anchor
 */
class Sep38PermissionDeniedException(
    val error: String
) : Sep38Exception(
    message = "Permission denied (403). $error"
) {
    override fun toString(): String {
        return "SEP-38 permission denied - error: $error"
    }
}
