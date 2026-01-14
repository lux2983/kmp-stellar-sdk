// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep06

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the /info endpoint containing anchor capabilities and supported assets.
 *
 * Provides information about what assets the anchor supports for deposit and withdrawal,
 * including standard operations and SEP-38 exchange operations, along with fee configuration
 * and feature support such as claimable balances.
 *
 * @property deposit Map of asset codes to deposit configuration
 * @property depositExchange Map of asset codes to deposit-exchange configuration (SEP-38)
 * @property withdraw Map of asset codes to withdrawal configuration
 * @property withdrawExchange Map of asset codes to withdrawal-exchange configuration (SEP-38)
 * @property fee Configuration for the optional /fee endpoint
 * @property transaction Configuration for the /transaction endpoint
 * @property transactions Configuration for the /transactions endpoint
 * @property features Feature flags indicating anchor capabilities
 */
@Serializable
data class Sep06InfoResponse(
    @SerialName("deposit")
    val deposit: Map<String, Sep06DepositAsset>? = null,

    @SerialName("deposit-exchange")
    val depositExchange: Map<String, Sep06DepositExchangeAsset>? = null,

    @SerialName("withdraw")
    val withdraw: Map<String, Sep06WithdrawAsset>? = null,

    @SerialName("withdraw-exchange")
    val withdrawExchange: Map<String, Sep06WithdrawExchangeAsset>? = null,

    @SerialName("fee")
    val fee: Sep06FeeEndpointInfo? = null,

    @SerialName("transaction")
    val transaction: Sep06TransactionEndpointInfo? = null,

    @SerialName("transactions")
    val transactions: Sep06TransactionsEndpointInfo? = null,

    @SerialName("features")
    val features: Sep06FeatureFlags? = null
)

/**
 * Configuration for a deposit asset supported by the anchor.
 *
 * Contains all the details about how deposits work for a specific asset,
 * including whether it's enabled, authentication requirements, fee structure,
 * transaction limits, and any additional fields required.
 *
 * @property enabled Whether SEP-6 deposit for this asset is supported
 * @property authenticationRequired Whether client must be authenticated before accessing the deposit endpoint
 * @property minAmount Minimum amount that can be deposited
 * @property maxAmount Maximum amount that can be deposited
 * @property feeFixed Fixed fee charged for the operation
 * @property feePercent Percentage fee charged for the operation
 * @property fields Custom fields required for the deposit transaction (deprecated, use SEP-12)
 */
@Serializable
data class Sep06DepositAsset(
    @SerialName("enabled")
    val enabled: Boolean,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean? = null,

    @SerialName("min_amount")
    val minAmount: String? = null,

    @SerialName("max_amount")
    val maxAmount: String? = null,

    @SerialName("fee_fixed")
    val feeFixed: String? = null,

    @SerialName("fee_percent")
    val feePercent: String? = null,

    @Deprecated("Use SEP-12 for KYC information")
    @SerialName("fields")
    val fields: Map<String, Sep06Field>? = null
)

/**
 * Configuration for a deposit-exchange asset supported by the anchor.
 *
 * Used when the anchor supports deposit operations that include asset exchange
 * via SEP-38 quotes as part of the transaction flow.
 *
 * @property enabled Whether SEP-6 deposit-exchange for this asset is supported
 * @property authenticationRequired Whether client must be authenticated before accessing the endpoint
 * @property fields Custom fields required for the deposit-exchange transaction (deprecated, use SEP-12)
 */
@Serializable
data class Sep06DepositExchangeAsset(
    @SerialName("enabled")
    val enabled: Boolean,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean? = null,

    @Deprecated("Use SEP-12 for KYC information")
    @SerialName("fields")
    val fields: Map<String, Sep06Field>? = null
)

