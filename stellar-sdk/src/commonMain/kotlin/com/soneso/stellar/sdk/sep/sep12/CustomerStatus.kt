// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

/**
 * Status of the customer's KYC process in SEP-12.
 *
 * Indicates the current state of customer verification and what actions
 * are required to proceed with anchor services.
 *
 * Status meanings:
 * - [ACCEPTED]: Customer has been approved. No further action required.
 * - [PROCESSING]: Customer information is being reviewed. Wait for status update.
 * - [NEEDS_INFO]: More information is required. Check the `fields` property for requirements.
 * - [REJECTED]: Customer was rejected. Check the `message` property for reason.
 *
 * Status flow:
 * 1. New customer starts in NEEDS_INFO status
 * 2. After submitting information, moves to PROCESSING
 * 3. May return to NEEDS_INFO if more information is needed
 * 4. Eventually reaches ACCEPTED (approved) or REJECTED (denied)
 *
 * Example - Check customer status:
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 *
 * when (response.status) {
 *     CustomerStatus.ACCEPTED -> {
 *         println("Customer approved, can proceed with transfer")
 *     }
 *     CustomerStatus.PROCESSING -> {
 *         println("Verification in progress, please wait")
 *     }
 *     CustomerStatus.NEEDS_INFO -> {
 *         println("More information required:")
 *         response.fields?.keys?.forEach { field ->
 *             println("- $field")
 *         }
 *     }
 *     CustomerStatus.REJECTED -> {
 *         println("Customer rejected: ${response.message}")
 *     }
 * }
 * ```
 *
 * See also:
 * - [GetCustomerInfoResponse] for full customer information
 * - [FieldStatus] for individual field verification status
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md#customer-statuses)
 */
enum class CustomerStatus {
    /**
     * Customer has been approved and can proceed with anchor services.
     *
     * No further action is required. The customer's information has been
     * verified and meets all regulatory requirements.
     */
    ACCEPTED,

    /**
     * Customer information is being reviewed.
     *
     * The anchor is processing the submitted information. Customer should
     * wait for status to change to ACCEPTED or NEEDS_INFO.
     */
    PROCESSING,

    /**
     * More information is required from the customer.
     *
     * The customer must provide additional information. Check the `fields`
     * property in GetCustomerInfoResponse for specific requirements.
     */
    NEEDS_INFO,

    /**
     * Customer was rejected and cannot use anchor services.
     *
     * The customer's application was denied. Check the `message` property
     * in GetCustomerInfoResponse for the reason.
     */
    REJECTED;

    companion object {
        /**
         * Converts a string status value to CustomerStatus enum.
         *
         * Used for parsing JSON responses from SEP-12 servers.
         *
         * @param value The string status value (e.g., "ACCEPTED", "PROCESSING")
         * @return The corresponding CustomerStatus enum value
         * @throws IllegalArgumentException if the value is not a valid status
         *
         * Example:
         * ```kotlin
         * val status = CustomerStatus.fromString("ACCEPTED")
         * // Returns CustomerStatus.ACCEPTED
         * ```
         */
        fun fromString(value: String): CustomerStatus {
            return when (value.uppercase()) {
                "ACCEPTED" -> ACCEPTED
                "PROCESSING" -> PROCESSING
                "NEEDS_INFO" -> NEEDS_INFO
                "REJECTED" -> REJECTED
                else -> throw IllegalArgumentException("Unknown customer status: $value")
            }
        }
    }
}
