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
 * Request to initiate a programmatic deposit with a Stellar anchor.
 *
 * A deposit occurs when a user sends an external asset (fiat via bank transfer, crypto from
 * another blockchain, etc.) to an anchor, and the anchor sends an equivalent amount of the
 * corresponding Stellar asset to the user's account.
 *
 * Unlike SEP-24's interactive flow, SEP-6 deposits are fully programmatic - all required
 * information must be provided in the API request.
 *
 * @property assetCode Code of the on-chain asset the user wants to receive from the anchor
 *     after the off-chain deposit. Must match a code in the /info response's deposit object.
 * @property account Stellar account (G...), muxed account (M...), or contract (C...) that will
 *     receive the deposited funds. May differ from the account authenticated via SEP-10.
 * @property jwt SEP-10 JWT token for authentication. Required for all deposit requests.
 * @property assetIssuer Issuer of the asset. Required when multiple assets share the same code.
 * @property memoType Type of memo the anchor should attach to the Stellar payment transaction:
 *     "text", "id", or "hash".
 * @property memo Value of memo to attach to the transaction. For hash type, this should be
 *     base64-encoded.
 * @property emailAddress Email address of the depositor. The anchor may use this to send
 *     status updates about the deposit.
 * @property type Deprecated: Use [fundingMethod] instead. Type of deposit method (e.g., SEPA,
 *     SWIFT) if the anchor supports multiple deposit methods.
 * @property fundingMethod Method of funding the deposit (replaces deprecated [type] field).
 *     Values come from the /info endpoint's funding_methods for the asset.
 * @property amount Amount the user intends to deposit. May be necessary for the anchor to
 *     determine KYC requirements.
 * @property countryCode ISO 3166-1 alpha-3 code of the user's current address. May be necessary
 *     for the anchor to determine KYC requirements.
 * @property claimableBalanceSupported Set to true if the client supports receiving funds via
 *     claimable balances. Allows deposits to accounts without a trustline.
 * @property customerId SEP-12 customer ID if the off-chain account is already known to the
 *     anchor but not linked to this Stellar account.
 * @property locationId ID of the chosen location for cash drop-off, if applicable.
 * @property walletName Deprecated: Use client_domain in SEP-10 JWT instead. Name of the wallet
 *     application for display purposes.
 * @property walletUrl Deprecated: Use client_domain in SEP-10 JWT instead. URL of the wallet
 *     application.
 * @property lang Language code (RFC 4646, e.g., "en", "es") for error messages and
 *     human-readable content. Defaults to "en".
 * @property onChangeCallback URL where the anchor should POST transaction status updates.
 *     The callback message format matches the /transaction endpoint response.
 * @property extraFields Additional fields required by the anchor beyond the standard parameters.
 *     Keys should match field names from the /info response.
 */
public data class Sep06DepositRequest(
    val assetCode: String,
    val account: String,
    val jwt: String,
    val assetIssuer: String? = null,
    val memoType: String? = null,
    val memo: String? = null,
    val emailAddress: String? = null,
    @Deprecated("Use fundingMethod instead")
    val type: String? = null,
    val fundingMethod: String? = null,
    val amount: String? = null,
    val countryCode: String? = null,
    val claimableBalanceSupported: Boolean? = null,
    val customerId: String? = null,
    val locationId: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletName: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletUrl: String? = null,
    val lang: String? = null,
    val onChangeCallback: String? = null,
    val extraFields: Map<String, String>? = null
)

