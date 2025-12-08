// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from a GET /customer request containing customer KYC status and field requirements.
 *
 * Indicates the current state of the customer's KYC process and what information
 * (if any) is still required. Used to guide the customer through the verification
 * process and display appropriate UI.
 *
 * Response properties:
 * - [id]: Customer ID for future requests (present if customer was registered)
 * - [status]: Current KYC status (ACCEPTED, PROCESSING, NEEDS_INFO, REJECTED)
 * - [fields]: Fields the anchor needs (present when status is NEEDS_INFO)
 * - [providedFields]: Fields already provided with their status
 * - [message]: Human-readable message about the customer's status
 *
 * Example - New customer (NEEDS_INFO):
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 * // Response:
 * // id: null (not yet registered)
 * // status: NEEDS_INFO
 * // fields: {
 * //   "first_name": GetCustomerInfoField(type="string", description="Legal first name"),
 * //   "last_name": GetCustomerInfoField(type="string", description="Legal last name"),
 * //   "email_address": GetCustomerInfoField(type="string", description="Email address")
 * // }
 *
 * response.fields?.forEach { (fieldName, fieldInfo) ->
 *     println("Need: $fieldName - ${fieldInfo.description}")
 * }
 * ```
 *
 * Example - Customer under review (PROCESSING):
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 * // Response:
 * // id: "d1ce2f48-3ff1-495d-9240-7a50d806cfed"
 * // status: PROCESSING
 * // message: "Your information is being reviewed. This typically takes 1-2 business days."
 *
 * println(response.message)
 * ```
 *
 * Example - Customer approved (ACCEPTED):
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 * // Response:
 * // id: "d1ce2f48-3ff1-495d-9240-7a50d806cfed"
 * // status: ACCEPTED
 * // message: "Your account has been approved"
 *
 * if (response.status == CustomerStatus.ACCEPTED) {
 *     proceedWithTransfer()
 * }
 * ```
 *
 * Example - Field needs verification (VERIFICATION_REQUIRED):
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 * // Response:
 * // status: NEEDS_INFO
 * // providedFields: {
 * //   "email_address": GetCustomerInfoProvidedField(
 * //     status=VERIFICATION_REQUIRED,
 * //     description="Email address"
 * //   )
 * // }
 *
 * val emailField = response.providedFields?.get("email_address")
 * if (emailField?.status == FieldStatus.VERIFICATION_REQUIRED) {
 *     println("Please enter the verification code sent to your email")
 * }
 * ```
 *
 * Example - Customer rejected (REJECTED):
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 * // Response:
 * // id: "d1ce2f48-3ff1-495d-9240-7a50d806cfed"
 * // status: REJECTED
 * // message: "Unable to verify identity. Please contact support."
 *
 * if (response.status == CustomerStatus.REJECTED) {
 *     showErrorDialog(response.message ?: "Application rejected")
 * }
 * ```
 *
 * See also:
 * - [GetCustomerInfoRequest] for request parameters
 * - [CustomerStatus] for status meanings
 * - [GetCustomerInfoField] for field requirement details
 * - [GetCustomerInfoProvidedField] for provided field status
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property id Customer ID if registered, null if not yet created
 * @property status Current KYC status (ACCEPTED, PROCESSING, NEEDS_INFO, REJECTED)
 * @property fields Fields the anchor needs from the customer (required for NEEDS_INFO status)
 * @property providedFields Fields already provided with verification status
 * @property message Human-readable message about the customer's status (required for REJECTED)
 */
@Serializable
data class GetCustomerInfoResponse(
    @SerialName("id")
    val id: String? = null,

    @SerialName("status")
    val status: CustomerStatus,

    @SerialName("fields")
    val fields: Map<String, GetCustomerInfoField>? = null,

    @SerialName("provided_fields")
    val providedFields: Map<String, GetCustomerInfoProvidedField>? = null,

    @SerialName("message")
    val message: String? = null
)
