// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fee structure for an exchange operation.
 *
 * Provides total fee amount and optional breakdown of individual fee components.
 * The [total] represents the complete fee charged by the anchor, while [details]
 * provides transparency about how the fee is calculated.
 *
 * Fees can be denominated in either the sell asset or buy asset, as indicated by
 * the [asset] property. This affects how the fee is applied in price calculations:
 * - If fee is in sell asset: sell_amount - fee = price * buy_amount
 * - If fee is in buy asset: sell_amount = price * (buy_amount + fee)
 *
 * Example - Simple fee (no breakdown):
 * ```kotlin
 * val fee = Sep38Fee(
 *     total = "10.00",
 *     asset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN"
 * )
 * ```
 *
 * Example - Detailed fee breakdown:
 * ```kotlin
 * val fee = Sep38Fee(
 *     total = "42.00",
 *     asset = "iso4217:BRL",
 *     details = listOf(
 *         Sep38FeeDetail("PIX fee", "12.00", "Fee charged in order to process the outgoing PIX transaction."),
 *         Sep38FeeDetail("Brazilian conciliation fee", "15.00", "Fee charged in order to process conciliation costs with intermediary banks."),
 *         Sep38FeeDetail("Service fee", "15.00")
 *     )
 * )
 * // sum(details.amount) = 12.00 + 15.00 + 15.00 = 42.00 = total
 * ```
 *
 * Example - Displaying fees to users:
 * ```kotlin
 * val price = sep38Service.price(
 *     context = "sep6",
 *     sellAsset = "iso4217:BRL",
 *     buyAsset = "stellar:USDC:G...",
 *     sellAmount = "542"
 * )
 *
 * println("Fee: ${price.fee.total} ${price.fee.asset}")
 * price.fee.details?.forEach { detail ->
 *     println("  ${detail.name}: ${detail.amount}")
 * }
 * ```
 *
 * Example - Fee validation:
 * ```kotlin
 * val quote = sep38Service.postQuote(request, jwtToken)
 *
 * // Verify fee breakdown sums to total
 * val detailSum = quote.fee.details?.sumOf { it.amount.toDouble() } ?: 0.0
 * val total = quote.fee.total.toDouble()
 * require((detailSum - total).absoluteValue < 0.01) { "Fee details don't sum to total" }
 * ```
 *
 * See also:
 * - [Sep38FeeDetail] for individual fee component structure
 * - [Sep38PriceResponse] which includes fee information for indicative prices
 * - [Sep38QuoteResponse] which includes fee information for firm quotes
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property total Total fee amount as a string decimal
 * @property asset Asset in which fee is denominated, using Asset Identification Format
 * @property details Optional breakdown of individual fee components (sum of amounts should equal total)
 */
@Serializable
data class Sep38Fee(
    @SerialName("total")
    val total: String,

    @SerialName("asset")
    val asset: String,

    @SerialName("details")
    val details: List<Sep38FeeDetail>? = null
)