/**
 * Configuration for a withdrawal asset supported by the anchor.
 *
 * Contains all the details about how withdrawals work for a specific asset,
 * including whether it's enabled, authentication requirements, fee structure,
 * transaction limits, and supported withdrawal types with their required fields.
 *
 * @property enabled Whether SEP-6 withdrawal for this asset is supported
 * @property authenticationRequired Whether client must be authenticated before accessing the withdraw endpoint
 * @property minAmount Minimum amount that can be withdrawn
 * @property maxAmount Maximum amount that can be withdrawn
 * @property feeFixed Fixed fee charged for the operation
 * @property feePercent Percentage fee charged for the operation
 * @property types Map of supported withdrawal types (e.g., bank_account, crypto) with their required fields
 */
@Serializable
data class Sep06WithdrawAsset(
    @SerialName("enabled")
    val enabled: Boolean,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean? = null,

    @SerialName("min_amount")
    val minAmount: String? = null,

    @SerialName("max_amount")
    val maxAmount: String? = null,

    @SerialName("fee_fixed")
    val feeFixed: String? = null,

    @SerialName("fee_percent")
    val feePercent: String? = null,

    @SerialName("types")
    val types: Map<String, Sep06WithdrawType>? = null
)

/**
 * Configuration for a withdrawal type (e.g., bank_account, crypto, cash).
 *
 * Specifies the fields required for a specific withdrawal method.
 *
 * @property fields Map of field names to field definitions required for this withdrawal type
 */
@Serializable
data class Sep06WithdrawType(
    @SerialName("fields")
    val fields: Map<String, Sep06Field>? = null
)

/**
 * Configuration for a withdrawal-exchange asset supported by the anchor.
 *
 * Used when the anchor supports withdrawal operations that include asset exchange
 * via SEP-38 quotes as part of the transaction flow.
 *
 * @property enabled Whether SEP-6 withdrawal-exchange for this asset is supported
 * @property authenticationRequired Whether client must be authenticated before accessing the endpoint
 * @property types Map of supported withdrawal types with their required fields
 */
@Serializable
data class Sep06WithdrawExchangeAsset(
    @SerialName("enabled")
    val enabled: Boolean,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean? = null,

    @SerialName("types")
    val types: Map<String, Sep06WithdrawType>? = null
)

/**
 * Describes a field that needs to be provided for a transaction.
 *
 * Anchors use this to specify additional fields required for deposits or
 * withdrawals beyond the standard parameters.
 *
 * @property description Description of field to show to user
 * @property optional Whether the field is optional (defaults to false)
 * @property choices List of possible values for the field
 */
@Serializable
data class Sep06Field(
    @SerialName("description")
    val description: String? = null,

    @SerialName("optional")
    val optional: Boolean? = null,

    @SerialName("choices")
    val choices: List<String>? = null
)

/**
 * Configuration for the /fee endpoint.
 *
 * @property enabled Whether the /fee endpoint is available
 * @property authenticationRequired Whether authentication is required to access the /fee endpoint
 * @property description Explanation of how fees are calculated
 */
@Serializable
data class Sep06FeeEndpointInfo(
    @SerialName("enabled")
    val enabled: Boolean? = null,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean? = null,

    @SerialName("description")
    val description: String? = null
)

/**
 * Configuration for the /transaction endpoint.
 *
 * @property enabled Whether the /transaction endpoint is available
 * @property authenticationRequired Whether authentication is required to access the endpoint
 */
@Serializable
data class Sep06TransactionEndpointInfo(
    @SerialName("enabled")
    val enabled: Boolean? = null,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean? = null
)

/**
 * Configuration for the /transactions endpoint.
 *
 * @property enabled Whether the /transactions endpoint is available
 * @property authenticationRequired Whether authentication is required to access the endpoint
 */
@Serializable
data class Sep06TransactionsEndpointInfo(
    @SerialName("enabled")
    val enabled: Boolean? = null,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean? = null
)

/**
 * Feature flags indicating anchor capabilities.
 *
 * @property accountCreation Whether the anchor can create Stellar accounts for users
 * @property claimableBalances Whether the anchor supports sending deposits as claimable balances
 */
