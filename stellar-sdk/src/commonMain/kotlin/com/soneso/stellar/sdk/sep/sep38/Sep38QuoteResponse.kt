// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing a firm quote.
 *
 * Provides a binding commitment from the anchor to exchange assets at the
 * specified rate. The quote is valid until [expiresAt] and must be honored
 * by the anchor if the client delivers funds before expiration.
 *
 * Unlike indicative prices from GET /price, firm quotes reserve liquidity
 * and guarantee the exchange rate. The [id] can be referenced in SEP-6,
 * SEP-24, or SEP-31 transactions to execute the exchange.
 *
 * Price relationships (as defined in SEP-38):
 * - totalPrice = sellAmount / buyAmount (total price including fees)
 * - If fee is in sellAsset: sellAmount - fee = price * buyAmount
 * - If fee is in buyAsset: sellAmount = price * (buyAmount + fee)
 *
 * Example - Requesting a firm quote:
 * ```kotlin
 * val quoteRequest = Sep38QuoteRequest(
 *     context = "sep6",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     sellAmount = "542",
 *     sellDeliveryMethod = "PIX",
 *     countryCode = "BR"
 * )
 *
 * val quoteResponse = sep38Service.postQuote(quoteRequest, jwtToken)
 *
 * // Response:
 * // id: "de762cda-a193-4961-861e-57b31fed6eb3"
 * // expiresAt: "2021-04-30T07:42:23"
 * // totalPrice: "5.42"
 * // price: "5.00"
 * // sellAsset: "iso4217:BRL"
 * // sellAmount: "542"
 * // sellDeliveryMethod: "PIX"
 * // buyAsset: "stellar:USDC:G..."
 * // buyAmount: "100"
 * // fee: { total: "42.00", asset: "iso4217:BRL" }
 *
 * println("Quote ID: ${quoteResponse.id}")
 * println("Valid until: ${quoteResponse.expiresAt}")
 * println("You'll receive: ${quoteResponse.buyAmount} USDC")
 * println("For: ${quoteResponse.sellAmount} BRL (includes ${quoteResponse.fee.total} BRL fee)")
 * ```
 *
 * Example - Retrieving an existing quote:
 * ```kotlin
 * val quoteId = "de762cda-a193-4961-861e-57b31fed6eb3"
 * val quoteResponse = sep38Service.getQuote(quoteId, jwtToken)
 *
 * // Check if quote is still valid
 * val expiresAt = Instant.parse(quoteResponse.expiresAt)
 * val isExpired = Instant.now() > expiresAt
 *
 * if (isExpired) {
 *     println("Quote expired. Please request a new quote.")
 * } else {
 *     println("Quote valid for ${Duration.between(Instant.now(), expiresAt).toMinutes()} more minutes")
 *     proceedWithTransfer(quoteResponse)
 * }
 * ```
 *
 * Example - Using quote with SEP-6 deposit:
 * ```kotlin
 * // 1. Request firm quote
 * val quote = sep38Service.postQuote(quoteRequest, jwtToken)
 *
 * // 2. Initiate SEP-6 deposit with quote ID
 * val depositRequest = Sep6DepositRequest(
 *     assetCode = "USDC",
 *     account = stellarAccount,
 *     amount = quote.buyAmount,
 *     quoteId = quote.id
 * )
 *
 * val depositResponse = sep6Service.deposit(depositRequest, jwtToken)
 *
 * // 3. User delivers BRL via PIX before quote expires
 * // 4. Anchor delivers USDC at guaranteed rate
 * ```
 *
 * Example - Displaying quote details with fee breakdown:
 * ```kotlin
 * val quote = sep38Service.postQuote(quoteRequest, jwtToken)
 *
 * println("=== Quote Details ===")
 * println("ID: ${quote.id}")
 * println("Expires: ${quote.expiresAt}")
 * println()
 * println("Sell: ${quote.sellAmount} ${quote.sellAsset}")
 * quote.sellDeliveryMethod?.let { println("Delivery: $it") }
 * println()
 * println("Buy: ${quote.buyAmount} ${quote.buyAsset}")
 * quote.buyDeliveryMethod?.let { println("Delivery: $it") }
 * println()
 * println("Exchange rate: ${quote.price}")
 * println("Effective rate (with fees): ${quote.totalPrice}")
 * println()
 * println("Total fee: ${quote.fee.total} ${quote.fee.asset}")
 * quote.fee.details?.forEach { detail ->
 *     println("  ${detail.name}: ${detail.amount}")
 * }
 * ```
 *
 * Example - Handling quote expiration:
 * ```kotlin
 * val quote = sep38Service.postQuote(quoteRequest, jwtToken)
 *
 * // Set expiration reminder
 * val expiresAt = Instant.parse(quote.expiresAt)
 * val warningTime = expiresAt.minusSeconds(300) // 5 minutes before expiration
 *
 * scheduleNotification(warningTime) {
 *     println("Warning: Quote ${quote.id} expires in 5 minutes!")
 * }
 * ```
 *
 * See also:
 * - [Sep38QuoteRequest] for request parameters
 * - [Sep38Fee] for fee structure and breakdown
 * - [Sep38PriceResponse] for indicative (non-binding) prices
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property id Unique identifier for this quote (used in SEP-6/24/31 transactions)
 * @property expiresAt Expiration timestamp in ISO 8601 format (UTC)
 * @property totalPrice Total price including fees (buyAmount / sellAmount)
 * @property price Price excluding fees
 * @property sellAsset Asset being sold using Asset Identification Format
 * @property sellAmount Amount of sellAsset to exchange
 * @property sellDeliveryMethod Optional delivery method for sell asset (off-chain assets only)
 * @property buyAsset Asset being bought using Asset Identification Format
 * @property buyAmount Amount of buyAsset to receive
 * @property buyDeliveryMethod Optional delivery method for buy asset (off-chain assets only)
 * @property fee Fee breakdown for the exchange
 */
@Serializable
data class Sep38QuoteResponse(
    @SerialName("id")
    val id: String,

    @SerialName("expires_at")
    val expiresAt: String,

    @SerialName("total_price")
    val totalPrice: String,

    @SerialName("price")
    val price: String,

    @SerialName("sell_asset")
    val sellAsset: String,

    @SerialName("sell_amount")
    val sellAmount: String,

    @SerialName("sell_delivery_method")
    val sellDeliveryMethod: String? = null,

    @SerialName("buy_asset")
    val buyAsset: String,

    @SerialName("buy_amount")
    val buyAmount: String,

    @SerialName("buy_delivery_method")
    val buyDeliveryMethod: String? = null,

    @SerialName("fee")
    val fee: Sep38Fee
)
