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

package com.soneso.stellar.sdk.sep.sep24

/**
 * Transaction statuses for SEP-24 deposit and withdrawal operations.
 *
 * These statuses represent the lifecycle states of an interactive anchor transaction.
 * A transaction progresses through various pending states before reaching a terminal state.
 *
 * @property value The string value used in SEP-24 API responses.
 * @see <a href="https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0024.md">SEP-24 Specification</a>
 */
public enum class Sep24TransactionStatus(public val value: String) {
    /**
     * The user has not yet completed the interactive flow.
     * The deposit/withdrawal is still in progress.
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
     * Deposit/withdrawal is being processed by an external system (e.g., bank).
     */
    PENDING_EXTERNAL("pending_external"),

    /**
     * Deposit/withdrawal operation is being processed internally by the anchor.
     */
    PENDING_ANCHOR("pending_anchor"),

    /**
     * Deposit/withdrawal has been placed on hold by the anchor.
     * The user may need to contact support or provide additional information.
     */
    ON_HOLD("on_hold"),

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
     * Could not complete the transaction because there is no market for this asset.
     * This is a terminal status.
     */
    NO_MARKET("no_market"),

    /**
     * Could not complete the transaction because the amount is too small.
     * This is a terminal status.
     */
    TOO_SMALL("too_small"),

    /**
     * Could not complete the transaction because the amount is too large.
     * This is a terminal status.
     */
    TOO_LARGE("too_large"),

    /**
     * An error occurred processing the transaction.
     * Check the transaction's message field for details.
     * This is a terminal status.
     */
    ERROR("error");

    public companion object {
        /**
         * Finds the [Sep24TransactionStatus] corresponding to the given string value.
         *
         * @param value The string value from a SEP-24 API response.
         * @return The matching status, or null if not found.
         */
        public fun fromValue(value: String): Sep24TransactionStatus? =
            entries.find { it.value == value }

        /**
         * Set of all terminal statuses that indicate the transaction has reached a final state.
         * No further status updates are expected for transactions in these states.
         */
        public val terminalStatuses: Set<Sep24TransactionStatus> = setOf(
            COMPLETED, REFUNDED, EXPIRED, ERROR, NO_MARKET, TOO_SMALL, TOO_LARGE
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