@Serializable
data class Sep06FeatureFlags(
    @SerialName("account_creation")
    val accountCreation: Boolean = true,

    @SerialName("claimable_balances")
    val claimableBalances: Boolean = false
)

/**
 * Response from initiating a deposit operation.
 *
 * Contains the instructions for completing the deposit, including how to send
 * funds to the anchor, transaction ID for tracking, estimated completion time,
 * and fee information.
 *
 * @property how Deprecated: Use instructions instead. Terse instructions for how to deposit
 * @property id The anchor's ID for this deposit transaction
 * @property eta Estimated time in seconds until the deposit is credited
 * @property minAmount Minimum amount that can be deposited
 * @property maxAmount Maximum amount that can be deposited
 * @property feeFixed Fixed fee charged for the operation
 * @property feePercent Percentage fee charged for the operation
 * @property extraInfo Additional information about the deposit process
 * @property instructions Map of SEP-9 financial account fields to deposit instructions
 */
@Serializable
data class Sep06DepositResponse(
    @Deprecated("Use instructions instead")
    @SerialName("how")
    val how: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("eta")
    val eta: Long? = null,

    @SerialName("min_amount")
    val minAmount: String? = null,

    @SerialName("max_amount")
    val maxAmount: String? = null,

    @SerialName("fee_fixed")
    val feeFixed: String? = null,

    @SerialName("fee_percent")
    val feePercent: String? = null,

    @SerialName("extra_info")
    val extraInfo: Sep06ExtraInfo? = null,

    @SerialName("instructions")
    val instructions: Map<String, Sep06DepositInstruction>? = null
)

/**
 * Instructions for completing an off-chain deposit.
 *
 * Provides specific details about how to complete a deposit, typically
 * containing account numbers, routing codes, or other payment identifiers.
 *
 * @property value The value of the field
 * @property description Human-readable description of the field
 */
@Serializable
data class Sep06DepositInstruction(
    @SerialName("value")
    val value: String,

    @SerialName("description")
    val description: String
)

/**
 * Additional information from the anchor.
 *
 * Contains optional messages or additional details that an anchor wants to
 * communicate to the user about their transaction.
 *
 * @property message Message from the anchor about the transaction
 */
@Serializable
data class Sep06ExtraInfo(
    @SerialName("message")
    val message: String? = null
)

/**
 * Response from initiating a withdrawal operation.
 *
 * Contains the details for completing the withdrawal, including the Stellar
 * account to send funds to, memo information, transaction ID, and fee information.
 *
 * @property accountId The Stellar account the user should send their token to
 * @property memoType Type of memo to attach to transaction (text, id, or hash)
 * @property memo Value of memo to attach to transaction
 * @property id The anchor's ID for this withdrawal transaction
 * @property eta Estimated time in seconds until the withdrawal is processed
 * @property minAmount Minimum amount that can be withdrawn
 * @property maxAmount Maximum amount that can be withdrawn
 * @property feeFixed Fixed fee charged for the operation
 * @property feePercent Percentage fee charged for the operation
 * @property extraInfo Additional information about the withdrawal process
 */
@Serializable
data class Sep06WithdrawResponse(
    @SerialName("account_id")
    val accountId: String? = null,

    @SerialName("memo_type")
    val memoType: String? = null,

    @SerialName("memo")
    val memo: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("eta")
    val eta: Long? = null,

    @SerialName("min_amount")
    val minAmount: String? = null,

    @SerialName("max_amount")
    val maxAmount: String? = null,

    @SerialName("fee_fixed")
    val feeFixed: String? = null,

    @SerialName("fee_percent")
    val feePercent: String? = null,

    @SerialName("extra_info")
    val extraInfo: Sep06ExtraInfo? = null
)

/**
 * Response containing a list of transactions.
 *
 * @property transactions List of transaction details
 */
@Serializable
data class Sep06TransactionsResponse(
    @SerialName("transactions")
    val transactions: List<Sep06Transaction>
)

/**
 * Response containing a single transaction.
 *
 * @property transaction The transaction details
 */
