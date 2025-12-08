// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from a GET /customer/files request containing information about uploaded files.
 *
 * Returns a list of file metadata for files uploaded via POST /customer/files.
 * Can be used to retrieve information about a specific file, all files for a customer,
 * or all files for the authenticated account.
 *
 * Query options:
 * - Provide file_id to get information about a single file
 * - Provide customer_id to get all files for a customer
 * - Provide neither to get all files for the authenticated account
 *
 * Example - Get info about a specific file:
 * ```kotlin
 * // After uploading a file
 * val uploadResponse = kycService.postCustomerFile(fileBytes, authToken)
 * val fileId = uploadResponse.fileId
 *
 * // Later, retrieve information about that file
 * val filesResponse = kycService.getCustomerFiles(
 *     jwt = authToken,
 *     fileId = fileId
 * )
 *
 * val file = filesResponse.files.first()
 * println("File ${file.fileId}:")
 * println("  Content Type: ${file.contentType}")
 * println("  Size: ${file.size} bytes")
 * file.customerId?.let { println("  Customer: $it") }
 * ```
 *
 * Example - Get all files for a customer:
 * ```kotlin
 * val filesResponse = kycService.getCustomerFiles(
 *     jwt = authToken,
 *     customerId = customerId
 * )
 *
 * println("Customer has ${filesResponse.files.size} uploaded files:")
 * filesResponse.files.forEach { file ->
 *     println("- ${file.fileId}: ${file.contentType} (${file.size} bytes)")
 * }
 * ```
 *
 * Example - Get all files for account:
 * ```kotlin
 * val filesResponse = kycService.getCustomerFiles(jwt = authToken)
 *
 * println("Total files: ${filesResponse.files.size}")
 * val totalSize = filesResponse.files.sumOf { it.size }
 * println("Total size: $totalSize bytes")
 *
 * // Group by content type
 * val byType = filesResponse.files.groupBy { it.contentType }
 * byType.forEach { (type, files) ->
 *     println("$type: ${files.size} files")
 * }
 * ```
 *
 * Example - Check file associations:
 * ```kotlin
 * val filesResponse = kycService.getCustomerFiles(jwt = authToken)
 *
 * val associated = filesResponse.files.filter { it.customerId != null }
 * val unassociated = filesResponse.files.filter { it.customerId == null }
 *
 * println("Associated with customers: ${associated.size}")
 * println("Not yet associated: ${unassociated.size}")
 * ```
 *
 * See also:
 * - [CustomerFileResponse] for individual file metadata
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md#customer-files)
 *
 * @property files List of file metadata for uploaded customer documents
 */
@Serializable
data class GetCustomerFilesResponse(
    @SerialName("files")
    val files: List<CustomerFileResponse>
)
