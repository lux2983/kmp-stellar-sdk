// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

/**
 * Request for verifying customer information fields using confirmation codes.
 *
 * DEPRECATED: This endpoint is deprecated as of SEP-12 v1.12.0.
 * Use [PutCustomerInfoRequest] with [verificationFields] instead.
 *
 * This was previously used to submit verification codes sent by the anchor to
 * confirm contact information such as email addresses or phone numbers. The
 * functionality has been merged into the main PUT /customer endpoint.
 *
 * Migration example:
 * ```kotlin
 * // OLD (deprecated):
 * val oldRequest = PutCustomerVerificationRequest(
 *     jwt = authToken,
 *     id = customerId,
 *     verificationFields = mapOf(
 *         "email_address_verification" to "123456"
 *     )
 * )
 * kycService.putCustomerVerification(oldRequest)
 *
 * // NEW (recommended):
 * val newRequest = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId,
 *     verificationFields = mapOf(
 *         "email_address_verification" to "123456"
 *     )
 * )
 * kycService.putCustomerInfo(newRequest)
 * ```
 *
 * See also:
 * - [PutCustomerInfoRequest] for the replacement endpoint
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property jwt JWT token from SEP-10 or SEP-45 authentication
 * @property id Customer ID from previous PUT request
 * @property verificationFields Verification codes with _verification suffix
 */
@Deprecated(
    message = "Deprecated in SEP-12 v1.12.0. Use PutCustomerInfoRequest with verificationFields instead.",
    replaceWith = ReplaceWith(
        "PutCustomerInfoRequest(jwt = jwt, id = id, verificationFields = verificationFields)",
        "com.soneso.stellar.sdk.sep.sep12.PutCustomerInfoRequest"
    )
)
data class PutCustomerVerificationRequest(
    val jwt: String,

    val id: String,

    val verificationFields: Map<String, String>
)
