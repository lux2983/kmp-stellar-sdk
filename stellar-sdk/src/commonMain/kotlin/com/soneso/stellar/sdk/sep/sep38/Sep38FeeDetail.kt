// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep38

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Detailed breakdown of a single fee component.
 *
 * Provides transparency about individual fees that make up the total fee for
 * an exchange operation. Each detail represents one type of fee charged by the
 * anchor (e.g., network fee, processing fee, conversion fee).
 *
 * The sum of all [amount] values across all fee details should equal the total
 * fee amount specified in the parent [Sep38Fee] object.
 *
 * Example - Network fee:
 * ```kotlin
 * val networkFee = Sep38FeeDetail(
 *     name = "Network fee",
 *     amount = "5.00",
 *     description = "Fee charged to cover blockchain transaction costs"
 * )
 * ```
 *
 * Example - Processing fee:
 * ```kotlin
 * val processingFee = Sep38FeeDetail(
 *     name = "Processing fee",
 *     amount = "10.00",
 *     description = "Fee for processing the exchange operation"
 * )
 * ```
 *
 * Example - Fee without description:
 * ```kotlin
 * val serviceFee = Sep38FeeDetail(
 *     name = "Service fee",
 *     amount = "8.40"
 * )
 * // Description is optional
 * ```
 *
 * Example - Displaying fee breakdown to users:
 * ```kotlin
 * val quote = sep38Service.postQuote(request, jwtToken)
 *
 * println("Total fee: ${quote.fee.total} ${quote.fee.asset}")
 * quote.fee.details?.forEach { detail ->
 *     println("  ${detail.name}: ${detail.amount}")
 *     detail.description?.let { println("    ($it)") }
 * }
 * // Output:
 * // Total fee: 42.00 iso4217:BRL
 * //   PIX fee: 12.00
 * //     (Fee charged in order to process the outgoing PIX transaction.)
 * //   Brazilian conciliation fee: 15.00
 * //     (Fee charged in order to process conciliation costs with intermediary banks.)
 * //   Service fee: 15.00
 * ```
 *
 * See also:
 * - [Sep38Fee] which contains the total fee and optional details list
 * - [Sep38PriceResponse] and [Sep38QuoteResponse] which include fee information
 * - [SEP-0038 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)
 *
 * @property name Name identifying this fee component (e.g., "Network fee", "Processing fee", "Service fee")
 * @property amount Amount for this fee component as a string decimal
 * @property description Optional human-readable explanation of this fee component
 */
@Serializable
data class Sep38FeeDetail(
    @SerialName("name")
    val name: String,

    @SerialName("amount")
    val amount: String,

    @SerialName("description")
    val description: String? = null
)
