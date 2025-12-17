// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38.exceptions

/**
 * Exception thrown when a quote is not found (HTTP 404).
 *
 * Indicates that the requested quote ID does not exist or is no longer available.
 * This error typically occurs when:
 * - Quote ID does not exist
 * - Quote has expired and been deleted by the anchor
 * - Quote belongs to a different user account
 * - Quote was already consumed in a transaction
 * - Invalid quote ID format
 *
 * Recovery actions:
 * - Verify quote ID is correct
 * - Create a new quote if the previous one expired
 * - Ensure using quote ID from same authenticated session
 * - Check quote expiration time before use
 *
 * Example - Handle expired quote:
 * ```kotlin
 * suspend fun useQuote(
 *     quoteService: Sep38QuoteService,
 *     quoteId: String,
 *     jwt: String
 * ): Sep38QuoteResponse? {
 *     try {
 *         return quoteService.getQuote(quoteId, jwt)
 *     } catch (e: Sep38NotFoundException) {
 *         println("Quote not found: ${e.error}")
 *         println("The quote may have expired, please request a new one")
 *         return null
 *     }
 * }
 * ```
 *
 * Example - Request new quote on expiration:
 * ```kotlin
 * suspend fun getOrCreateQuote(
 *     quoteService: Sep38QuoteService,
 *     quoteId: String?,
 *     request: Sep38PostQuoteRequest,
 *     jwt: String
 * ): Sep38QuoteResponse {
 *     // Try to use existing quote
 *     if (quoteId != null) {
 *         try {
 *             return quoteService.getQuote(quoteId, jwt)
 *         } catch (e: Sep38NotFoundException) {
 *             println("Quote expired, creating new quote...")
 *             // Fall through to create new quote
 *         }
 *     }
 *
 *     // Create new quote
 *     return quoteService.postQuote(request, jwt)
 * }
 * ```
 *
 * Example - Check expiration before use:
 * ```kotlin
 * suspend fun getQuoteIfValid(
 *     quoteService: Sep38QuoteService,
 *     quoteId: String,
 *     jwt: String
 * ): Sep38QuoteResponse? {
 *     try {
 *         val quote = quoteService.getQuote(quoteId, jwt)
 *
 *         // Check if quote is still valid
 *         val now = Clock.System.now()
 *         if (quote.expiresAt < now) {
 *             println("Quote expired at ${quote.expiresAt}")
 *             return null
 *         }
 *
 *         return quote
 *     } catch (e: Sep38NotFoundException) {
 *         println("Quote not found: ${e.error}")
 *         return null
 *     }
 * }
 * ```
 *
 * Example - Cache quotes with expiration tracking:
 * ```kotlin
 * class QuoteCache {
 *     private val cache = mutableMapOf<String, CachedQuote>()
 *
 *     data class CachedQuote(
 *         val quote: Sep38QuoteResponse,
 *         val cachedAt: Instant
 *     )
 *
 *     suspend fun getQuote(
 *         quoteService: Sep38QuoteService,
 *         quoteId: String,
 *         jwt: String
 *     ): Sep38QuoteResponse? {
 *         // Check cache first
 *         val cached = cache[quoteId]
 *         if (cached != null) {
 *             val now = Clock.System.now()
 *             if (cached.quote.expiresAt > now) {
 *                 println("Using cached quote (expires at ${cached.quote.expiresAt})")
 *                 return cached.quote
 *             } else {
 *                 println("Cached quote expired, removing from cache")
 *                 cache.remove(quoteId)
 *             }
 *         }
 *
 *         // Fetch from server
 *         try {
 *             val quote = quoteService.getQuote(quoteId, jwt)
 *             cache[quoteId] = CachedQuote(quote, Clock.System.now())
 *             return quote
 *         } catch (e: Sep38NotFoundException) {
 *             println("Quote not found: ${e.error}")
 *             cache.remove(quoteId) // Remove if it was cached
 *             return null
 *         }
 *     }
 *
 *     fun clearExpired() {
 *         val now = Clock.System.now()
 *         cache.entries.removeIf { it.value.quote.expiresAt <= now }
 *     }
 * }
 * ```
 *
 * Example - Validate quote ID format:
 * ```kotlin
 * fun isValidQuoteId(quoteId: String): Boolean {
 *     // Most anchors use UUID format
 *     val uuidPattern = Regex(
 *         "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
 *     )
 *     return quoteId.matches(uuidPattern)
 * }
 *
 * suspend fun getQuoteWithValidation(
 *     quoteService: Sep38QuoteService,
 *     quoteId: String,
 *     jwt: String
 * ): Sep38QuoteResponse? {
 *     if (!isValidQuoteId(quoteId)) {
 *         println("Invalid quote ID format: $quoteId")
 *         return null
 *     }
 *
 *     try {
 *         return quoteService.getQuote(quoteId, jwt)
 *     } catch (e: Sep38NotFoundException) {
 *         println("Quote not found: ${e.error}")
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
 * @property error Error message from the anchor
 */
class Sep38NotFoundException(
    val error: String
) : Sep38Exception(
    message = "Quote not found (404). $error"
) {
    override fun toString(): String {
        return "SEP-38 not found - error: $error"
    }
}
