// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06.exceptions

/**
 * Exception thrown when SEP-6 request parameters are invalid (HTTP 400).
 *
 * Indicates that the anchor rejected the request due to invalid, malformed, or missing
 * parameters. This error typically occurs when:
 * - Asset code is not supported by the anchor
 * - Required fields are missing (e.g., asset_code, type)
 * - Invalid amount format or value
 * - Invalid account format
 * - Unsupported deposit/withdrawal type
 * - Amount exceeds anchor limits
 * - Invalid destination address format
 * - Unsupported currency conversion
 *
 * Recovery actions:
 * - Verify the asset code is supported (check /info endpoint)
 * - Ensure all required parameters are provided
 * - Validate amount is within anchor's min/max limits
 * - Check account ID format is valid
 * - Verify the deposit/withdrawal type is supported
 *
 * Example - Handle invalid request:
 * ```kotlin
 * suspend fun initiateDeposit(
 *     sep06Service: Sep06Service,
 *     assetCode: String,
 *     type: String,
 *     jwt: String
 * ): Sep06DepositResponse? {
 *     try {
 *         return sep06Service.deposit(assetCode, type, jwt)
 *     } catch (e: Sep06InvalidRequestException) {
 *         println("Invalid request: ${e.errorMessage}")
 *
 *         // Check supported assets and types
 *         val info = sep06Service.info()
 *         val assetInfo = info.deposit[assetCode]
 *         if (assetInfo == null) {
 *             println("Asset $assetCode not supported")
 *         } else {
 *             println("Supported types: ${assetInfo.types?.keys?.joinToString()}")
 *         }
 *
 *         return null
 *     }
 * }
 * ```
 *
 * Example - Validate before request:
 * ```kotlin
 * suspend fun depositWithValidation(
 *     sep06Service: Sep06Service,
 *     assetCode: String,
 *     amount: String?,
 *     type: String,
 *     jwt: String
 * ): Sep06DepositResponse? {
 *     // Get anchor info for validation
 *     val info = sep06Service.info()
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
 *         return sep06Service.deposit(assetCode, amount, type, jwt)
 *     } catch (e: Sep06InvalidRequestException) {
 *         println("Request rejected: ${e.errorMessage}")
 *         return null
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep06Exception] base class
 * - [SEP-0006 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md)
 *
 * @property errorMessage Detailed error message from the anchor describing the invalid request
 */
class Sep06InvalidRequestException(
    val errorMessage: String
) : Sep06Exception(errorMessage) {
    override fun toString(): String {
        return "SEP-06 invalid request: $errorMessage"
    }
}