/**
 * Request to initiate a programmatic deposit with asset conversion (SEP-38 exchange).
 *
 * A deposit exchange allows a user to send an off-chain asset to an anchor and receive a
 * different Stellar asset in return. For example, depositing EUR via bank transfer and
 * receiving USDC on Stellar. This requires coordination with SEP-38 for obtaining quotes.
 *
 * @property destinationAsset Code of the on-chain asset the user wants to receive from the
 *     anchor. Must match a code in the /info response's deposit-exchange object.
 * @property sourceAsset Off-chain asset the user will send to the anchor, in SEP-38 Asset
 *     Identification Format (e.g., "iso4217:EUR"). Must match a value from SEP-38 /prices.
 * @property amount Amount of the source asset the user will deposit. Should equal
 *     quote.sell_amount if a quote_id is provided.
 * @property account Stellar account (G...), muxed account (M...), or contract (C...) that will
 *     receive the deposited funds.
 * @property jwt SEP-10 JWT token for authentication. Required for all deposit requests.
 * @property quoteId SEP-38 quote ID for firm pricing. When provided, the anchor must honor the
 *     quoted conversion rate if the deposit is completed before the quote expires.
 * @property memoType Type of memo the anchor should attach to the Stellar payment transaction:
 *     "text", "id", or "hash".
 * @property memo Value of memo to attach to the transaction. For hash type, this should be
 *     base64-encoded.
 * @property emailAddress Email address of the depositor for status updates.
 * @property type Deprecated: Use [fundingMethod] instead. Type of deposit method.
 * @property fundingMethod Method of funding the deposit (replaces deprecated [type] field).
 * @property countryCode ISO 3166-1 alpha-3 code of the user's current address.
 * @property claimableBalanceSupported Set to true if the client supports receiving funds via
 *     claimable balances.
 * @property customerId SEP-12 customer ID if already known to the anchor.
 * @property locationId ID of the chosen location for cash drop-off.
 * @property walletName Deprecated: Use client_domain in SEP-10 JWT instead.
 * @property walletUrl Deprecated: Use client_domain in SEP-10 JWT instead.
 * @property lang Language code (RFC 4646) for error messages.
 * @property onChangeCallback URL for transaction status update callbacks.
 * @property extraFields Additional fields required by the anchor.
 */
public data class Sep06DepositExchangeRequest(
    val destinationAsset: String,
    val sourceAsset: String,
    val amount: String,
    val account: String,
    val jwt: String,
    val quoteId: String? = null,
    val memoType: String? = null,
    val memo: String? = null,
    val emailAddress: String? = null,
    @Deprecated("Use fundingMethod instead")
    val type: String? = null,
    val fundingMethod: String? = null,
    val countryCode: String? = null,
    val claimableBalanceSupported: Boolean? = null,
    val customerId: String? = null,
    val locationId: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletName: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletUrl: String? = null,
    val lang: String? = null,
    val onChangeCallback: String? = null,
    val extraFields: Map<String, String>? = null
)

/**
 * Request to initiate a programmatic withdrawal from a Stellar anchor.
 *
 * A withdrawal occurs when a user sends a Stellar asset to an anchor's account, and the
 * anchor delivers the equivalent amount in an off-chain asset (fiat to bank account, crypto
 * to external blockchain, cash pickup, etc.).
 *
 * Unlike SEP-24's interactive flow, SEP-6 withdrawals are fully programmatic - all required
 * information must be provided in the API request.
 *
 * @property assetCode Code of the on-chain asset the user wants to withdraw. Must match a code
 *     in the /info response's withdraw object.
 * @property type Type of withdrawal method: "crypto", "bank_account", "cash", "mobile",
 *     "bill_payment", or other custom values. Deprecated but still required for compatibility.
 *     Use [fundingMethod] for new implementations.
 * @property jwt SEP-10 JWT token for authentication. Required for all withdrawal requests.
 * @property fundingMethod Method of delivering the withdrawal (replaces deprecated [type]).
 * @property dest Destination account for the withdrawal (crypto address, bank account number,
 *     IBAN, mobile number, or email address).
 * @property destExtra Additional destination information such as routing number, BIC, memo,
 *     or partner name handling the withdrawal.
 * @property account Stellar account (G...) or muxed account (M...) that will send the
 *     withdrawal. May differ from the account authenticated via SEP-10.
 * @property memo Memo value for the Stellar transaction. Only needed if SEP-10 authentication
 *     is not used.
 * @property memoType Deprecated: Type of memo ("text", "id", or "hash"). Memos for user
 *     identification should use type "id".
 * @property amount Amount the user intends to withdraw. May be necessary for KYC determination.
 * @property countryCode ISO 3166-1 alpha-3 code of the user's current address.
 * @property refundMemo Memo the anchor must use when sending refund payments. If not specified,
 *     the anchor uses the same memo from the original payment.
 * @property refundMemoType Type of the refund memo: "id", "text", or "hash". Required if
 *     [refundMemo] is specified.
 * @property customerId SEP-12 customer ID if already known to the anchor.
 * @property locationId ID of the chosen location for cash pickup.
 * @property walletName Deprecated: Use client_domain in SEP-10 JWT instead.
 * @property walletUrl Deprecated: Use client_domain in SEP-10 JWT instead.
 * @property lang Language code (RFC 4646) for error messages.
 * @property onChangeCallback URL for transaction status update callbacks.
 * @property extraFields Additional fields required by the anchor.
 */
