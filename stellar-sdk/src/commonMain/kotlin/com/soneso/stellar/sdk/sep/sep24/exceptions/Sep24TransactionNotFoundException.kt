// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep24.exceptions

/**
 * Exception thrown when a SEP-24 transaction is not found (HTTP 404).
 *
 * Indicates that the requested transaction ID does not exist or is not accessible
 * to the authenticated user. This error typically occurs when:
 * - Transaction ID does not exist
 * - Transaction belongs to a different user account
 * - Invalid transaction ID format
 * - Transaction has been purged from the anchor's system
 *
 * Recovery actions:
 * - Verify the transaction ID is correct
 * - Ensure the authenticated account owns the transaction
 * - Check transaction history for valid transaction IDs
 * - Initiate a new deposit or withdrawal if the transaction is no longer available
 *
 * Example - Handle transaction not found:
 * ```kotlin
 * suspend fun getTransactionStatus(
 *     sep24Service: Sep24Service,
 *     transactionId: String,
 *     jwt: String
 * ): Sep24TransactionResponse? {
 *     try {
 *         return sep24Service.getTransaction(transactionId, jwt)
 *     } catch (e: Sep24TransactionNotFoundException) {
 *         println("Transaction not found: ${e.transactionId ?: transactionId}")
 *         return null
 *     }
 * }
 * ```
 *
 * Example - List transactions instead:
 * ```kotlin
 * suspend fun findTransaction(
 *     sep24Service: Sep24Service,
 *     transactionId: String,
 *     assetCode: String,
 *     jwt: String
 * ): Sep24TransactionResponse? {
 *     try {
 *         return sep24Service.getTransaction(transactionId, jwt)
 *     } catch (e: Sep24TransactionNotFoundException) {
 *         println("Transaction ${e.transactionId} not found, checking history...")
 *
 *         // Fall back to listing transactions
 *         val transactions = sep24Service.getTransactions(assetCode, jwt)
 *         return transactions.find { it.id == transactionId }
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep24Exception] base class
 * - [SEP-0024 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0024.md)
 *
 * @property transactionId The transaction ID that was not found, if available
 */
class Sep24TransactionNotFoundException(
    val transactionId: String? = null
) : Sep24Exception(
    message = "Transaction not found${transactionId?.let { ": $it" } ?: ""}"
) {
    override fun toString(): String {
        return "SEP-24 transaction not found${transactionId?.let { ": $it" } ?: ""}"
    }
}
