// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import com.soneso.stellar.sdk.sep.sep09.StandardKYCFields

/**
 * Request for uploading or updating customer KYC information in SEP-12.
 *
 * Submits customer data to the anchor in an authenticated and idempotent manner.
 * Used for both initial registration and updating existing customer information.
 * Supports text fields, binary file uploads, verification codes, and file references.
 *
 * Request categories:
 * 1. Standard KYC fields via [kycFields] (SEP-09 standard fields)
 * 2. Custom fields via [customFields] and [customFiles]
 * 3. Verification codes via [verificationFields] (e.g., email/phone verification)
 * 4. File references via [fileReferences] (for separately uploaded files)
 *
 * The request is idempotent: multiple calls with the same data won't create duplicate
 * customers. Use the customer [id] from the response for subsequent updates.
 *
 * Example - Basic registration:
 * ```kotlin
 * val request = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     kycFields = StandardKYCFields(
 *         naturalPersonKYCFields = NaturalPersonKYCFields(
 *             firstName = "John",
 *             lastName = "Doe",
 *             emailAddress = "john@example.com",
 *             birthDate = LocalDate(1990, 1, 15)
 *         )
 *     )
 * )
 *
 * val response = kycService.putCustomerInfo(request)
 * println("Customer ID: ${response.id}")
 * ```
 *
 * Example - With document uploads:
 * ```kotlin
 * val request = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId, // Update existing customer
 *     kycFields = StandardKYCFields(
 *         naturalPersonKYCFields = NaturalPersonKYCFields(
 *             photoIdFront = loadImageBytes("passport_front.jpg"),
 *             photoIdBack = loadImageBytes("passport_back.jpg")
 *         )
 *     )
 * )
 *
 * val response = kycService.putCustomerInfo(request)
 * ```
 *
 * Example - Email verification:
 * ```kotlin
 * // User receives code "123456" via email
 * val request = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId,
 *     verificationFields = mapOf(
 *         "email_address_verification" to "123456",
 *         "mobile_number_verification" to "654321"
 *     )
 * )
 *
 * val response = kycService.putCustomerInfo(request)
 * ```
 *
 * Example - File references (two-step upload):
 * ```kotlin
 * // Step 1: Upload file separately
 * val fileResponse = kycService.postCustomerFile(photoBytes, authToken)
 *
 * // Step 2: Reference file in customer update
 * val request = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId,
 *     fileReferences = mapOf(
 *         "photo_id_front_file_id" to fileResponse.fileId
 *     )
 * )
 *
 * val response = kycService.putCustomerInfo(request)
 * ```
 *
 * Example - Organization KYC:
 * ```kotlin
 * val request = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     type = "sep31-receiver",
 *     kycFields = StandardKYCFields(
 *         organizationKYCFields = OrganizationKYCFields(
 *             name = "Acme Corp",
 *             registrationNumber = "123456789",
 *             directorName = "Jane Smith"
 *         )
 *     )
 * )
 * ```
 *
 * Example - Custom fields:
 * ```kotlin
 * val request = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     customFields = mapOf(
 *         "custom_field_1" to "value1",
 *         "custom_field_2" to "value2"
 *     ),
 *     customFiles = mapOf(
 *         "custom_document" to documentBytes
 *     )
 * )
 * ```
 *
 * Field submission order:
 * - Text fields are submitted first
 * - Binary files must be sent at the end (SEP-12 requirement)
 * - The SDK handles proper multipart/form-data ordering automatically
 *
 * See also:
 * - [PutCustomerInfoResponse] for response details
 * - [StandardKYCFields] for SEP-09 standard fields
 * - [GetCustomerInfoRequest] for checking requirements
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property jwt JWT token from SEP-10 or SEP-45 authentication
 * @property id Customer ID from previous PUT request (optional)
 * @property account Stellar account ID - deprecated, use JWT sub value instead (optional)
 * @property memo Memo for shared accounts (optional)
 * @property memoType Type of memo - deprecated, should always be 'id' (optional)
 * @property type KYC type (e.g., 'sep6-deposit', 'sep31-sender') (optional)
 * @property transactionId Associated transaction ID (optional)
 * @property kycFields SEP-09 standard KYC fields (optional)
 * @property customFields Custom text fields not defined in SEP-09 (optional)
 * @property customFiles Custom binary fields not defined in SEP-09 (optional)
 * @property verificationFields Verification codes with _verification suffix (optional)
 * @property fileReferences File IDs from POST /customer/files with _file_id suffix (optional)
 */
data class PutCustomerInfoRequest(
    val jwt: String,

    val id: String? = null,

    @Deprecated("Use JWT sub value instead. Maintained for backwards compatibility only.")
    val account: String? = null,

    val memo: String? = null,

    @Deprecated("Memos should always be of type id. Maintained for backwards compatibility with outdated clients.")
    val memoType: String? = null,

    val type: String? = null,

    val transactionId: String? = null,

    val kycFields: StandardKYCFields? = null,

    val customFields: Map<String, String>? = null,

    val customFiles: Map<String, ByteArray>? = null,

    val verificationFields: Map<String, String>? = null,

    val fileReferences: Map<String, String>? = null
)
