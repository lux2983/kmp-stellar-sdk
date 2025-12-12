// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing supported assets and delivery methods.
 *
 * Returns comprehensive information about all assets supported by the anchor
 * for exchange operations, including available delivery methods and country
 * restrictions. This is typically the first call made to discover what exchanges
 * are possible.
 *
 * The response lists both on-chain Stellar assets and off-chain assets (fiat currencies,
 * commodities, etc.) that can be exchanged through the anchor. Not all asset pairs
 * may be supported - use the GET /prices endpoint to see which specific pairs are available.
 *
 * Example - Fetching available assets:
 * ```kotlin
 * val infoResponse = sep38Service.info()
 *
 * infoResponse.assets.forEach { asset ->
 *     println("Asset: ${asset.asset}")
 *
 *     asset.countryCodes?.let { codes ->
 *         println("  Available in: ${codes.joinToString(", ")}")
 *     }
 *
 *     asset.sellDeliveryMethods?.forEach { method ->
 *         println("  Sell via: ${method.name} - ${method.description}")
 *     }
 *
 *     asset.buyDeliveryMethods?.forEach { method ->
 *         println("  Buy via: ${method.name} - ${method.description}")
 *     }
 * }
 * ```
 *
 * Example - Finding assets with specific delivery method:
 * ```kotlin
 * val infoResponse = sep38Service.info()
 *
 * val pixAssets = infoResponse.assets.filter { asset ->
 *     asset.sellDeliveryMethods?.any { it.name == "PIX" } == true
 * }
 *
 * println("Assets supporting PIX delivery:")
 * pixAssets.forEach { println("  ${it.asset}") }
 * ```
 *
 * Example - Building UI for asset selection:
 * ```kotlin
 * val infoResponse = sep38Service.info()
 *
 * // Separate on-chain and off-chain assets
 * val stellarAssets = infoResponse.assets.filter { it.asset.startsWith("stellar:") }
 * val fiatAssets = infoResponse.assets.filter { it.asset.startsWith("iso4217:") }
 *
 * // Display in UI
 * displayAssetCategory("Stellar Assets", stellarAssets)
 * displayAssetCategory("Fiat Currencies", fiatAssets)
 * ```
 *
 * Example - Checking country availability:
 * ```kotlin
 * val infoResponse = sep38Service.info(jwtToken)
 * val userCountry = "BR"
 *
 * val availableAssets = infoResponse.assets.filter { asset ->
 *     asset.countryCodes == null || asset.countryCodes.contains(userCountry)
 * }
 *
 * println("Assets available in Brazil:")
 * availableAssets.forEach { println("  ${it.asset}") }
 * ```
 *
 * See also:
 * - [Sep38Asset] for asset details including delivery methods
 * - [Sep38DeliveryMethod] for delivery method structure
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property assets List of supported assets with their delivery methods and restrictions
 */
@Serializable
data class Sep38InfoResponse(
    @SerialName("assets")
    val assets: List<Sep38Asset>
)
