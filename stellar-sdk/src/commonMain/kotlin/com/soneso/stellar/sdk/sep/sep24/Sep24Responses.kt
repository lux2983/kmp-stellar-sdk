// Copyright 2026 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep24

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the /info endpoint containing anchor capabilities and supported assets.
 *
 * Provides information about what assets the anchor supports for deposit and withdrawal,
 * fee configuration, and feature support such as claimable balances.
 *
 * @property depositAssets Map of asset codes to deposit configuration
 * @property withdrawAssets Map of asset codes to withdrawal configuration
 * @property feeEndpoint Configuration for the optional /fee endpoint
 * @property features Feature flags indicating anchor capabilities
 */
@Serializable
data class Sep24InfoResponse(
    @SerialName("deposit")
    val depositAssets: Map<String, Sep24AssetInfo>? = null,

    @SerialName("withdraw")
    val withdrawAssets: Map<String, Sep24AssetInfo>? = null,

    @SerialName("fee")
    val feeEndpoint: Sep24FeeEndpointInfo? = null,

    @SerialName("features")
    val features: Sep24Features? = null
)

/**
 * Asset configuration for deposit or withdrawal operations.
 *
 * Describes the limits and fee structure for a specific asset.
 *
 * @property enabled Whether this asset is currently enabled for the operation
 * @property minAmount Minimum amount that can be deposited/withdrawn
 * @property maxAmount Maximum amount that can be deposited/withdrawn
 * @property feeFixed Fixed fee charged for the operation
 * @property feePercent Percentage fee charged for the operation
 * @property feeMinimum Minimum fee charged regardless of amount
 */
@Serializable
data class Sep24AssetInfo(
    @SerialName("enabled")
    val enabled: Boolean,

    @SerialName("min_amount")
    val minAmount: String? = null,

    @SerialName("max_amount")
    val maxAmount: String? = null,

    @SerialName("fee_fixed")
    val feeFixed: String? = null,

    @SerialName("fee_percent")
    val feePercent: String? = null,

    @SerialName("fee_minimum")
    val feeMinimum: String? = null
)

/**
 * Configuration for the /fee endpoint.
 *
 * Note: The /fee endpoint is deprecated in favor of SEP-38 quotes.
 *
 * @property enabled Whether the /fee endpoint is available
 * @property authenticationRequired Whether authentication is required to access the /fee endpoint
 */
@Serializable
data class Sep24FeeEndpointInfo(
    @SerialName("enabled")
    val enabled: Boolean,

    @SerialName("authentication_required")
    val authenticationRequired: Boolean = false
)

/**
 * Feature flags indicating anchor capabilities.
 *
 * @property accountCreation Whether the anchor can create Stellar accounts for users
 * @property claimableBalances Whether the anchor supports sending deposits as claimable balances
 */
@Serializable
data class Sep24Features(
    @SerialName("account_creation")
    val accountCreation: Boolean = true,

    @SerialName("claimable_balances")
    val claimableBalances: Boolean = false
)

/**
 * Response from initiating an interactive deposit or withdrawal flow.
 *
 * Contains the URL to display in a webview for the user to complete
 * the interactive flow with the anchor.
 *
 * @property type Always "interactive_customer_info_needed"
 * @property url URL to display in a webview for the user
 * @property id Unique identifier for this transaction
 */
@Serializable
data class Sep24InteractiveResponse(
    @SerialName("type")
    val type: String,

    @SerialName("url")
    val url: String,

    @SerialName("id")
    val id: String
)

/**
 * Response from the /fee endpoint.
 *
 * Note: This endpoint is deprecated in favor of SEP-38 quotes.
 *
 * @property fee The fee amount as a string
 */
@Serializable
data class Sep24FeeResponse(
    @SerialName("fee")
    val fee: String? = null
)

/**
 * Response containing a single transaction.
 *
 * @property transaction The transaction details
 */
@Serializable
data class Sep24TransactionResponse(
    @SerialName("transaction")
    val transaction: Sep24Transaction
)

/**
 * Response containing a list of transactions.
 *
 * @property transactions List of transaction details
 */
@Serializable
data class Sep24TransactionsResponse(
    @SerialName("transactions")
    val transactions: List<Sep24Transaction>
)

