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
 * Transaction kinds for SEP-6 deposit and withdrawal operations.
 *
 * These kinds identify the type of operation being performed by a SEP-6 transaction.
 * Standard operations transfer assets directly, while exchange operations involve
 * conversion between different assets using SEP-38 quotes.
 *
 * @property value The string value used in SEP-6 API responses.
 * @see <a href="https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0006.md">SEP-6 Specification</a>
 */
public enum class Sep06TransactionKind(public val value: String) {
    /**
     * Standard deposit operation.
     *
     * The user sends an off-chain asset (e.g., fiat currency) to the anchor,
     * and receives the same asset on the Stellar network.
     */
    DEPOSIT("deposit"),

    /**
     * Standard withdrawal operation.
     *
     * The user sends an asset on the Stellar network to the anchor,
     * and receives the same off-chain asset (e.g., fiat currency).
     */
    WITHDRAWAL("withdrawal"),

    /**
     * Deposit with asset exchange.
     *
     * The user sends one off-chain asset (e.g., USD) to the anchor,
     * and receives a different asset on the Stellar network (e.g., USDC).
     * Uses SEP-38 quotes for exchange rate determination.
     */
    DEPOSIT_EXCHANGE("deposit-exchange"),

    /**
     * Withdrawal with asset exchange.
     *
     * The user sends one asset on the Stellar network to the anchor,
     * and receives a different off-chain asset (e.g., EUR instead of EURC).
     * Uses SEP-38 quotes for exchange rate determination.
     */
    WITHDRAWAL_EXCHANGE("withdrawal-exchange");

    /**
     * Checks if this kind represents a deposit operation.
     *
     * @return True if this is a deposit or deposit-exchange, false otherwise.
     */
    public fun isDeposit(): Boolean = this == DEPOSIT || this == DEPOSIT_EXCHANGE

    /**
     * Checks if this kind represents a withdrawal operation.
     *
     * @return True if this is a withdrawal or withdrawal-exchange, false otherwise.
     */
    public fun isWithdrawal(): Boolean = this == WITHDRAWAL || this == WITHDRAWAL_EXCHANGE

    /**
     * Checks if this kind involves asset exchange.
     *
     * Exchange operations use SEP-38 quotes to convert between different assets.
     *
     * @return True if this is a deposit-exchange or withdrawal-exchange, false otherwise.
     */
    public fun isExchange(): Boolean = this == DEPOSIT_EXCHANGE || this == WITHDRAWAL_EXCHANGE

    public companion object {
        /**
         * Finds the [Sep06TransactionKind] corresponding to the given string value.
         *
         * @param value The string value from a SEP-6 API response.
         * @return The matching kind, or null if not found.
         */
        public fun fromValue(value: String): Sep06TransactionKind? =
            entries.find { it.value == value }
    }
}
