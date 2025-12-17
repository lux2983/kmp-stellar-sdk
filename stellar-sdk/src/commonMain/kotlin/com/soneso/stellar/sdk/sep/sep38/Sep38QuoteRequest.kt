// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request for creating a firm quote.
 *
 * Specifies the parameters for a binding quote commitment from the anchor.
 * Either [sellAmount] or [buyAmount] must be provided, but not both.
 *
 * The anchor will reserve liquidity at the quoted rate until the quote expires.
 * The quote can be used with SEP-6, SEP-24, or SEP-31 transactions by referencing
 * the quote ID returned in the response.
 *
 * Example - Request quote for selling BRL:
 * ```kotlin
 * val request = Sep38QuoteRequest(
 *     context = "sep6",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     sellAmount = "542",
 *     sellDeliveryMethod = "PIX",
 *     countryCode = "BR"
 * )
 *
 * val quote = sep38Service.postQuote(request, jwtToken)
 * ```
 *
 * Example - Request quote for buying USDC:
 * ```kotlin
 * val request = Sep38QuoteRequest(
 *     context = "sep31",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     buyAmount = "100",
 *     sellDeliveryMethod = "PIX",
 *     countryCode = "BR"
 * )
 *
 * val quote = sep38Service.postQuote(request, jwtToken)
 * ```
 *
 * Example - Request quote with custom expiration:
 * ```kotlin
 * val request = Sep38QuoteRequest(
 *     context = "sep6",
 *     sellAsset = "stellar:USDC:G...",
 *     buyAsset = "iso4217:USD",
 *     sellAmount = "100",
 *     expireAfter = "2021-04-30T07:42:23Z" // ISO 8601 format
 * )
 *
 * val quote = sep38Service.postQuote(request, jwtToken)
 * // Anchor may provide expires_at on or after the requested expireAfter time
 * ```
 *
 * Example - Request quote with both delivery methods:
 * ```kotlin
 * val request = Sep38QuoteRequest(
 *     context = "sep6",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "iso4217:USD",
 *     sellAmount = "500",
 *     sellDeliveryMethod = "PIX",
 *     buyDeliveryMethod = "WIRE",
 *     countryCode = "BR"
 * )
 *
 * val quote = sep38Service.postQuote(request, jwtToken)
 * // Exchange BRL (via PIX) for USD (via wire transfer)
 * ```
 *
 * Example - Validation before request:
 * ```kotlin
 * fun createQuoteRequest(
 *     sellAsset: String,
 *     buyAsset: String,
 *     sellAmount: String? = null,
 *     buyAmount: String? = null
 * ): Sep38QuoteRequest {
 *     // Must provide exactly one amount
 *     require((sellAmount != null) xor (buyAmount != null)) {
 *         "Must provide either sellAmount or buyAmount, but not both"
 *     }
 *
 *     return Sep38QuoteRequest(
 *         context = "sep6",
 *         sellAsset = sellAsset,
 *         buyAsset = buyAsset,
 *         sellAmount = sellAmount,
 *         buyAmount = buyAmount
 *     )
 * }
 * ```
 *
 * Example - SEP-24 interactive flow context:
 * ```kotlin
 * val request = Sep38QuoteRequest(
 *     context = "sep24", // For interactive deposit/withdrawal
 *     sellAsset = "iso4217:USD",
 *     buyAsset = "stellar:USDC:G...",
 *     sellAmount = "100",
 *     buyDeliveryMethod = "bank_account"
 * )
 *
 * val quote = sep38Service.postQuote(request, jwtToken)
 * // Use quote in SEP-24 interactive flow
 * ```
 *
 * See also:
 * - [Sep38QuoteResponse] for the response structure
 * - [Sep38PriceResponse] for indicative (non-binding) prices
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property context Context for quote usage ("sep6", "sep24", or "sep31")
 * @property sellAsset Asset to sell using Asset Identification Format (e.g., "stellar:USDC:G...", "iso4217:BRL")
 * @property buyAsset Asset to buy using Asset Identification Format
 * @property sellAmount Optional amount of sellAsset to exchange (provide either this or buyAmount)
 * @property buyAmount Optional amount of buyAsset to receive (provide either this or sellAmount)
 * @property expireAfter Optional desired expiration time in ISO 8601 format (UTC)
 * @property sellDeliveryMethod Optional delivery method name from GET /info response (for off-chain sell assets)
 * @property buyDeliveryMethod Optional delivery method name from GET /info response (for off-chain buy assets)
 * @property countryCode Optional ISO 3166-1 alpha-2 or ISO 3166-2 country code
 */
@Serializable
data class Sep38QuoteRequest(
    @SerialName("context")
    val context: String,

    @SerialName("sell_asset")
    val sellAsset: String,

    @SerialName("buy_asset")
    val buyAsset: String,

    @SerialName("sell_amount")
    val sellAmount: String? = null,

    @SerialName("buy_amount")
    val buyAmount: String? = null,

    @SerialName("expire_after")
    val expireAfter: String? = null,

    @SerialName("sell_delivery_method")
    val sellDeliveryMethod: String? = null,

    @SerialName("buy_delivery_method")
    val buyDeliveryMethod: String? = null,

    @SerialName("country_code")
    val countryCode: String? = null
)
