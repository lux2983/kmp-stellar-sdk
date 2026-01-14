// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06.exceptions

/**
 * Exception thrown when additional customer information is required (HTTP 403).
 *
 * Indicates that the anchor requires additional KYC information before processing
 * the request. The response type is "non_interactive_customer_info_needed" and
 * includes a list of SEP-9 field names that must be collected and submitted
 * via SEP-12 KYC API before retrying the request.
 *
 * This error typically occurs when:
 * - Initial deposit/withdrawal request is made without sufficient KYC
 * - Anchor requires additional information for higher transaction limits
 * - Regulatory requirements necessitate more customer data
 * - Transaction type requires specific fields (e.g., bank account details)
 *
 * Recovery actions:
 * - Collect the required fields from the user
 * - Submit the information via SEP-12 KYC API
 * - Retry the original deposit/withdrawal request
 *
 * Example - Handle customer information needed:
 * ```kotlin
 * suspend fun initiateDeposit(
 *     sep06Service: Sep06Service,
 *     sep12Service: Sep12Service,
 *     assetCode: String,
 *     jwt: String
 * ): Sep06DepositResponse? {
 *     try {
 *         return sep06Service.deposit(assetCode, jwt)
 *     } catch (e: Sep06CustomerInformationNeededException) {
 *         println("Additional information required:")
 *         e.fields.forEach { field ->
 *             println("  - $field")
 *         }
 *
 *         // Collect and submit required fields via SEP-12
 *         val customerData = collectCustomerData(e.fields)
 *         sep12Service.putCustomer(customerData, jwt)
 *
 *         // Retry the deposit
 *         return sep06Service.deposit(assetCode, jwt)
 *     }
 * }
 * ```
 *
 * Example - Display required fields to user:
 * ```kotlin
 * fun handleKycRequired(e: Sep06CustomerInformationNeededException) {
 *     val fieldDescriptions = mapOf(
 *         "first_name" to "First Name",
 *         "last_name" to "Last Name",
 *         "email_address" to "Email Address",
 *         "bank_account_number" to "Bank Account Number",
 *         "bank_routing_number" to "Bank Routing Number"
 *     )
 *
 *     println("Please provide the following information:")
 *     e.fields.forEach { field ->
 *         val description = fieldDescriptions[field] ?: field
 *         println("  - $description")
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep06Exception] base class
 * - [Sep06CustomerInformationStatusException] for KYC status errors
 * - [SEP-0006 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md)
 * - [SEP-0009 Standard KYC Fields](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md)
 * - [SEP-0012 KYC API](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property fields List of SEP-9 field names that must be collected from the customer
 */
class Sep06CustomerInformationNeededException(
    val fields: List<String>
) : Sep06Exception(
    message = "Customer information needed: ${fields.joinToString()}"
) {
    override fun toString(): String {
        return "SEP-06 customer information needed: ${fields.joinToString()}"
    }
}
