// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Delivery method for off-chain asset exchange operations.
 *
 * Describes how a user can deliver an off-chain asset to the anchor (sell delivery method)
 * or receive an off-chain asset from the anchor (buy delivery method). Examples include
 * bank transfer (ACH, WIRE), mobile money (PIX), or cash pickup.
 *
 * The [name] is used as the identifier in API requests (sell_delivery_method, buy_delivery_method),
 * while [description] provides human-readable details for display to end users.
 *
 * Example - Bank transfer delivery method:
 * ```kotlin
 * val method = Sep38DeliveryMethod(
 *     name = "ACH",
 *     description = "Send USD directly to the Anchor's bank account via ACH"
 * )
 * ```
 *
 * Example - Mobile money delivery method:
 * ```kotlin
 * val method = Sep38DeliveryMethod(
 *     name = "PIX",
 *     description = "Send BRL directly to the account of your choice via PIX"
 * )
 * ```
 *
 * Example - Cash pickup delivery method:
 * ```kotlin
 * val method = Sep38DeliveryMethod(
 *     name = "cash",
 *     description = "Pick up cash BRL at one of our payout locations"
 * )
 * ```
 *
 * See also:
 * - [Sep38Asset] which contains lists of available delivery methods
 * - [Sep38QuoteRequest] which uses delivery method names in requests
 * - [Sep38QuoteResponse] which may include delivery method information
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property name Unique identifier for this delivery method (e.g., "WIRE", "ACH", "PIX", "cash")
 * @property description Human-readable description of the delivery method for user display
 */
@Serializable
data class Sep38DeliveryMethod(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String
)
