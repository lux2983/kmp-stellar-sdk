// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing an indicative price quote.
 *
 * Provides non-binding price information for an asset pair. Unlike firm quotes,
 * indicative prices are not binding commitments and may change before execution.
 *
 * The [totalPrice] includes all fees, while [price] excludes fees. This allows
 * clients to show users both the nominal exchange rate and the effective rate
 * including all costs.
 *
 * Price relationships (as defined in SEP-38):
 * - totalPrice = sellAmount / buyAmount (total price including fees)
 * - If fee is in sellAsset: sellAmount - fee = price * buyAmount
 * - If fee is in buyAsset: sellAmount = price * (buyAmount + fee)
 *
 * Example - Getting indicative price for selling BRL:
 * ```kotlin
 * val priceResponse = sep38Service.price(
 *     context = "sep6",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     sellAmount = "542",
 *     sellDeliveryMethod = "PIX",
 *     countryCode = "BR"
 * )
 *
 * // Response:
 * // totalPrice: "5.42" (542/100)
 * // price: "5.00" ((542-42)/100)
 * // sellAmount: "542"
 * // buyAmount: "100"
 * // fee: { total: "42.00", asset: "iso4217:BRL" }
 *
 * println("Exchange rate: ${priceResponse.price} BRL per USDC")
 * println("Effective rate (with fees): ${priceResponse.totalPrice} BRL per USDC")
 * println("You'll receive: ${priceResponse.buyAmount} USDC")
 * println("Total cost: ${priceResponse.sellAmount} BRL (includes ${priceResponse.fee.total} BRL fee)")
 * ```
 *
 * Example - Getting indicative price for buying BRL:
 * ```kotlin
 * val priceResponse = sep38Service.price(
 *     context = "sep31",
 *     sellAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     buyAsset = "iso4217:BRL",
 *     buyAmount = "500",
 *     buyDeliveryMethod = "PIX",
 *     countryCode = "BR"
 * )
 *
 * // Response:
 * // totalPrice: "0.20" (100/500)
 * // price: "0.18" ((100-10)/500)
 * // sellAmount: "100"
 * // buyAmount: "500"
 * // fee: { total: "10.00", asset: "stellar:USDC:G..." }
 *
 * println("You need to send: ${priceResponse.sellAmount} USDC")
 * println("Recipient gets: ${priceResponse.buyAmount} BRL")
 * println("Fee: ${priceResponse.fee.total} USDC")
 * ```
 *
 * Example - Displaying fee breakdown:
 * ```kotlin
 * val priceResponse = sep38Service.price(
 *     context = "sep6",
 *     sellAsset = "stellar:USDC:G...",
 *     buyAsset = "iso4217:BRL",
 *     sellAmount = "100",
 *     buyDeliveryMethod = "PIX"
 * )
 *
 * println("Total fee: ${priceResponse.fee.total} ${priceResponse.fee.asset}")
 * priceResponse.fee.details?.forEach { detail ->
 *     println("  ${detail.name}: ${detail.amount}")
 *     detail.description?.let { desc -> println("    $desc") }
 * }
 * // Output:
 * // Total fee: 55.5556 iso4217:BRL
 * //   PIX fee: 55.5556
 * //     Fee charged in order to process the outgoing PIX transaction.
 * ```
 *
 * Example - Comparing rates from different delivery methods:
 * ```kotlin
 * val pixPrice = sep38Service.price(
 *     context = "sep6",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "stellar:USDC:G...",
 *     sellAmount = "500",
 *     sellDeliveryMethod = "PIX"
 * )
 *
 * val achPrice = sep38Service.price(
 *     context = "sep6",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "stellar:USDC:G...",
 *     sellAmount = "500",
 *     sellDeliveryMethod = "ACH"
 * )
 *
 * println("PIX: ${pixPrice.buyAmount} USDC (fee: ${pixPrice.fee.total})")
 * println("ACH: ${achPrice.buyAmount} USDC (fee: ${achPrice.fee.total})")
 * ```
 *
 * See also:
 * - [Sep38Fee] for fee structure and breakdown
 * - [Sep38QuoteResponse] for firm (binding) quotes
 * - [Sep38PricesResponse] for prices across multiple asset pairs
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property totalPrice Total price including all fees (buyAmount / sellAmount)
 * @property price Exchange rate excluding fees
 * @property sellAmount Amount of asset being sold
 * @property buyAmount Amount of asset being bought
 * @property fee Fee breakdown for the exchange
 */
@Serializable
data class Sep38PriceResponse(
    @SerialName("total_price")
    val totalPrice: String,

    @SerialName("price")
    val price: String,

    @SerialName("sell_amount")
    val sellAmount: String,

    @SerialName("buy_amount")
    val buyAmount: String,

    @SerialName("fee")
    val fee: Sep38Fee
)