@Serializable
data class Sep06TransactionResponse(
    @SerialName("transaction")
    val transaction: Sep06Transaction
)

/**
 * Response from the /fee endpoint.
 *
 * @property fee The fee amount as a string
 */
@Serializable
data class Sep06FeeResponse(
    @SerialName("fee")
    val fee: String
)

/**
 * Represents a SEP-6 deposit or withdrawal transaction.
 *
 * Contains information about the state and details of a transaction, including
 * amounts, fees, status, and platform-specific fields for deposits and withdrawals.
 *
 * @property id Unique identifier for this transaction
 * @property kind Type of transaction: deposit, deposit-exchange, withdrawal, or withdrawal-exchange
 * @property status Current status of the transaction
 * @property statusEta Estimated time in seconds until status changes
 * @property moreInfoUrl URL for additional transaction information
 * @property amountIn Amount received by the anchor
 * @property amountInAsset Asset of amountIn using Asset Identification Format
 * @property amountOut Amount sent by the anchor to the user
 * @property amountOutAsset Asset of amountOut using Asset Identification Format
 * @property amountFee Deprecated: Use feeDetails instead
 * @property amountFeeAsset Deprecated: Use feeDetails instead
 * @property feeDetails Detailed fee breakdown
 * @property quoteId SEP-38 quote ID if applicable
 * @property from Sending address (Stellar for withdrawals, external for deposits)
 * @property to Receiving address (Stellar for deposits, external for withdrawals)
 * @property externalExtra Extra information for the external account (routing number, BIC)
 * @property externalExtraText Human-readable external info (bank name)
 * @property depositMemo Memo for the deposit Stellar payment
 * @property depositMemoType Type of memo: text, id, or hash
 * @property withdrawAnchorAccount Anchor's Stellar account to send withdrawal payment to
 * @property withdrawMemo Memo for the withdrawal Stellar payment
 * @property withdrawMemoType Type of memo for withdrawal: text, id, or hash
 * @property startedAt ISO 8601 UTC timestamp when transaction started
 * @property updatedAt ISO 8601 UTC timestamp when transaction was last updated
 * @property completedAt ISO 8601 UTC timestamp when transaction completed
 * @property userActionRequiredBy ISO 8601 UTC timestamp by which user action is required
 * @property stellarTransactionId Stellar transaction hash
 * @property externalTransactionId External payment system transaction ID
 * @property message Human-readable message about the transaction
 * @property refunded Deprecated: Use refunds object instead
 * @property refunds Refund information if applicable
 * @property requiredInfoMessage Message about required information
 * @property requiredInfoUpdates Fields needed for information update
 * @property instructions Deposit instructions (SEP-9 financial account fields)
 * @property claimableBalanceId Claimable balance ID if deposit was sent as claimable balance
 */
