// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing indicative prices for multiple assets.
 *
 * Returns a list of available buy assets OR sell assets with their indicative prices.
 * Used to display multiple exchange options to users before they select a specific
 * asset pair for a firm quote.
 *
 * The response contains either [buyAssets] (when selling a specific asset) or
 * [sellAssets] (when buying a specific asset), but never both in the same response.
 *
 * Indicative prices are non-binding estimates and may change. For binding commitments,
 * use the POST /quote endpoint to request a firm quote.
 *
 * Example - Getting buy options (selling USDC):
 * ```kotlin
 * val pricesResponse = sep38Service.prices(
 *     sellAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     sellAmount = "100"
 * )
 *
 * // Response contains buyAssets (what you can buy with 100 USDC)
 * pricesResponse.buyAssets?.forEach { buyAsset ->
 *     println("Buy ${buyAsset.asset} at price ${buyAsset.price} (${buyAsset.decimals} decimals)")
 * }
 * // Output:
 * // Buy iso4217:BRL at price 0.18 (2 decimals)
 * // Buy iso4217:USD at price 1.00 (2 decimals)
 * ```
 *
 * Example - Getting sell options (buying USDC):
 * ```kotlin
 * val pricesResponse = sep38Service.prices(
 *     buyAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
 *     buyAmount = "100"
 * )
 *
 * // Response contains sellAssets (what you need to sell to get 100 USDC)
 * pricesResponse.sellAssets?.forEach { sellAsset ->
 *     println("Sell ${sellAsset.asset} at price ${sellAsset.price} (${sellAsset.decimals} decimals)")
 * }
 * // Output:
 * // Sell iso4217:BRL at price 5.42 (2 decimals)
 * // Sell iso4217:USD at price 1.00 (2 decimals)
 * ```
 *
 * Example - Building asset selection UI:
 * ```kotlin
 * val pricesResponse = sep38Service.prices(
 *     sellAsset = "stellar:USDC:G...",
 *     sellAmount = "100",
 *     countryCode = "BR"
 * )
 *
 * pricesResponse.buyAssets?.forEach { buyAsset ->
 *     val assetName = extractAssetName(buyAsset.asset)
 *     val formattedPrice = "%.${buyAsset.decimals}f".format(buyAsset.price.toDouble())
 *
 *     displayAssetOption(
 *         name = assetName,
 *         price = formattedPrice,
 *         onClick = { proceedToQuote(buyAsset) }
 *     )
 * }
 * ```
 *
 * Example - Filtering and sorting prices:
 * ```kotlin
 * val pricesResponse = sep38Service.prices(
 *     sellAsset = "stellar:USDC:G...",
 *     sellAmount = "1000"
 * )
 *
 * // Find best rate (highest price when selling)
 * val bestRate = pricesResponse.buyAssets
 *     ?.maxByOrNull { it.price.toDouble() }
 *
 * println("Best rate: ${bestRate?.asset} at ${bestRate?.price}")
 * ```
 *
 * See also:
 * - [Sep38BuyAsset] for buy asset structure with price and decimals
 * - [Sep38SellAsset] for sell asset structure with price and decimals
 * - [Sep38PriceResponse] for getting a price for a specific asset pair
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property buyAssets List of available buy assets with prices (present when selling a specific asset)
 * @property sellAssets List of available sell assets with prices (present when buying a specific asset)
 */
@Serializable
data class Sep38PricesResponse(
    @SerialName("buy_assets")
    val buyAssets: List<Sep38BuyAsset>? = null,

    @SerialName("sell_assets")
    val sellAssets: List<Sep38SellAsset>? = null
)
