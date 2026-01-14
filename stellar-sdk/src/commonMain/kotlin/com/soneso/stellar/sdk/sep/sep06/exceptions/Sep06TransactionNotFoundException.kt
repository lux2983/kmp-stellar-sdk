// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06.exceptions

/**
 * Exception thrown when a SEP-6 transaction is not found (HTTP 404).
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
 *     sep06Service: Sep06Service,
 *     transactionId: String,
 *     jwt: String
 * ): Sep06TransactionResponse? {
 *     try {
 *         return sep06Service.getTransaction(transactionId, jwt)
 *     } catch (e: Sep06TransactionNotFoundException) {
 *         println("Transaction not found: ${e.transactionId ?: transactionId}")
 *         return null
 *     }
 * }
 * ```
 *
 * Example - List transactions instead:
 * ```kotlin
 * suspend fun findTransaction(
 *     sep06Service: Sep06Service,
 *     transactionId: String,
 *     assetCode: String,
 *     jwt: String
 * ): Sep06TransactionResponse? {
 *     try {
 *         return sep06Service.getTransaction(transactionId, jwt)
 *     } catch (e: Sep06TransactionNotFoundException) {
 *         println("Transaction ${e.transactionId} not found, checking history...")
 *
 *         // Fall back to listing transactions
 *         val transactions = sep06Service.getTransactions(assetCode, jwt)
 *         return transactions.find { it.id == transactionId }
 *     }
 * }
 * ```
 *
 * See also:
 * - [Sep06Exception] base class
 * - [SEP-0006 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md)
 *
 * @property transactionId The transaction ID that was not found, if available
 */
class Sep06TransactionNotFoundException(
    val transactionId: String? = null
) : Sep06Exception(
    message = "Transaction not found${transactionId?.let { ": $it" } ?: ""}"
) {
    override fun toString(): String {
        return "SEP-06 transaction not found${transactionId?.let { ": $it" } ?: ""}"
    }
}
