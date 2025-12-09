// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from a POST /customer/files request or part of GET /customer/files response.
 *
 * Contains metadata about an uploaded file including its unique identifier which can
 * be used in subsequent PUT /customer requests. This enables a two-step upload process
 * where files are uploaded separately and then referenced by ID.
 *
 * File upload workflow:
 * 1. Upload file via POST /customer/files
 * 2. Receive CustomerFileResponse with file_id
 * 3. Use file_id in PUT /customer request with _file_id suffix
 *
 * Response properties:
 * - [fileId]: Unique identifier for referencing the file
 * - [contentType]: MIME type of the uploaded file
 * - [size]: File size in bytes
 * - [customerId]: Optional customer ID if file is associated with a customer
 *
 * Example - Upload and reference file:
 * ```kotlin
 * // Step 1: Upload file separately
 * val photoBytes = loadImageBytes("passport_front.jpg")
 * val fileResponse = kycService.postCustomerFile(photoBytes, authToken)
 *
 * println("File uploaded:")
 * println("  ID: ${fileResponse.fileId}")
 * println("  Type: ${fileResponse.contentType}")
 * println("  Size: ${fileResponse.size} bytes")
 *
 * // Step 2: Reference file in customer update
 * val putRequest = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId,
 *     fileReferences = mapOf(
 *         "photo_id_front_file_id" to fileResponse.fileId
 *     )
 * )
 * kycService.putCustomerInfo(putRequest)
 * ```
 *
 * Example - Upload multiple files:
 * ```kotlin
 * val idFront = kycService.postCustomerFile(idFrontBytes, authToken)
 * val idBack = kycService.postCustomerFile(idBackBytes, authToken)
 * val proofAddress = kycService.postCustomerFile(proofBytes, authToken)
 *
 * println("Uploaded ${listOf(idFront, idBack, proofAddress).size} files")
 * println("Total size: ${idFront.size + idBack.size + proofAddress.size} bytes")
 *
 * val putRequest = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     fileReferences = mapOf(
 *         "photo_id_front_file_id" to idFront.fileId,
 *         "photo_id_back_file_id" to idBack.fileId,
 *         "photo_proof_address_file_id" to proofAddress.fileId
 *     )
 * )
 * kycService.putCustomerInfo(putRequest)
 * ```
 *
 * Example - Get file information:
 * ```kotlin
 * val filesResponse = kycService.getCustomerFiles(
 *     jwt = authToken,
 *     customerId = customerId
 * )
 *
 * filesResponse.files.forEach { file ->
 *     println("File ${file.fileId}:")
 *     println("  Type: ${file.contentType}")
 *     println("  Size: ${file.size} bytes")
 *     file.customerId?.let { println("  Customer: $it") }
 * }
 * ```
 *
 * Common MIME types:
 * - Image files: image/jpeg, image/png, image/gif
 * - Document files: application/pdf
 * - Other: application/octet-stream
 *
 * See also:
 * - [GetCustomerFilesResponse] for listing multiple files
 * - [PutCustomerInfoRequest] for referencing files via fileReferences
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md#customer-files)
 *
 * @property fileId Unique identifier for the uploaded file
 * @property contentType MIME type of the uploaded file
 * @property size File size in bytes
 * @property customerId Optional customer ID this file is associated with
 * @property expiresAt Optional expiration time in UTC ISO 8601 format when the file will be deleted
 */
@Serializable
data class CustomerFileResponse(
    @SerialName("file_id")
    val fileId: String,

    @SerialName("content_type")
    val contentType: String,

    @SerialName("size")
    val size: Long,

    @SerialName("customer_id")
    val customerId: String? = null,

    @SerialName("expires_at")
    val expiresAt: String? = null
)
