// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a field that the anchor needs from the customer in SEP-12.
 *
 * Defines a piece of information the anchor has not yet received for the customer.
 * Used in GetCustomerInfoResponse to communicate field requirements and constraints.
 *
 * This object is required for customers in the NEEDS_INFO status but may be included
 * with any status. Fields are specified as an object with keys representing SEP-9
 * field names. Customers in ACCEPTED status should not have any required fields.
 *
 * Field attributes:
 * - [type]: Data type of the field value (string, binary, number, date)
 * - [description]: Human-readable description of the field
 * - [choices]: Optional array of valid values (for enum-like fields)
 * - [optional]: Whether the field is required (defaults to required if not specified)
 *
 * Example - Required text field:
 * ```kotlin
 * GetCustomerInfoField(
 *     type = "string",
 *     description = "Your legal last name",
 *     choices = null,
 *     optional = false
 * )
 * ```
 *
 * Example - Optional enum field:
 * ```kotlin
 * GetCustomerInfoField(
 *     type = "string",
 *     description = "Type of identification document",
 *     choices = listOf("passport", "drivers_license", "national_id"),
 *     optional = true
 * )
 * ```
 *
 * Example - Required binary field:
 * ```kotlin
 * GetCustomerInfoField(
 *     type = "binary",
 *     description = "Front side of your ID document (JPEG or PNG)",
 *     choices = null,
 *     optional = false
 * )
 * ```
 *
 * Example - Using fields from response:
 * ```kotlin
 * val response = kycService.getCustomerInfo(request)
 *
 * if (response.status == CustomerStatus.NEEDS_INFO) {
 *     response.fields?.forEach { (fieldName, fieldInfo) ->
 *         println("Field: $fieldName")
 *         println("  Type: ${fieldInfo.type}")
 *         println("  Description: ${fieldInfo.description}")
 *         println("  Required: ${fieldInfo.optional != true}")
 *
 *         if (fieldInfo.choices != null) {
 *             println("  Valid choices: ${fieldInfo.choices.joinToString(", ")}")
 *         }
 *     }
 * }
 * ```
 *
 * See also:
 * - [GetCustomerInfoProvidedField] for fields already provided by customer
 * - [GetCustomerInfoResponse] for full customer information
 * - [SEP-0009 Standard KYC Fields](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md)
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property type The data type of the field value (string, binary, number, date)
 * @property description Human-readable description of this field
 * @property choices Optional array of valid values for this field (for enum-like selection)
 * @property optional Whether this field is optional (defaults to required if null or false)
 */
@Serializable
data class GetCustomerInfoField(
    @SerialName("type")
    val type: String,

    @SerialName("description")
    val description: String,

    @SerialName("choices")
    val choices: List<String>? = null,

    @SerialName("optional")
    val optional: Boolean? = null
)
