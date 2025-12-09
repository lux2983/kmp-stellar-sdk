// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a field that the anchor has already received from the customer in SEP-12.
 *
 * Defines a piece of information the anchor has received for the customer, along with
 * its verification status. Used in GetCustomerInfoResponse to communicate which fields
 * have been provided and their current verification state.
 *
 * This object is not required unless one or more provided fields require verification
 * via the verification endpoint. If the server does not wish to expose which fields
 * were accepted or rejected, the status property may be omitted.
 *
 * Field attributes:
 * - [type]: Data type of the field value (string, binary, number, date)
 * - [description]: Human-readable description of the field
 * - [choices]: Optional array of valid values (for reference)
 * - [optional]: Whether the field is optional
 * - [status]: Verification status (ACCEPTED, PROCESSING, REJECTED, VERIFICATION_REQUIRED)
 * - [error]: Human-readable error message if status is REJECTED
 *
 * Example - Accepted field:
 * ```kotlin
 * GetCustomerInfoProvidedField(
 *     type = "string",
 *     description = "Legal last name",
 *     choices = null,
 *     optional = false,
 *     status = FieldStatus.ACCEPTED,
 *     error = null
 * )
 * ```
 *
 * Example - Rejected field with error:
 * ```kotlin
 * GetCustomerInfoProvidedField(
 *     type = "string",
 *     description = "Email address",
 *     choices = null,
 *     optional = false,
 *     status = FieldStatus.REJECTED,
 *     error = "Invalid email format"
 * )
 * ```
 *
 * Example - Field requiring verification:
 * ```kotlin
 * GetCustomerInfoProvidedField(
 *     type = "string",
 *     description = "Mobile phone number",
 *     choices = null,
 *     optional = false,
 *     status = FieldStatus.VERIFICATION_REQUIRED,
 *     error = null
 * )
 * ```
 *
 * Example - Checking provided fields:
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 *
 * response.providedFields?.forEach { (fieldName, fieldInfo) ->
 *     when (fieldInfo.status) {
 *         FieldStatus.ACCEPTED -> {
 *             println("$fieldName: Verified successfully")
 *         }
 *         FieldStatus.PROCESSING -> {
 *             println("$fieldName: Under review")
 *         }
 *         FieldStatus.REJECTED -> {
 *             println("$fieldName: Rejected - ${fieldInfo.error}")
 *             // Prompt user to correct and resubmit
 *         }
 *         FieldStatus.VERIFICATION_REQUIRED -> {
 *             println("$fieldName: Verification code required")
 *             // Prompt user to enter code sent via email/SMS
 *         }
 *         null -> {
 *             println("$fieldName: Provided (status unknown)")
 *         }
 *     }
 * }
 * ```
 *
 * See also:
 * - [GetCustomerInfoField] for fields the anchor needs from the customer
 * - [FieldStatus] for field verification status values
 * - [GetCustomerInfoResponse] for full customer information
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property type The data type of the field value (string, binary, number, date)
 * @property description Human-readable description of this field
 * @property choices Optional array of valid values for this field
 * @property optional Whether this field is optional
 * @property status The verification status of this field (may be null if not exposed)
 * @property error Human-readable error description if status is REJECTED
 */
@Serializable
data class GetCustomerInfoProvidedField(
    @SerialName("type")
    val type: String,

    @SerialName("description")
    val description: String,

    @SerialName("choices")
    val choices: List<String>? = null,

    @SerialName("optional")
    val optional: Boolean? = null,

    @SerialName("status")
    val status: FieldStatus? = null,

    @SerialName("error")
    val error: String? = null
)
