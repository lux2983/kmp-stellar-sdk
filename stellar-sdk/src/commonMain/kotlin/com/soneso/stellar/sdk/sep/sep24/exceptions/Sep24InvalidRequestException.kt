// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep24.exceptions

/**
 * Exception thrown when SEP-24 request parameters are invalid (HTTP 400).
 *
 * Indicates that the anchor rejected the request due to invalid, malformed, or missing
 * parameters. This error typically occurs when:
 * - Asset code is not supported by the anchor
 * - Required fields are missing (e.g., asset_code)
 * - Invalid amount format or value
 * - Invalid account format
 * - Unsupported deposit/withdrawal type
 * - Amount exceeds anchor limits
 * - Missing required KYC information
 *
 * Recovery actions:
 * - Verify the asset code is supported (check /info endpoint)
 * - Ensure all required parameters are provided
 * - Validate amount is within anchor's min/max limits
 * - Check account ID format is valid
 *
 * Example - Handle invalid asset:
 * ```kotlin
 * suspend fun initiateDeposit(
 *     sep24Service: Sep24Service,
 *     assetCode: String,
 *     jwt: String
 * ): Sep24DepositResponse? {
 *     try {
 *         return sep24Service.deposit(assetCode, jwt)
 *     } catch (e: Sep24InvalidRequestException) {
 *         println("Invalid request: ${e.message}")
 *
 *         // Check supported assets
 *         val info = sep24Service.info()
 *         val supportedAssets = info.deposit.keys
 *         println("Supported assets: ${supportedAssets.joinToString()}")
 *
 *         return null
 *     }
 * }
 * ```
 *
 * Example - Validate before request:
 * ```kotlin
 * suspend fun depositWithValidation(
 *     sep24Service: Sep24Service,
 *     assetCode: String,
 *     amount: String?,
 *     jwt: String
 * ): Sep24DepositResponse? {
 *     // Get anchor info for validation
 *     val info = sep24Service.info()
 *     val assetInfo = info.deposit[assetCode]
 *
 *     if (assetInfo == null) {
 *         println("Asset $assetCode not supported for deposit")
 *         return null
 *     }
 *
 *     // Validate amount if provided
 *     if (amount != null) {
 *         val amountValue = amount.toDoubleOrNull()
 *         if (amountValue == null || amountValue <= 0) {
 *             println("Invalid amount: $amount")
 *             return null
 *         }
 *
 *         assetInfo.minAmount?.let { min ->
 *             if (amountValue < min) {
 *                 println("Amount below minimum: $min")
 *                 return null
 *             }
 *         }
 *
 *         assetInfo.maxAmount?.let { max ->
 *             if (amountValue > max) {
 *                 println("Amount above maximum: $max")
 *                 return null
 *             }
 *         }
 *     }
 *
 *     try {
 *         return sep24Service.deposit(assetCode, amount, jwt)
 *     } catch (e: Sep24InvalidRequestException) {
 *         println("Request rejected: ${e.message}")
 *         return null
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep24Exception] base class
 * - [SEP-0024 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0024.md)
 *
 * @param message Error message describing the invalid request
 */
class Sep24InvalidRequestException(
    message: String
) : Sep24Exception(message) {
    override fun toString(): String {
        return "SEP-24 invalid request: $message"
    }
}