public data class Sep06WithdrawRequest(
    val assetCode: String,
    @Deprecated("Use fundingMethod for new implementations")
    val type: String,
    val jwt: String,
    val fundingMethod: String? = null,
    val dest: String? = null,
    val destExtra: String? = null,
    val account: String? = null,
    val memo: String? = null,
    @Deprecated("Memos for user identification should use type 'id'")
    val memoType: String? = null,
    val amount: String? = null,
    val countryCode: String? = null,
    val refundMemo: String? = null,
    val refundMemoType: String? = null,
    val customerId: String? = null,
    val locationId: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletName: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletUrl: String? = null,
    val lang: String? = null,
    val onChangeCallback: String? = null,
    val extraFields: Map<String, String>? = null
)

/**
 * Request to initiate a programmatic withdrawal with asset conversion (SEP-38 exchange).
 *
 * A withdrawal exchange allows a user to send a Stellar asset to an anchor and receive a
 * different off-chain asset in return. For example, sending USDC on Stellar and receiving
 * EUR in a bank account. This requires coordination with SEP-38 for obtaining quotes.
 *
 * @property sourceAsset Code of the on-chain asset the user wants to withdraw. Must match a
 *     code in the /info response's withdraw-exchange object.
 * @property destinationAsset Off-chain asset the user will receive, in SEP-38 Asset
 *     Identification Format (e.g., "iso4217:EUR"). Must match a value from SEP-38 /prices.
 * @property amount Amount of the source asset the user will send to the anchor. Should equal
 *     quote.sell_amount if a quote_id is provided.
 * @property type Type of withdrawal method. Deprecated but still required for compatibility.
 * @property jwt SEP-10 JWT token for authentication. Required for all withdrawal requests.
 * @property fundingMethod Method of delivering the withdrawal (replaces deprecated [type]).
 * @property quoteId SEP-38 quote ID for firm pricing. When provided, the anchor must honor the
 *     quoted conversion rate if the Stellar transaction is created before the quote expires.
 * @property dest Destination account for the withdrawal.
 * @property destExtra Additional destination information.
 * @property account Stellar account that will send the withdrawal.
 * @property memo Memo value for the Stellar transaction.
 * @property memoType Deprecated: Type of memo.
 * @property countryCode ISO 3166-1 alpha-3 code of the user's current address.
 * @property refundMemo Memo for refund payments.
 * @property refundMemoType Type of the refund memo.
 * @property customerId SEP-12 customer ID if already known to the anchor.
 * @property locationId ID of the chosen location for cash pickup.
 * @property walletName Deprecated: Use client_domain in SEP-10 JWT instead.
 * @property walletUrl Deprecated: Use client_domain in SEP-10 JWT instead.
 * @property lang Language code (RFC 4646) for error messages.
 * @property onChangeCallback URL for transaction status update callbacks.
 * @property extraFields Additional fields required by the anchor.
 */
public data class Sep06WithdrawExchangeRequest(
    val sourceAsset: String,
    val destinationAsset: String,
    val amount: String,
    @Deprecated("Use fundingMethod for new implementations")
    val type: String,
    val jwt: String,
    val fundingMethod: String? = null,
    val quoteId: String? = null,
    val dest: String? = null,
    val destExtra: String? = null,
    val account: String? = null,
    val memo: String? = null,
    @Deprecated("Memos for user identification should use type 'id'")
    val memoType: String? = null,
    val countryCode: String? = null,
    val refundMemo: String? = null,
    val refundMemoType: String? = null,
    val customerId: String? = null,
    val locationId: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletName: String? = null,
    @Deprecated("Use client_domain in SEP-10 JWT instead")
    val walletUrl: String? = null,
    val lang: String? = null,
    val onChangeCallback: String? = null,
    val extraFields: Map<String, String>? = null
)

