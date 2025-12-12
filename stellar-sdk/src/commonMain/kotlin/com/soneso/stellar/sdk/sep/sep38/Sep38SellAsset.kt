// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sell asset information with price and decimal precision.
 *
 * Represents a sell asset option returned from the GET /prices endpoint when
 * querying by buy asset and amount. Includes the indicative price and the number
 * of decimal places for amount precision. Used to display multiple exchange options
 * to users before they select a specific asset pair.
 *
 * The [price] is an indicative (non-binding) rate for one unit of the buy asset
 * in terms of this sell asset. The [decimals] field indicates how many decimal
 * places should be used when displaying or entering amounts for this asset.
 *
 * Example - USDC sell asset:
 * ```kotlin
 * val sellAsset = Sep38SellAsset(
 *     asset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     price = "5.42",
 *     decimals = 7
 * )
 * // Interpretation: Need to sell 5.42 USDC to buy 1 unit of buy asset
 * // Display amounts with up to 7 decimal places
 * ```
 *
 * Example - Fiat sell asset:
 * ```kotlin
 * val sellAsset = Sep38SellAsset(
 *     asset = "iso4217:BRL",
 *     price = "0.18",
 *     decimals = 2
 * )
 * // Interpretation: Need to sell 0.18 BRL to buy 1 unit of buy asset
 * // Display amounts with up to 2 decimal places (standard for fiat)
 * ```
 *
 * Example - Displaying prices from buy perspective:
 * ```kotlin
 * val pricesResponse = sep38Service.prices(
 *     buyAsset = "stellar:USDC:G...",
 *     buyAmount = "100"
 * )
 *
 * pricesResponse.sellAssets.forEach { sellAsset ->
 *     val formattedPrice = "%.${sellAsset.decimals}f".format(sellAsset.price.toDouble())
 *     println("Need to sell $formattedPrice ${sellAsset.asset} to buy 1 USDC")
 * }
 * ```
 *
 * See also:
 * - [Sep38PricesResponse] which contains the list of sell assets
 * - [Sep38BuyAsset] for the equivalent buy asset representation
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property asset Asset identifier in Asset Identification Format (e.g., "stellar:CODE:ISSUER", "iso4217:USD")
 * @property price Indicative price for one unit of the buy asset in terms of this sell asset
 * @property decimals Number of decimal places for amount precision when displaying this asset
 */
@Serializable
data class Sep38SellAsset(
    @SerialName("asset")
    val asset: String,

    @SerialName("price")
    val price: String,

    @SerialName("decimals")
    val decimals: Int
)
