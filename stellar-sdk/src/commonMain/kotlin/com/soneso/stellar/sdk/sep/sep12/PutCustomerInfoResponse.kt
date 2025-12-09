// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from a PUT /customer request after uploading or updating customer information.
 *
 * Contains the customer ID that should be used for all future requests to check status
 * or update information for this customer. The ID is stable and should be persisted
 * for later use.
 *
 * The response is minimal by design - it only includes the customer ID. To check the
 * current status of the customer's KYC process, make a GET /customer request using
 * this ID.
 *
 * Example - Initial registration:
 * ```kotlin
 * val putRequest = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     kycFields = StandardKYCFields(
 *         naturalPersonKYCFields = NaturalPersonKYCFields(
 *             firstName = "John",
 *             lastName = "Doe"
 *         )
 *     )
 * )
 *
 * val response = kycService.putCustomerInfo(putRequest)
 * println("Customer registered with ID: ${response.id}")
 *
 * // Save this ID for future requests
 * saveCustomerId(response.id)
 * ```
 *
 * Example - Update existing customer:
 * ```kotlin
 * val putRequest = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     id = savedCustomerId, // Use ID from previous registration
 *     kycFields = StandardKYCFields(
 *         naturalPersonKYCFields = NaturalPersonKYCFields(
 *             emailAddress = "newemail@example.com"
 *         )
 *     )
 * )
 *
 * val response = kycService.putCustomerInfo(putRequest)
 * // Same ID returned
 * assert(response.id == savedCustomerId)
 * ```
 *
 * Example - Check status after submission:
 * ```kotlin
 * val putResponse = kycService.putCustomerInfo(putRequest)
 * val customerId = putResponse.id
 *
 * // Check current status
 * val getRequest = GetCustomerInfoRequest(
 *     jwt = authToken,
 *     id = customerId
 * )
 * val statusResponse = kycService.getCustomerInfo(getRequest)
 * println("Status: ${statusResponse.status}")
 * ```
 *
 * See also:
 * - [PutCustomerInfoRequest] for request parameters
 * - [GetCustomerInfoRequest] for checking customer status
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property id Unique identifier for the created or updated customer
 */
@Serializable
data class PutCustomerInfoResponse(
    @SerialName("id")
    val id: String
)
