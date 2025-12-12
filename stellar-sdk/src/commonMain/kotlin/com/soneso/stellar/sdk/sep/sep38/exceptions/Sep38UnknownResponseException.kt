// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38.exceptions

/**
 * Exception thrown for unexpected HTTP responses.
 *
 * Indicates that the anchor returned an HTTP status code that is not explicitly
 * handled by other exception types. This typically represents server errors (5xx)
 * or unexpected response codes. Common causes:
 * - Server errors (500, 502, 503, 504)
 * - Network connectivity issues
 * - Anchor service temporarily unavailable
 * - Rate limiting (429)
 * - Maintenance mode
 * - Unexpected 2xx responses with non-standard codes
 *
 * Recovery actions:
 * - Check anchor service status
 * - Retry request after exponential backoff delay
 * - Contact anchor support if problem persists
 * - Check for service announcements
 * - Verify network connectivity
 *
 * Example - Handle server errors with retry:
 * ```kotlin
 * suspend fun postQuoteWithRetry(
 *     quoteService: Sep38QuoteService,
 *     request: Sep38PostQuoteRequest,
 *     jwt: String,
 *     maxRetries: Int = 3
 * ): Sep38QuoteResponse? {
 *     var delay = 1000L // Start with 1 second
 *
 *     repeat(maxRetries) { attempt ->
 *         try {
 *             return quoteService.postQuote(request, jwt)
 *         } catch (e: Sep38UnknownResponseException) {
 *             println("Attempt ${attempt + 1} failed with status ${e.statusCode}: ${e.responseBody}")
 *
 *             // Only retry on server errors (5xx)
 *             if (e.statusCode in 500..599) {
 *                 if (attempt < maxRetries - 1) {
 *                     println("Retrying in ${delay}ms...")
 *                     delay(delay)
 *                     delay *= 2 // Exponential backoff
 *                 } else {
 *                     println("Max retries reached, giving up")
 *                 }
 *             } else {
 *                 println("Non-retryable status code: ${e.statusCode}")
 *                 return null
 *             }
 *         } catch (e: Exception) {
 *             println("Other error: ${e.message}")
 *             return null
 *         }
 *     }
 *
 *     return null
 * }
 * ```
 *
 * Example - Handle rate limiting:
 * ```kotlin
 * suspend fun handleRateLimiting(
 *     quoteService: Sep38QuoteService,
 *     request: Sep38PostQuoteRequest,
 *     jwt: String
 * ): Sep38QuoteResponse? {
 *     try {
 *         return quoteService.postQuote(request, jwt)
 *     } catch (e: Sep38UnknownResponseException) {
 *         when (e.statusCode) {
 *             429 -> {
 *                 println("Rate limited, waiting 60 seconds...")
 *                 delay(60_000)
 *
 *                 // Retry once after waiting
 *                 return try {
 *                     quoteService.postQuote(request, jwt)
 *                 } catch (retryError: Exception) {
 *                     println("Retry failed: ${retryError.message}")
 *                     null
 *                 }
 *             }
 *             in 500..599 -> {
 *                 println("Server error ${e.statusCode}, service may be down")
 *                 null
 *             }
 *             else -> {
 *                 println("Unexpected status ${e.statusCode}: ${e.responseBody}")
 *                 null
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * Example - Comprehensive error handling:
 * ```kotlin
 * sealed class QuoteResult {
 *     data class Success(val quote: Sep38QuoteResponse) : QuoteResult()
 *     data class Retryable(val delay: Long) : QuoteResult()
 *     data class Failed(val reason: String) : QuoteResult()
 * }
 *
 * suspend fun postQuoteWithErrorHandling(
 *     quoteService: Sep38QuoteService,
 *     request: Sep38PostQuoteRequest,
 *     jwt: String
 * ): QuoteResult {
 *     try {
 *         val quote = quoteService.postQuote(request, jwt)
 *         return QuoteResult.Success(quote)
 *     } catch (e: Sep38BadRequestException) {
 *         return QuoteResult.Failed("Invalid request: ${e.error}")
 *     } catch (e: Sep38PermissionDeniedException) {
 *         return QuoteResult.Failed("Permission denied: ${e.error}")
 *     } catch (e: Sep38UnknownResponseException) {
 *         return when (e.statusCode) {
 *             429 -> QuoteResult.Retryable(60_000) // Rate limited
 *             in 500..599 -> QuoteResult.Retryable(5_000) // Server error
 *             503 -> QuoteResult.Retryable(30_000) // Service unavailable
 *             else -> QuoteResult.Failed("Unexpected status ${e.statusCode}: ${e.responseBody}")
 *         }
 *     } catch (e: Exception) {
 *         return QuoteResult.Failed("Network error: ${e.message}")
 *     }
 * }
 * ```
 *
 * Example - Service health check:
 * ```kotlin
 * suspend fun checkServiceHealth(quoteService: Sep38QuoteService): Boolean {
 *     try {
 *         // Try to get info endpoint (doesn't require auth)
 *         quoteService.info()
 *         return true
 *     } catch (e: Sep38UnknownResponseException) {
 *         println("Service health check failed: ${e.statusCode}")
 *         return false
 *     } catch (e: Exception) {
 *         println("Service unreachable: ${e.message}")
 *         return false
 *     }
 * }
 *
 * suspend fun postQuoteWithHealthCheck(
 *     quoteService: Sep38QuoteService,
 *     request: Sep38PostQuoteRequest,
 *     jwt: String
 * ): Sep38QuoteResponse? {
 *     // Check service health first
 *     if (!checkServiceHealth(quoteService)) {
 *         println("Service is unhealthy, skipping request")
 *         return null
 *     }
 *
 *     try {
 *         return quoteService.postQuote(request, jwt)
 *     } catch (e: Sep38UnknownResponseException) {
 *         println("Unexpected response: ${e.statusCode} - ${e.responseBody}")
 *         return null
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep38Exception] base class
 * - [Sep38QuoteService] for quote API operations
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property statusCode HTTP status code returned
 * @property responseBody Raw response body for debugging
 */
class Sep38UnknownResponseException(
    val statusCode: Int,
    val responseBody: String
) : Sep38Exception(
    message = "Unknown response (HTTP $statusCode). Response body: $responseBody"
) {
    override fun toString(): String {
        return "SEP-38 unknown response - status: $statusCode, body: $responseBody"
    }
}
