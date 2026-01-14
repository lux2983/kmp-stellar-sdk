/*
 * Copyright 2026 Soneso
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soneso.stellar.sdk.sep.sep06

/**
 * Transaction statuses for SEP-6 deposit and withdrawal operations.
 *
 * These statuses represent the lifecycle states of a programmatic anchor transaction.
 * A transaction progresses through various pending states before reaching a terminal state.
 *
 * Unlike SEP-24 which uses interactive web flows, SEP-6 transactions are managed
 * programmatically through API calls.
 *
 * @property value The string value used in SEP-6 API responses.
 * @see <a href="https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md">SEP-6 Specification</a>
 */
public enum class Sep06TransactionStatus(public val value: String) {
    /**
     * The anchor has not yet received all required information from the user.
     * Additional fields may need to be submitted via the deposit or withdraw endpoint.
     */
    INCOMPLETE("incomplete"),

    /**
     * For deposits: the user has been instructed to send funds to the anchor.
     * For withdrawals: the anchor is waiting to receive funds from the Stellar network.
     */
    PENDING_USER_TRANSFER_START("pending_user_transfer_start"),

    /**
     * For deposits: funds have been received by the anchor.
     * For withdrawals: the user has submitted the Stellar transaction.
     */
    PENDING_USER_TRANSFER_COMPLETE("pending_user_transfer_complete"),

    /**
     * Deposit/withdrawal is being processed by an external system (e.g., bank, payment processor).
     */
    PENDING_EXTERNAL("pending_external"),

    /**
     * Deposit/withdrawal operation is being processed internally by the anchor.
     */
    PENDING_ANCHOR("pending_anchor"),

    /**
     * The anchor is waiting for the Stellar network transaction to complete.
     */
    PENDING_STELLAR("pending_stellar"),

    /**
     * The user must establish a trustline for the asset before the anchor can complete the deposit.
     */
    PENDING_TRUST("pending_trust"),

    /**
     * The anchor is waiting for the user to take some action.
     * Check the transaction's message field for details.
     */
    PENDING_USER("pending_user"),

    /**
     * The anchor is waiting for the user to update their KYC information via SEP-12.
     * The required_info_updates field contains the fields that need updating.
     */
    PENDING_CUSTOMER_INFO_UPDATE("pending_customer_info_update"),

    /**
     * The anchor is waiting for the user to provide additional transaction information.
     * The required_info_updates field contains the fields that need updating.
     */
    PENDING_TRANSACTION_INFO_UPDATE("pending_transaction_info_update"),

    /**
     * The deposit/withdrawal has completed successfully.
     * This is a terminal status.
     */
    COMPLETED("completed"),

    /**
     * The deposit/withdrawal has been refunded.
     * This is a terminal status.
     */
    REFUNDED("refunded"),

    /**
     * The transaction has expired. The user did not complete the required action in time.
     * This is a terminal status.
     */
    EXPIRED("expired"),

    /**
     * An error occurred processing the transaction.
     * Check the transaction's message field for details.
     * This is a terminal status.
     */
    ERROR("error"),

    /**
     * Could not complete the transaction because there is no market for this asset pair.
     * This is a terminal status and indicates an error condition.
     */
    NO_MARKET("no_market"),

    /**
     * Could not complete the transaction because the amount is too small.
     * This is a terminal status and indicates an error condition.
     */
    TOO_SMALL("too_small"),

    /**
     * Could not complete the transaction because the amount is too large.
     * This is a terminal status and indicates an error condition.
     */
    TOO_LARGE("too_large");

    /**
     * Checks if this status represents a terminal state.
     *
     * Terminal statuses indicate the transaction has reached a final state.
     * No further status updates are expected for transactions in these states.
     *
     * @return True if this status is terminal, false otherwise.
     */
    public fun isTerminal(): Boolean = this in terminalStatuses

    /**
     * Checks if this status represents an error condition.
     *
     * Error statuses indicate the transaction could not be completed due to an error.
     * These are all terminal statuses.
     *
     * @return True if this status represents an error, false otherwise.
     */
    public fun isError(): Boolean = this in errorStatuses

    /**
     * Checks if this status represents a pending state.
     *
     * Pending statuses indicate the transaction is still in progress and
     * waiting for some action or processing to complete.
     *
     * @return True if this status is a pending state, false otherwise.
     */
    public fun isPending(): Boolean = this in pendingStatuses

    public companion object {
        /**
         * Finds the [Sep06TransactionStatus] corresponding to the given string value.
         *
         * @param value The string value from a SEP-6 API response.
         * @return The matching status, or null if not found.
         */
        public fun fromValue(value: String): Sep06TransactionStatus? =
            entries.find { it.value == value }

        /**
         * Set of all terminal statuses that indicate the transaction has reached a final state.
         * No further status updates are expected for transactions in these states.
         */
        public val terminalStatuses: Set<Sep06TransactionStatus> = setOf(
            COMPLETED, REFUNDED, EXPIRED, ERROR, NO_MARKET, TOO_SMALL, TOO_LARGE
        )

        /**
         * Set of all error statuses that indicate the transaction failed.
         * These are all terminal statuses.
         */
        public val errorStatuses: Set<Sep06TransactionStatus> = setOf(
            ERROR, NO_MARKET, TOO_SMALL, TOO_LARGE
        )

        /**
         * Set of all pending statuses that indicate the transaction is still in progress.
         */
        public val pendingStatuses: Set<Sep06TransactionStatus> = setOf(
            PENDING_USER_TRANSFER_START,
            PENDING_USER_TRANSFER_COMPLETE,
            PENDING_EXTERNAL,
            PENDING_ANCHOR,
            PENDING_STELLAR,
            PENDING_TRUST,
            PENDING_USER,
            PENDING_CUSTOMER_INFO_UPDATE,
            PENDING_TRANSACTION_INFO_UPDATE
        )

        /**
         * Checks if the given status string represents a terminal state.
         *
         * @param status The status string to check.
         * @return True if the status is terminal, false otherwise.
         */
        public fun isTerminal(status: String): Boolean =
            fromValue(status) in terminalStatuses
    }
}
