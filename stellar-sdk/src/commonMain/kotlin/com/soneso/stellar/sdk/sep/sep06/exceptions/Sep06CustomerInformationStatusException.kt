// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06.exceptions

/**
 * Exception thrown when customer KYC status prevents the request (HTTP 403).
 *
 * Indicates that the anchor has received customer information but the verification
 * status prevents the transaction from proceeding. The response type is
 * "customer_info_status" with a status of either "pending" or "denied".
 *
 * Status values:
 * - "pending": KYC verification is in progress, retry later
 * - "denied": KYC verification failed, customer cannot use the service
 *
 * This error typically occurs when:
 * - KYC verification is still being processed by the anchor
 * - Manual review is required for the customer
 * - Customer failed identity verification
 * - Customer is on a restricted list
 * - Regulatory requirements prevent service
 *
 * Recovery actions:
 * - For "pending" status: Wait and retry the request later
 * - For "denied" status: Direct user to more_info_url if available
 * - Contact anchor support for resolution
 *
 * Example - Handle KYC status:
 * ```kotlin
 * suspend fun initiateDeposit(
 *     sep06Service: Sep06Service,
 *     assetCode: String,
 *     jwt: String
 * ): Sep06DepositResponse? {
 *     try {
 *         return sep06Service.deposit(assetCode, jwt)
 *     } catch (e: Sep06CustomerInformationStatusException) {
 *         when (e.status) {
 *             "pending" -> {
 *                 println("KYC verification in progress, please wait...")
 *                 e.moreInfoUrl?.let { url ->
 *                     println("Check status at: $url")
 *                 }
 *                 // Schedule retry later
 *             }
 *             "denied" -> {
 *                 println("KYC verification denied")
 *                 e.moreInfoUrl?.let { url ->
 *                     println("More information: $url")
 *                 }
 *                 // Cannot proceed
 *             }
 *         }
 *         return null
 *     }
 * }
 * ```
 *
 * Example - Polling for KYC completion:
 * ```kotlin
 * suspend fun waitForKycApproval(
 *     sep06Service: Sep06Service,
 *     assetCode: String,
 *     jwt: String,
 *     maxAttempts: Int = 10,
 *     delayMs: Long = 30000
 * ): Sep06DepositResponse? {
 *     repeat(maxAttempts) { attempt ->
 *         try {
 *             return sep06Service.deposit(assetCode, jwt)
 *         } catch (e: Sep06CustomerInformationStatusException) {
 *             if (e.status == "pending") {
 *                 println("KYC pending (attempt ${attempt + 1}/$maxAttempts)")
 *                 delay(delayMs)
 *             } else {
 *                 println("KYC denied: ${e.moreInfoUrl ?: "No additional info"}")
 *                 return null
 *             }
 *         }
 *     }
 *     println("KYC verification timeout")
 *     return null
 * }
 * ```
 *
 * See also:
 * - [Sep06Exception] base class
 * - [Sep06CustomerInformationNeededException] for additional KYC fields needed
 * - [SEP-0006 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md)
 * - [SEP-0012 KYC API](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property status KYC verification status ("pending" or "denied")
 * @property moreInfoUrl Optional URL where the user can get more information about their status
 * @property eta Estimated time in seconds until the status may change (for "pending" status)
 */
class Sep06CustomerInformationStatusException(
    val status: String,
    val moreInfoUrl: String? = null,
    val eta: Long? = null
) : Sep06Exception(
    message = "Customer information status: $status${moreInfoUrl?.let { " (more info: $it)" } ?: ""}"
) {
    override fun toString(): String {
        val etaInfo = eta?.let { " (ETA: ${it}s)" } ?: ""
        return "SEP-06 customer information status: $status${moreInfoUrl?.let { " (more info: $it)" } ?: ""}$etaInfo"
    }
}
