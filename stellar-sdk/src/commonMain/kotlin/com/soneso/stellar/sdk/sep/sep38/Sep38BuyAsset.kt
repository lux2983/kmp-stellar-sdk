// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Buy asset information with price and decimal precision.
 *
 * Represents a buy asset option returned from the GET /prices endpoint, including
 * its indicative price and the number of decimal places for amount precision.
 * Used to display multiple exchange options to users before they select a specific
 * asset pair.
 *
 * The [price] is an indicative (non-binding) rate for one unit of this asset
 * in terms of the sell asset. The [decimals] field indicates how many decimal
 * places should be used when displaying or entering amounts for this asset.
 *
 * Example - USDC buy asset:
 * ```kotlin
 * val buyAsset = Sep38BuyAsset(
 *     asset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     price = "5.42",
 *     decimals = 7
 * )
 * // Interpretation: 1 USDC = 5.42 units of sell asset
 * // Display amounts with up to 7 decimal places
 * ```
 *
 * Example - Fiat buy asset:
 * ```kotlin
 * val buyAsset = Sep38BuyAsset(
 *     asset = "iso4217:BRL",
 *     price = "0.18",
 *     decimals = 2
 * )
 * // Interpretation: 1 BRL = 0.18 units of sell asset
 * // Display amounts with up to 2 decimal places (standard for fiat)
 * ```
 *
 * Example - Displaying prices to users:
 * ```kotlin
 * val pricesResponse = sep38Service.prices(
 *     sellAsset = "stellar:USDC:G...",
 *     sellAmount = "100"
 * )
 *
 * pricesResponse.buyAssets.forEach { buyAsset ->
 *     val formattedPrice = "%.${buyAsset.decimals}f".format(buyAsset.price.toDouble())
 *     println("1 ${buyAsset.asset} = $formattedPrice USDC")
 * }
 * ```
 *
 * See also:
 * - [Sep38PricesResponse] which contains the list of buy assets
 * - [Sep38SellAsset] for the equivalent sell asset representation
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property asset Asset identifier in Asset Identification Format (e.g., "stellar:CODE:ISSUER", "iso4217:USD")
 * @property price Indicative price for one unit of this asset in terms of the sell asset
 * @property decimals Number of decimal places for amount precision when displaying this asset
 */
@Serializable
data class Sep38BuyAsset(
    @SerialName("asset")
    val asset: String,

    @SerialName("price")
    val price: String,

    @SerialName("decimals")
    val decimals: Int
)