/**
 * Request to query transaction history for an account with a Stellar anchor.
 *
 * Returns a list of transactions for the specified asset, with optional filtering by date,
 * transaction kind, and pagination parameters. Only returns transactions that are deposits
 * to or withdrawals from the anchor.
 *
 * @property assetCode Code of the asset for which to retrieve transaction history.
 * @property account Stellar account (G...) or muxed account (M...) for which to retrieve
 *     transactions.
 * @property jwt SEP-10 JWT token for authentication. Required to identify the account.
 * @property noOlderThan ISO 8601 UTC datetime string. Only transactions started on or after
 *     this date will be returned (e.g., "2023-01-15T12:00:00Z").
 * @property limit Maximum number of transactions to return. The anchor may impose its own
 *     maximum limit.
 * @property kind Filter by transaction kind. Can be a single value ("deposit", "withdrawal")
 *     or a comma-separated list ("deposit,withdrawal,deposit-exchange,withdrawal-exchange").
 * @property pagingId Pagination cursor. Use the id of the last transaction from a previous
 *     response to fetch the next page.
 * @property lang Language code (RFC 4646) for any returned messages.
 */
public data class Sep06TransactionsRequest(
    val assetCode: String,
    val account: String,
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
 * @property id Anchor's unique identifier for the transaction. This is the id returned in
 *     the deposit or withdraw response.
 * @property stellarTransactionId Stellar transaction hash for the on-chain payment. Useful
 *     when querying the status of a completed transaction.
 * @property externalTransactionId External (off-chain) transaction identifier. This is the
 *     identifier from the external payment system (e.g., bank reference number).
 * @property lang Language code (RFC 4646) for any returned messages.
 * @property jwt SEP-10 JWT token for authentication. Optional as some anchors allow
 *     transaction queries without authentication for public transactions.
 */
public data class Sep06TransactionRequest(
    val id: String? = null,
    val stellarTransactionId: String? = null,
    val externalTransactionId: String? = null,
    val lang: String? = null,
    val jwt: String? = null
)

/**
 * Request to query the fee for a deposit or withdrawal operation.
 *
 * Note: The /fee endpoint is deprecated in SEP-6. Anchors should use SEP-38 quotes for fee
 * calculation instead. This request class is provided for compatibility with anchors that
 * still support the legacy fee endpoint.
 *
 * @property operation Type of operation: "deposit" or "withdraw".
 * @property assetCode Code of the asset for the operation.
 * @property amount Amount for which to calculate the fee.
 * @property type Type of deposit or withdrawal method. The available types can be found in
 *     the /info response for each asset.
 * @property jwt SEP-10 JWT token for authentication. Optional as some anchors allow fee
 *     queries without authentication.
 */
public data class Sep06FeeRequest(
    val operation: String,
    val assetCode: String,
    val amount: String,
    val type: String? = null,
    val jwt: String? = null
)

/**
 * Request to update a transaction with additional information requested by the anchor.
 *
 * This endpoint allows clients to provide additional information that the anchor has
 * requested after a transaction was initiated. This is typically used when the anchor needs
 * extra details such as destination information, KYC fields, or other transaction-specific
 * requirements.
 *
 * The transaction must be in the "pending_transaction_info_update" status for this request
 * to succeed. The required fields can be found in the transaction's requiredInfoUpdates
 * property.
 *
 * @property id Anchor's unique identifier for the transaction to update.
 * @property fields Map of field names to values being updated. Keys should match the field
 *     names from the transaction's requiredInfoUpdates property.
 * @property jwt SEP-10 JWT token for authentication. Required to verify ownership of the
 *     transaction.
 */
public data class Sep06PatchTransactionRequest(
    val id: String,
    val fields: Map<String, String>,
    val jwt: String
)
