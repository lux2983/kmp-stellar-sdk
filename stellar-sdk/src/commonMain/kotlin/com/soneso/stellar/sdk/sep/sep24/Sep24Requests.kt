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
 * Request to initiate an interactive deposit flow with a Stellar anchor.
 *
 * The anchor will return an interactive URL where the user can complete the deposit process
 * through a web interface. After the user completes the flow, the anchor will send the
 * deposited funds to the specified Stellar account.
 *
 * @property assetCode Code of the Stellar asset to deposit (e.g., "USDC", "native" for XLM).
 *     This is the asset the user will receive on Stellar.
 * @property jwt SEP-10 JWT token for authentication. Required for all deposit requests.
 * @property assetIssuer Issuer of the asset. Omit for native XLM. Required when multiple
 *     assets share the same code.
 * @property sourceAsset Off-chain asset the user will send to the anchor, in SEP-38 format
 *     (e.g., "iso4217:USD"). Used when the deposit involves a currency conversion.
 * @property amount Amount the user intends to deposit. The anchor may adjust this during
 *     the interactive flow.
 * @property quoteId SEP-38 quote ID for firm pricing. When provided, the anchor must honor
 *     the quoted price for the deposit.
 * @property account Stellar account (G...), muxed account (M...), or contract (C...) that
 *     will receive the deposited funds. If omitted, the account from the SEP-10 JWT is used.
 * @property memo Memo to attach to the Stellar payment transaction. Used to identify the
 *     deposit recipient when the account is shared.
 * @property memoType Type of the memo: "text", "id", or "hash".
 * @property walletName Name of the wallet application, displayed to the user during the
 *     interactive flow.
 * @property walletUrl URL of the wallet application. The anchor may redirect the user here
 *     after completing the deposit flow.
 * @property lang Language code (RFC 4646, e.g., "en", "en-US", "es") for the interactive
 *     UI and any returned messages.
 * @property claimableBalanceSupported Set to true if the client supports receiving funds
 *     via claimable balances. Allows deposits to accounts without a trustline.
 * @property customerId SEP-12 customer ID if KYC has already been completed. Allows the
 *     anchor to skip redundant KYC collection.
 * @property kycFields SEP-9 KYC field values as key-value pairs (field name to value).
 *     Allows pre-populating KYC information in the interactive flow.
 * @property kycFiles SEP-9 KYC file uploads as key-value pairs (field name to file bytes).
 *     Used for documents like ID photos or proof of address.
 */
public data class Sep24DepositRequest(
    val assetCode: String,
    val jwt: String,
    val assetIssuer: String? = null,
    val sourceAsset: String? = null,
    val amount: String? = null,
    val quoteId: String? = null,
    val account: String? = null,
    val memo: String? = null,
    val memoType: String? = null,
    val walletName: String? = null,
    val walletUrl: String? = null,
    val lang: String? = null,
    val claimableBalanceSupported: Boolean? = null,
    val customerId: String? = null,
    val kycFields: Map<String, String>? = null,
    val kycFiles: Map<String, ByteArray>? = null
)

/**
 * Request to initiate an interactive withdrawal flow with a Stellar anchor.
 *
 * The anchor will return an interactive URL where the user can complete the withdrawal process
 * through a web interface. After the user sends funds to the anchor's Stellar account, the
 * anchor will deliver the withdrawn assets through the specified off-chain channel.
 *
 * @property assetCode Code of the Stellar asset to withdraw (e.g., "USDC", "native" for XLM).
 *     This is the asset the user will send from their Stellar account.
 * @property jwt SEP-10 JWT token for authentication. Required for all withdrawal requests.
 * @property assetIssuer Issuer of the asset. Omit for native XLM. Required when multiple
 *     assets share the same code.
 * @property destinationAsset Off-chain asset the user will receive, in SEP-38 format
 *     (e.g., "iso4217:USD"). Used when the withdrawal involves a currency conversion.
 * @property amount Amount the user intends to withdraw. The anchor may adjust this during
 *     the interactive flow.
 * @property quoteId SEP-38 quote ID for firm pricing. When provided, the anchor must honor
 *     the quoted price for the withdrawal.
 * @property account Stellar account (G...), muxed account (M...), or contract (C...) that
 *     will send the withdrawal. If omitted, the account from the SEP-10 JWT is used.
 * @property memo Deprecated: Use the sub value in the SEP-10 JWT instead. Previously used
 *     to identify the user when the Stellar account is shared.
 * @property memoType Deprecated: Use the sub value in the SEP-10 JWT instead. Previously
 *     specified the memo type.
 * @property walletName Name of the wallet application, displayed to the user during the
 *     interactive flow.
 * @property walletUrl URL of the wallet application for anchor reference.
 * @property lang Language code (RFC 4646, e.g., "en", "en-US", "es") for the interactive
 *     UI and any returned messages.
 * @property refundMemo Memo to use if the anchor needs to refund the withdrawal. Helps
 *     route refund payments back to the correct user.
 * @property refundMemoType Type of the refund memo: "text", "id", or "hash".
 * @property customerId SEP-12 customer ID if KYC has already been completed. Allows the
 *     anchor to skip redundant KYC collection.
 * @property kycFields SEP-9 KYC field values as key-value pairs (field name to value).
 *     Allows pre-populating KYC information in the interactive flow.
 * @property kycFiles SEP-9 KYC file uploads as key-value pairs (field name to file bytes).
 *     Used for documents like ID photos or proof of address.
 */
