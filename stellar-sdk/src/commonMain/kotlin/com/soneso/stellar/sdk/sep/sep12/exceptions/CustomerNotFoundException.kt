// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12.exceptions

/**
 * Exception thrown when a customer is not found (HTTP 404).
 *
 * Indicates that no customer record exists for the specified account/memo combination
 * or customer ID. This typically occurs when:
 * - Attempting to retrieve info for an unregistered customer
 * - Using an incorrect customer ID
 * - Account/memo combination doesn't match any customer
 * - Customer was deleted (GDPR request)
 *
 * Recovery actions:
 * - Register the customer via PUT /customer
 * - Verify the account ID and memo are correct
 * - Check if customer ID is still valid
 *
 * Example - Handle customer not found:
 * ```kotlin
 * try {
 *     val info = kycService.getCustomerInfo(
 *         GetCustomerInfoRequest(
 *             jwt = authToken,
 *             id = customerId
 *         )
 *     )
 * } catch (e: CustomerNotFoundException) {
 *     println("Customer ${e.accountId} not found, registering...")
 *
 *     // Register new customer
 *     val putRequest = PutCustomerInfoRequest(
 *         jwt = authToken,
 *         account = e.accountId,
 *         kycFields = StandardKYCFields(...)
 *     )
 *     val response = kycService.putCustomerInfo(putRequest)
 *     println("Registered with ID: ${response.id}")
 * }
 * ```
 *
 * Example - Verify customer exists before update:
 * ```kotlin
 * fun updateCustomer(customerId: String, newEmail: String) {
 *     try {
 *         // Check if customer exists
 *         val info = kycService.getCustomerInfo(
 *             GetCustomerInfoRequest(jwt = authToken, id = customerId)
 *         )
 *
 *         // Customer exists, update email
 *         kycService.putCustomerInfo(
 *             PutCustomerInfoRequest(
 *                 jwt = authToken,
 *                 id = customerId,
 *                 kycFields = StandardKYCFields(
 *                     naturalPersonKYCFields = NaturalPersonKYCFields(
 *                         emailAddress = newEmail
 *                     )
 *                 )
 *             )
 *         )
 *     } catch (e: CustomerNotFoundException) {
 *         println("Customer no longer exists: ${e.accountId}")
 *     }
 * }
 * ```
 *
 * See also:
 * - [KYCException] base class
 * - [PutCustomerInfoRequest] for registering customers
 * - [GetCustomerInfoRequest] for checking customer status
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property accountId The Stellar account ID or customer ID that was not found
 */
class CustomerNotFoundException(
    val accountId: String
) : KYCException(
    message = "Customer not found for account: $accountId. " +
            "This customer may not be registered yet or the customer ID is invalid."
)
