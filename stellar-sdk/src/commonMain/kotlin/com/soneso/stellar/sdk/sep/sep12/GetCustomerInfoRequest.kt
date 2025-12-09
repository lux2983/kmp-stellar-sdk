// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

/**
 * Request for retrieving customer KYC information and status from a SEP-12 anchor.
 *
 * Used to check what information an anchor requires for a customer, or to verify
 * the current status of a customer's KYC process. This endpoint serves two purposes:
 * 1. Discover required fields for new customers
 * 2. Check KYC status for existing customers
 *
 * Customer identification methods:
 * - Use [id] if you have a customer ID from a previous registration
 * - Use JWT sub value for account identification (recommended)
 * - Use [account] and [memo]/[memoType] for backwards compatibility
 * - Use [transactionId] when KYC requirements depend on transaction details
 *
 * Example - Check required fields for new customer:
 * ```kotlin
 * val request = GetCustomerInfoRequest(
 *     jwt = authToken,
 *     account = userAccountId,
 *     type = "sep31-sender"
 * )
 *
 * val response = kycService.getCustomerInfo(request)
 * if (response.status == CustomerStatus.NEEDS_INFO) {
 *     println("Required fields: ${response.fields?.keys}")
 * }
 * ```
 *
 * Example - Check existing customer status:
 * ```kotlin
 * val request = GetCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId
 * )
 *
 * val response = kycService.getCustomerInfo(request)
 * println("Status: ${response.status}")
 * ```
 *
 * Example - With transaction context:
 * ```kotlin
 * val request = GetCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId,
 *     transactionId = "abc123",
 *     type = "sep6-deposit"
 * )
 *
 * val response = kycService.getCustomerInfo(request)
 * // May require additional fields based on transaction amount
 * ```
 *
 * Example - Shared account with memo:
 * ```kotlin
 * val request = GetCustomerInfoRequest(
 *     jwt = authToken,
 *     account = sharedAccountId,
 *     memo = "user_12345",
 *     memoType = "id"
 * )
 * ```
 *
 * See also:
 * - [GetCustomerInfoResponse] for response details
 * - [CustomerStatus] for possible status values
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property jwt JWT token from SEP-10 or SEP-45 authentication
 * @property id Customer ID from previous PUT request (optional)
 * @property account Stellar account ID - deprecated, use JWT sub value instead (optional)
 * @property memo Memo for shared accounts (optional)
 * @property memoType Type of memo - deprecated, should always be 'id' (optional)
 * @property type KYC type (e.g., 'sep6-deposit', 'sep31-sender') (optional)
 * @property transactionId Associated transaction ID (optional)
 * @property lang Language code (ISO 639-1) for descriptions (optional)
 */
data class GetCustomerInfoRequest(
    val jwt: String,

    val id: String? = null,

    @Deprecated("Use JWT sub value instead. Maintained for backwards compatibility only.")
    val account: String? = null,

    val memo: String? = null,

    @Deprecated("Memos should always be of type id. Maintained for backwards compatibility with outdated clients.")
    val memoType: String? = null,

    val type: String? = null,

    val transactionId: String? = null,

    val lang: String? = null
)