@Serializable
data class Sep06Transaction(
    @SerialName("id")
    val id: String,

    @SerialName("kind")
    val kind: String,

    @SerialName("status")
    val status: String,

    @SerialName("status_eta")
    val statusEta: Long? = null,

    @SerialName("more_info_url")
    val moreInfoUrl: String? = null,

    @SerialName("amount_in")
    val amountIn: String? = null,

    @SerialName("amount_in_asset")
    val amountInAsset: String? = null,

    @SerialName("amount_out")
    val amountOut: String? = null,

    @SerialName("amount_out_asset")
    val amountOutAsset: String? = null,

    @Deprecated("Use feeDetails instead")
    @SerialName("amount_fee")
    val amountFee: String? = null,

    @Deprecated("Use feeDetails instead")
    @SerialName("amount_fee_asset")
    val amountFeeAsset: String? = null,

    @SerialName("fee_details")
    val feeDetails: Sep06FeeDetails? = null,

    @SerialName("quote_id")
    val quoteId: String? = null,

    @SerialName("from")
    val from: String? = null,

    @SerialName("to")
    val to: String? = null,

    @SerialName("external_extra")
    val externalExtra: String? = null,

    @SerialName("external_extra_text")
    val externalExtraText: String? = null,

    @SerialName("deposit_memo")
    val depositMemo: String? = null,

    @SerialName("deposit_memo_type")
    val depositMemoType: String? = null,

    @SerialName("withdraw_anchor_account")
    val withdrawAnchorAccount: String? = null,

    @SerialName("withdraw_memo")
    val withdrawMemo: String? = null,

    @SerialName("withdraw_memo_type")
    val withdrawMemoType: String? = null,

    @SerialName("started_at")
    val startedAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("completed_at")
    val completedAt: String? = null,

    @SerialName("user_action_required_by")
    val userActionRequiredBy: String? = null,

    @SerialName("stellar_transaction_id")
    val stellarTransactionId: String? = null,

    @SerialName("external_transaction_id")
    val externalTransactionId: String? = null,

    @SerialName("message")
    val message: String? = null,

    @Deprecated("Use refunds object instead")
    @SerialName("refunded")
    val refunded: Boolean? = null,

    @SerialName("refunds")
    val refunds: Sep06Refunds? = null,

    @SerialName("required_info_message")
    val requiredInfoMessage: String? = null,

    @SerialName("required_info_updates")
    val requiredInfoUpdates: Map<String, Sep06Field>? = null,

    @SerialName("instructions")
    val instructions: Map<String, Sep06DepositInstruction>? = null,

    @SerialName("claimable_balance_id")
    val claimableBalanceId: String? = null
) {
    /**
     * Get the status as a [Sep06TransactionStatus] enum value.
     *
     * @return The status enum, or null if the status string is not recognized
     */
    fun getStatusEnum(): Sep06TransactionStatus? =
        Sep06TransactionStatus.fromValue(status)

    /**
     * Get the kind as a [Sep06TransactionKind] enum value.
     *
     * @return The kind enum, or null if the kind string is not recognized
     */
    fun getKindEnum(): Sep06TransactionKind? =
        Sep06TransactionKind.fromValue(kind)

    /**
     * Check if the transaction is in a terminal state.
     *
     * Terminal states are: completed, refunded, expired, error, no_market, too_small, too_large.
     *
     * @return true if the transaction is in a terminal state
     */
    fun isTerminal(): Boolean = Sep06TransactionStatus.isTerminal(status)
}

/**
 * Detailed fee breakdown for a transaction.
 *
 * @property total Total fee amount
 * @property asset Asset of the fee using Asset Identification Format
 * @property details Optional list of individual fee components
 */
@Serializable
data class Sep06FeeDetails(
    @SerialName("total")
    val total: String,

    @SerialName("asset")
    val asset: String,

    @SerialName("details")
    val details: List<Sep06FeeDetail>? = null
)

/**
 * Individual fee component in a fee breakdown.
 *
 * @property name Name of the fee component (e.g., "Service fee", "Network fee")
 * @property description Optional human-readable description
 * @property amount Amount of this fee component
 */
@Serializable
data class Sep06FeeDetail(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("amount")
    val amount: String
)

/**
 * Refund information for a transaction.
 *
 * @property amountRefunded Total amount refunded to the user
 * @property amountFee Total fee deducted from the refund
 * @property payments List of individual refund payments
 */
@Serializable
data class Sep06Refunds(
    @SerialName("amount_refunded")
    val amountRefunded: String,

    @SerialName("amount_fee")
    val amountFee: String,

    @SerialName("payments")
    val payments: List<Sep06RefundPayment>
)

/**
 * Individual refund payment.
 *
 * @property id Transaction ID of the refund payment
 * @property idType Type of ID: "stellar" for Stellar transaction hash, "external" for external payment system ID
 * @property amount Amount of this refund payment
 * @property fee Fee deducted from this refund payment
 */
@Serializable
data class Sep06RefundPayment(
    @SerialName("id")
    val id: String,

    @SerialName("id_type")
    val idType: String,

    @SerialName("amount")
    val amount: String,

    @SerialName("fee")
    val fee: String
)
