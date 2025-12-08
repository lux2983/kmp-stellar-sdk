// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12.exceptions

/**
 * Exception thrown when attempting to create a customer that already exists (HTTP 409).
 *
 * Indicates a conflict where a customer record already exists for the specified account/memo
 * combination. This typically occurs when:
 * - Attempting to register a customer that is already registered
 * - Account/memo combination is already in use
 * - Duplicate registration attempt
 *
 * Note: Most PUT /customer requests are idempotent and should not throw this error.
 * This exception is only thrown in specific scenarios where the anchor enforces
 * strict uniqueness constraints.
 *
 * Recovery actions:
 * - Use the existing customer ID for updates
 * - Query customer info to get the existing customer ID
 * - Use PUT /customer with the existing ID to update information
 *
 * Example - Handle duplicate customer:
 * ```kotlin
 * suspend fun registerCustomer(
 *     kycService: KYCService,
 *     accountId: String,
 *     kycFields: StandardKYCFields,
 *     jwt: String
 * ): String {
 *     try {
 *         val response = kycService.putCustomerInfo(
 *             PutCustomerInfoRequest(
 *                 jwt = jwt,
 *                 account = accountId,
 *                 kycFields = kycFields
 *             )
 *         )
 *         return response.id
 *     } catch (e: CustomerAlreadyExistsException) {
 *         println("Customer already exists")
 *
 *         // Get existing customer ID
 *         val customerId = e.existingCustomerId
 *             ?: getExistingCustomerId(kycService, accountId, jwt)
 *
 *         println("Using existing customer ID: $customerId")
 *
 *         // Update existing customer instead
 *         val updateResponse = kycService.putCustomerInfo(
 *             PutCustomerInfoRequest(
 *                 jwt = jwt,
 *                 id = customerId,
 *                 kycFields = kycFields
 *             )
 *         )
 *         return updateResponse.id
 *     }
 * }
 *
 * suspend fun getExistingCustomerId(
 *     kycService: KYCService,
 *     accountId: String,
 *     jwt: String
 * ): String {
 *     val info = kycService.getCustomerInfo(
 *         GetCustomerInfoRequest(jwt = jwt, account = accountId)
 *     )
 *     return info.id ?: throw IllegalStateException("Customer exists but has no ID")
 * }
 * ```
 *
 * Example - Check if customer exists first:
 * ```kotlin
 * suspend fun ensureCustomerExists(
 *     kycService: KYCService,
 *     accountId: String,
 *     kycFields: StandardKYCFields,
 *     jwt: String
 * ): String {
 *     // Check if customer already exists
 *     return try {
 *         val info = kycService.getCustomerInfo(
 *             GetCustomerInfoRequest(jwt = jwt, account = accountId)
 *         )
 *
 *         if (info.id != null) {
 *             println("Customer already exists: ${info.id}")
 *             info.id
 *         } else {
 *             // Customer exists but no ID, register
 *             registerNewCustomer(kycService, accountId, kycFields, jwt)
 *         }
 *     } catch (e: CustomerNotFoundException) {
 *         // Customer doesn't exist, register
 *         registerNewCustomer(kycService, accountId, kycFields, jwt)
 *     }
 * }
 *
 * suspend fun registerNewCustomer(
 *     kycService: KYCService,
 *     accountId: String,
 *     kycFields: StandardKYCFields,
 *     jwt: String
 * ): String {
 *     val response = kycService.putCustomerInfo(
 *         PutCustomerInfoRequest(
 *             jwt = jwt,
 *             account = accountId,
 *             kycFields = kycFields
 *         )
 *     )
 *     return response.id
 * }
 * ```
 *
 * See also:
 * - [KYCException] base class
 * - [CustomerNotFoundException] for non-existent customers
 * - [GetCustomerInfoRequest] for checking customer existence
 * - [PutCustomerInfoRequest] for creating/updating customers
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property existingCustomerId The ID of the existing customer (null if not provided by server)
 */
class CustomerAlreadyExistsException(
    val existingCustomerId: String? = null
) : KYCException(
    message = buildString {
        append("Customer already exists (409 Conflict). ")
        if (existingCustomerId != null) {
            append("Existing customer ID: $existingCustomerId. ")
        }
        append("Use the existing customer ID to update information instead of creating a new customer.")
    }
)