/**
 * Represents a SEP-24 deposit or withdrawal transaction.
 *
 * Contains comprehensive information about the state and details of
 * an interactive transaction, including amounts, fees, status, and
 * platform-specific fields for deposits and withdrawals.
 *
 * @property id Unique identifier for this transaction
 * @property kind Type of transaction: "deposit" or "withdrawal"
 * @property status Current status of the transaction
 * @property statusEta Estimated time in seconds until status changes
 * @property kycVerified Whether KYC information has been verified
 * @property moreInfoUrl URL for additional transaction information
 * @property amountIn Amount received by the anchor
 * @property amountInAsset Asset of amountIn using Asset Identification Format
 * @property amountOut Amount sent by the anchor to the user
 * @property amountOutAsset Asset of amountOut using Asset Identification Format
 * @property amountFee Deprecated: Use feeDetails instead
 * @property amountFeeAsset Deprecated: Use feeDetails instead
 * @property feeDetails Detailed fee breakdown
 * @property quoteId SEP-38 quote ID if applicable
 * @property startedAt ISO 8601 UTC timestamp when transaction started
 * @property completedAt ISO 8601 UTC timestamp when transaction completed
 * @property updatedAt ISO 8601 UTC timestamp when transaction was last updated
 * @property userActionRequiredBy ISO 8601 UTC timestamp by which user action is required
 * @property stellarTransactionId Stellar transaction hash
 * @property externalTransactionId External payment system transaction ID
 * @property message Human-readable message about the transaction
 * @property refunded Deprecated: Use refunds object instead
 * @property refunds Refund information if applicable
 * @property from Sending address (Stellar for withdrawals, external for deposits)
 * @property to Receiving address (Stellar for deposits, external for withdrawals)
 * @property depositMemo Memo to use for the deposit Stellar payment
 * @property depositMemoType Type of memo: "text", "id", or "hash"
 * @property claimableBalanceId Claimable balance ID if deposit was sent as claimable balance
 * @property withdrawAnchorAccount Stellar account to send withdrawal payment to
 * @property withdrawMemo Memo to use for the withdrawal Stellar payment
 * @property withdrawMemoType Type of memo for withdrawal: "text", "id", or "hash"
 */
@Serializable
data class Sep24Transaction(
    @SerialName("id")
    val id: String,

    @SerialName("kind")
    val kind: String,

    @SerialName("status")
    val status: String,

    @SerialName("status_eta")
    val statusEta: Int? = null,

    @SerialName("kyc_verified")
    val kycVerified: Boolean? = null,

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
    val feeDetails: Sep24FeeDetails? = null,

    @SerialName("quote_id")
    val quoteId: String? = null,

    @SerialName("started_at")
    val startedAt: String? = null,

    @SerialName("completed_at")
    val completedAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

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
    val refunds: Sep24Refunds? = null,

    @SerialName("from")
    val from: String? = null,

    @SerialName("to")
    val to: String? = null,

    // Deposit-specific fields
    @SerialName("deposit_memo")
    val depositMemo: String? = null,

    @SerialName("deposit_memo_type")
    val depositMemoType: String? = null,

    @SerialName("claimable_balance_id")
    val claimableBalanceId: String? = null,

    // Withdraw-specific fields
    @SerialName("withdraw_anchor_account")
    val withdrawAnchorAccount: String? = null,

    @SerialName("withdraw_memo")
    val withdrawMemo: String? = null,

    @SerialName("withdraw_memo_type")
    val withdrawMemoType: String? = null
) {
    /**
     * Get the status as a [Sep24TransactionStatus] enum value.
     *
     * @return The status enum, or null if the status string is not recognized
     */
    fun getStatusEnum(): Sep24TransactionStatus? =
        Sep24TransactionStatus.fromValue(status)

    /**
     * Check if the transaction is in a terminal state.
     *
     * Terminal states are: completed, refunded, expired, error, no_market, too_small, too_large.
     *
     * @return true if the transaction is in a terminal state
     */
    fun isTerminal(): Boolean = Sep24TransactionStatus.isTerminal(status)
}

/**
 * Detailed fee breakdown for a transaction.
 *
 * @property total Total fee amount
 * @property asset Asset of the fee using Asset Identification Format
 * @property breakdown Optional list of individual fee components
 */
@Serializable
data class Sep24FeeDetails(
    @SerialName("total")
    val total: String,

    @SerialName("asset")
    val asset: String,

    @SerialName("breakdown")
    val breakdown: List<Sep24FeeDetail>? = null
)

/**
 * Individual fee component in a fee breakdown.
 *
 * @property name Name of the fee component (e.g., "Service fee", "Network fee")
 * @property amount Amount of this fee component
 * @property description Optional human-readable description
 */
@Serializable
data class Sep24FeeDetail(
    @SerialName("name")
    val name: String,

    @SerialName("amount")
    val amount: String,

    @SerialName("description")
    val description: String? = null
)

/**
 * Refund information for a transaction.
 *
 * @property amountRefunded Total amount refunded to the user
 * @property amountFee Total fee deducted from the refund
 * @property payments List of individual refund payments
 */
@Serializable
data class Sep24Refunds(
    @SerialName("amount_refunded")
    val amountRefunded: String,

    @SerialName("amount_fee")
    val amountFee: String,

    @SerialName("payments")
    val payments: List<Sep24RefundPayment>
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
data class Sep24RefundPayment(
    @SerialName("id")
    val id: String,

    @SerialName("id_type")
    val idType: String,

    @SerialName("amount")
    val amount: String,

    @SerialName("fee")
    val fee: String
)
