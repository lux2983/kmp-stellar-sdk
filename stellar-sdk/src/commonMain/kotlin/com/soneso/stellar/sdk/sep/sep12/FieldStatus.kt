// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

/**
 * Verification status of individual KYC fields in SEP-12.
 *
 * Indicates the current state of each provided field during the KYC process.
 * Allows anchors to communicate field-level verification status and requirements.
 *
 * Status meanings:
 * - [ACCEPTED]: Field has been verified and accepted
 * - [PROCESSING]: Field is being reviewed
 * - [REJECTED]: Field was rejected (see error property for reason)
 * - [VERIFICATION_REQUIRED]: Field needs additional verification (e.g., email/phone code)
 *
 * Use cases:
 * - Email verification: Status is VERIFICATION_REQUIRED until code is submitted
 * - Phone verification: Status is VERIFICATION_REQUIRED until SMS code is provided
 * - Document review: Status is PROCESSING while being reviewed, then ACCEPTED or REJECTED
 * - Field corrections: Status is REJECTED with error message indicating what's wrong
 *
 * Example - Check field status:
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 *
 * response.providedFields?.forEach { (fieldName, fieldInfo) ->
 *     when (fieldInfo.status) {
 *         FieldStatus.ACCEPTED -> {
 *             println("$fieldName: Verified")
 *         }
 *         FieldStatus.PROCESSING -> {
 *             println("$fieldName: Under review")
 *         }
 *         FieldStatus.REJECTED -> {
 *             println("$fieldName: Rejected - ${fieldInfo.error}")
 *         }
 *         FieldStatus.VERIFICATION_REQUIRED -> {
 *             println("$fieldName: Verification code required")
 *         }
 *         null -> {
 *             println("$fieldName: Status not provided")
 *         }
 *     }
 * }
 * ```
 *
 * Example - Handle email verification:
 * ```kotlin
 * val emailField = response.providedFields?.get("email_address")
 * if (emailField?.status == FieldStatus.VERIFICATION_REQUIRED) {
 *     // User receives email with code
 *     val verifyRequest = PutCustomerInfoRequest(
 *         jwt = authToken,
 *         verificationFields = mapOf(
 *             "email_address_verification" to "123456"
 *         )
 *     )
 *     kycService.putCustomerInfo(verifyRequest)
 * }
 * ```
 *
 * See also:
 * - [GetCustomerInfoProvidedField] for field information with status
 * - [CustomerStatus] for overall customer verification status
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md#field-statuses)
 */
enum class FieldStatus {
    /**
     * Field has been verified and accepted.
     *
     * The provided information meets requirements and has passed verification.
     */
    ACCEPTED,

    /**
     * Field is being reviewed.
     *
     * The anchor is processing this field. Customer should wait for status
     * to change to ACCEPTED or REJECTED.
     */
    PROCESSING,

    /**
     * Field was rejected.
     *
     * The provided information was not accepted. Check the error property
     * in GetCustomerInfoProvidedField for details on what needs to be corrected.
     */
    REJECTED,

    /**
     * Field requires additional verification.
     *
     * Customer must provide a verification code sent via email, SMS, or other channel.
     * Use PutCustomerInfoRequest with verificationFields to submit the code.
     *
     * Example verification field names:
     * - email_address_verification: Code sent to email
     * - mobile_number_verification: Code sent via SMS
     */
    VERIFICATION_REQUIRED;

    companion object {
        /**
         * Converts a string status value to FieldStatus enum.
         *
         * Used for parsing JSON responses from SEP-12 servers.
         *
         * @param value The string status value (e.g., "ACCEPTED", "VERIFICATION_REQUIRED")
         * @return The corresponding FieldStatus enum value
         * @throws IllegalArgumentException if the value is not a valid status
         *
         * Example:
         * ```kotlin
         * val status = FieldStatus.fromString("VERIFICATION_REQUIRED")
         * // Returns FieldStatus.VERIFICATION_REQUIRED
         * ```
         */
        fun fromString(value: String): FieldStatus {
            return when (value.uppercase()) {
                "ACCEPTED" -> ACCEPTED
                "PROCESSING" -> PROCESSING
                "REJECTED" -> REJECTED
                "VERIFICATION_REQUIRED" -> VERIFICATION_REQUIRED
                else -> throw IllegalArgumentException("Unknown field status: $value")
            }
        }
    }
}
