// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12.exceptions

/**
 * Exception thrown when an uploaded file exceeds the server's size limit (HTTP 413).
 *
 * Indicates that the file being uploaded is too large. Different anchors may have
 * different size limits for uploaded documents. This error typically occurs when:
 * - Document/image file exceeds maximum allowed size
 * - Multiple files exceed cumulative size limit
 * - Uncompressed or high-resolution images
 *
 * Recovery actions:
 * - Compress the file to reduce size
 * - Resize images to lower resolution
 * - Convert to more efficient format (e.g., JPEG instead of PNG)
 * - Split large documents into multiple files
 * - Contact anchor support to understand size limits
 *
 * Example - Handle file too large:
 * ```kotlin
 * suspend fun uploadDocument(
 *     kycService: KYCService,
 *     documentBytes: ByteArray,
 *     jwt: String
 * ): CustomerFileResponse {
 *     try {
 *         return kycService.postCustomerFile(documentBytes, jwt)
 *     } catch (e: FileTooLargeException) {
 *         println("File too large: ${e.fileSize} bytes")
 *         println("Please compress or resize the file")
 *
 *         // Attempt to compress image
 *         val compressedBytes = compressImage(documentBytes)
 *         println("Compressed to: ${compressedBytes.size} bytes")
 *
 *         // Retry with compressed file
 *         return kycService.postCustomerFile(compressedBytes, jwt)
 *     }
 * }
 * ```
 *
 * Example - Pre-check file size:
 * ```kotlin
 * fun validateFileSize(fileBytes: ByteArray, maxSizeMB: Int = 5): Boolean {
 *     val maxSizeBytes = maxSizeMB * 1024 * 1024
 *     if (fileBytes.size > maxSizeBytes) {
 *         println("File too large: ${fileBytes.size} bytes (max: $maxSizeBytes)")
 *         return false
 *     }
 *     return true
 * }
 *
 * suspend fun uploadWithValidation(
 *     kycService: KYCService,
 *     fileBytes: ByteArray,
 *     jwt: String
 * ) {
 *     if (!validateFileSize(fileBytes, maxSizeMB = 5)) {
 *         throw FileTooLargeException(fileBytes.size.toLong())
 *     }
 *
 *     kycService.postCustomerFile(fileBytes, jwt)
 * }
 * ```
 *
 * Example - Compress before upload:
 * ```kotlin
 * suspend fun uploadImage(
 *     kycService: KYCService,
 *     imageBytes: ByteArray,
 *     jwt: String,
 *     maxSizeBytes: Long = 5_000_000 // 5 MB
 * ): CustomerFileResponse {
 *     var bytes = imageBytes
 *
 *     // Compress if too large
 *     if (bytes.size > maxSizeBytes) {
 *         println("Image too large (${bytes.size} bytes), compressing...")
 *         bytes = compressImage(bytes, quality = 0.8)
 *         println("Compressed to ${bytes.size} bytes")
 *     }
 *
 *     return try {
 *         kycService.postCustomerFile(bytes, jwt)
 *     } catch (e: FileTooLargeException) {
 *         // If still too large after compression, try lower quality
 *         println("Still too large, reducing quality further...")
 *         bytes = compressImage(imageBytes, quality = 0.5)
 *         kycService.postCustomerFile(bytes, jwt)
 *     }
 * }
 * ```
 *
 * Example - Show user-friendly error:
 * ```kotlin
 * fun showFileSizeError(exception: FileTooLargeException, filename: String) {
 *     val sizeMB = (exception.fileSize ?: 0) / (1024.0 * 1024.0)
 *     showError(
 *         title = "File Too Large",
 *         message = buildString {
 *             append("The file '$filename' is too large ")
 *             if (exception.fileSize != null) {
 *                 append("(%.2f MB).\n\n".format(sizeMB))
 *             } else {
 *                 append(".\n\n")
 *             }
 *             append("Please try:\n")
 *             append("• Compressing the file\n")
 *             append("• Reducing image resolution\n")
 *             append("• Converting to a smaller format (JPEG)")
 *         }
 *     )
 * }
 * ```
 *
 * See also:
 * - [KYCException] base class
 * - [CustomerFileResponse] for successful uploads
 * - [PutCustomerInfoRequest] for direct file upload (alternative)
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property fileSize The size of the file that was rejected, in bytes (null if unknown)
 */
class FileTooLargeException(
    val fileSize: Long? = null
) : KYCException(
    message = buildString {
        append("File too large (413 Payload Too Large). ")
        if (fileSize != null) {
            val sizeMB = fileSize / (1024.0 * 1024.0)
            append("File size: %.2f MB. ".format(sizeMB))
        }
        append("Please compress the file or reduce its size before uploading.")
    }
)
