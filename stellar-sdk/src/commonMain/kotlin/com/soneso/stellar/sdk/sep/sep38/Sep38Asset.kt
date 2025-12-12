// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about a supported asset for exchange operations.
 *
 * Describes an asset that can be bought or sold through the anchor, including
 * available delivery methods and country restrictions. Assets are identified
 * using the Asset Identification Format (e.g., "stellar:USDC:G...", "iso4217:USD").
 *
 * Delivery methods specify how off-chain assets can be transferred:
 * - [sellDeliveryMethods]: How users deliver off-chain assets to the anchor
 * - [buyDeliveryMethods]: How users receive off-chain assets from the anchor
 *
 * Country codes restrict where the asset is available for exchange operations.
 *
 * Example - Stellar asset (no delivery methods):
 * ```kotlin
 * val asset = Sep38Asset(
 *     asset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN"
 * )
 * // No delivery methods needed for on-chain assets
 * ```
 *
 * Example - Fiat asset with delivery methods:
 * ```kotlin
 * val asset = Sep38Asset(
 *     asset = "iso4217:BRL",
 *     sellDeliveryMethods = listOf(
 *         Sep38DeliveryMethod("cash", "Deposit cash BRL at one of our agent locations."),
 *         Sep38DeliveryMethod("ACH", "Send BRL directly to the Anchor's bank account."),
 *         Sep38DeliveryMethod("PIX", "Send BRL directly to the Anchor's bank account.")
 *     ),
 *     buyDeliveryMethods = listOf(
 *         Sep38DeliveryMethod("cash", "Pick up cash BRL at one of our payout locations."),
 *         Sep38DeliveryMethod("ACH", "Have BRL sent directly to your bank account."),
 *         Sep38DeliveryMethod("PIX", "Have BRL sent directly to the account of your choice.")
 *     ),
 *     countryCodes = listOf("BR")
 * )
 * ```
 *
 * Example - Asset with regional restrictions:
 * ```kotlin
 * val asset = Sep38Asset(
 *     asset = "iso4217:USD",
 *     sellDeliveryMethods = listOf(
 *         Sep38DeliveryMethod("WIRE", "International wire transfer")
 *     ),
 *     buyDeliveryMethods = listOf(
 *         Sep38DeliveryMethod("WIRE", "International wire transfer")
 *     ),
 *     countryCodes = listOf("US", "CA", "MX")
 * )
 * ```
 *
 * See also:
 * - [Sep38InfoResponse] which contains the list of supported assets
 * - [Sep38DeliveryMethod] for delivery method details
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property asset Asset identifier in Asset Identification Format (e.g., "stellar:CODE:ISSUER", "iso4217:USD")
 * @property sellDeliveryMethods Optional delivery methods for selling this asset to the anchor (off-chain assets only)
 * @property buyDeliveryMethods Optional delivery methods for buying this asset from the anchor (off-chain assets only)
 * @property countryCodes Optional ISO 3166-1 alpha-2 or ISO 3166-2 country codes where asset is available
 */
@Serializable
data class Sep38Asset(
    @SerialName("asset")
    val asset: String,

    @SerialName("sell_delivery_methods")
    val sellDeliveryMethods: List<Sep38DeliveryMethod>? = null,

    @SerialName("buy_delivery_methods")
    val buyDeliveryMethods: List<Sep38DeliveryMethod>? = null,

    @SerialName("country_codes")
    val countryCodes: List<String>? = null
)