public data class Sep24WithdrawRequest(
    val assetCode: String,
    val jwt: String,
    val assetIssuer: String? = null,
    val destinationAsset: String? = null,
    val amount: String? = null,
    val quoteId: String? = null,
    val account: String? = null,
    @Deprecated("Use sub value in SEP-10 JWT instead")
    val memo: String? = null,
    @Deprecated("Use sub value in SEP-10 JWT instead")
    val memoType: String? = null,
    val walletName: String? = null,
    val walletUrl: String? = null,
    val lang: String? = null,
    val refundMemo: String? = null,
    val refundMemoType: String? = null,
    val customerId: String? = null,
    val kycFields: Map<String, String>? = null,
    val kycFiles: Map<String, ByteArray>? = null
)

/**
 * Request to query the fee for a deposit or withdrawal operation.
 *
 * Note: The /fee endpoint is deprecated in SEP-24. Anchors should use SEP-38 quotes
 * for fee calculation instead. This request class is provided for compatibility with
 * anchors that still support the legacy fee endpoint.
 *
 * @property operation Type of operation: "deposit" or "withdraw".
 * @property assetCode Code of the asset for the operation.
 * @property amount Amount for which to calculate the fee.
 * @property jwt SEP-10 JWT token for authentication. Optional as some anchors allow
 *     fee queries without authentication.
 * @property type Type of deposit or withdrawal method. The available types can be found
 *     in the /info response for each asset.
 */
public data class Sep24FeeRequest(
    val operation: String,
    val assetCode: String,
    val amount: String,
    val jwt: String? = null,
    val type: String? = null
)

/**
 * Request to query transaction history for the authenticated account.
 *
 * Returns a list of transactions for the specified asset, with optional filtering
 * by date, transaction kind, and pagination parameters.
 *
 * @property assetCode Code of the asset for which to retrieve transaction history.
 * @property jwt SEP-10 JWT token for authentication. Required to identify the account.
 * @property noOlderThan ISO 8601 UTC datetime string. Only transactions started on or
 *     after this date will be returned (e.g., "2023-01-15T12:00:00Z").
 * @property limit Maximum number of transactions to return. The anchor may impose its
 *     own maximum limit.
 * @property kind Filter by transaction kind: "deposit" or "withdrawal". If omitted,
 *     both types are returned.
 * @property pagingId Pagination cursor. Use the id of the last transaction from a
 *     previous response to fetch the next page.
 * @property lang Language code (RFC 4646) for any returned messages.
 */
public data class Sep24TransactionsRequest(
    val assetCode: String,
    val jwt: String,
    val noOlderThan: String? = null,
    val limit: Int? = null,
    val kind: String? = null,
    val pagingId: String? = null,
    val lang: String? = null
)

/**
 * Request to query a single transaction by its identifier.
 *
 * At least one of [id], [stellarTransactionId], or [externalTransactionId] must be provided
 * to identify the transaction. The anchor will return the transaction details if found.
 *
 * @property jwt SEP-10 JWT token for authentication. Required to verify access to the
 *     transaction.
 * @property id Anchor's unique identifier for the transaction. This is the id returned
 *     in the deposit or withdraw response.
 * @property stellarTransactionId Stellar transaction hash for the on-chain payment.
 *     Useful when querying the status of a completed transaction.
 * @property externalTransactionId External (off-chain) transaction identifier. This is
 *     the identifier from the external payment system (e.g., bank reference).
 * @property lang Language code (RFC 4646) for any returned messages.
 */
public data class Sep24TransactionRequest(
    val jwt: String,
    val id: String? = null,
    val stellarTransactionId: String? = null,
    val externalTransactionId: String? = null,
    val lang: String? = null
)
