// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep09

/**
 * Financial account information for KYC verification.
 *
 * Contains bank account, mobile money, and cryptocurrency account details
 * for receiving or sending payments. These fields can be used directly for
 * natural persons or nested within organization fields using a prefix.
 *
 * Supports multiple account types:
 * - Traditional bank accounts (with routing numbers, branch codes)
 * - Regional banking systems (CLABE for Mexico, CBU for Argentina)
 * - Mobile money accounts
 * - Cryptocurrency addresses
 *
 * Example - Bank account:
 * ```kotlin
 * val account = FinancialAccountKYCFields(
 *     bankName = "Example Bank",
 *     bankAccountNumber = "1234567890",
 *     bankNumber = "123456789", // Routing number
 *     bankBranchNumber = "001",
 *     bankAccountType = "checking"
 * )
 *
 * // Extract fields for submission
 * val fields = account.fields()
 * ```
 *
 * Example - Cryptocurrency:
 * ```kotlin
 * val account = FinancialAccountKYCFields(
 *     cryptoAddress = "GDJK...",
 *     externalTransferMemo = "12345"
 * )
 * ```
 *
 * Example - With organization prefix:
 * ```kotlin
 * val orgAccount = FinancialAccountKYCFields(
 *     bankName = "Corporate Bank",
 *     bankAccountNumber = "9876543210"
 * )
 *
 * // When used in organization context, fields will have "organization." prefix
 * val fields = orgAccount.fields(keyPrefix = "organization.")
 * // Result: {"organization.bank_name": "Corporate Bank", ...}
 * ```
 *
 * See also:
 * - [NaturalPersonKYCFields] for individual customer fields
 * - [OrganizationKYCFields] for business entity fields
 * - [SEP-0009 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md)
 *
 * @property bankName Name of the bank (useful in regions without unified routing)
 * @property bankAccountType Type of bank account (e.g., checking, savings)
 * @property bankAccountNumber Account number identifying the bank account
 * @property bankNumber Bank routing number (in US) or equivalent identifier
 * @property bankPhoneNumber Phone number with country code for the bank
 * @property bankBranchNumber Branch identifier for the bank
 * @property externalTransferMemo Destination tag/memo for transaction identification
 * @property clabeNumber Bank account number for Mexico (CLABE system)
 * @property cbuNumber CBU (Clave Bancaria Uniforme) or CVU (Clave Virtual Uniforme) for Argentina
 * @property cbuAlias Alias for a CBU or CVU account
 * @property mobileMoneyNumber Mobile phone number in E.164 format for mobile money account
 * @property mobileMoneyProvider Name of the mobile money service provider
 * @property cryptoAddress Cryptocurrency account address
 * @property cryptoMemo Destination tag/memo for crypto transactions (deprecated, use externalTransferMemo)
 */
data class FinancialAccountKYCFields(
    val bankName: String? = null,
    val bankAccountType: String? = null,
    val bankAccountNumber: String? = null,
    val bankNumber: String? = null,
    val bankPhoneNumber: String? = null,
    val bankBranchNumber: String? = null,
    val externalTransferMemo: String? = null,
    val clabeNumber: String? = null,
    val cbuNumber: String? = null,
    val cbuAlias: String? = null,
    val mobileMoneyNumber: String? = null,
    val mobileMoneyProvider: String? = null,
    val cryptoAddress: String? = null,
    @Deprecated("Use externalTransferMemo instead")
    val cryptoMemo: String? = null
) {
    companion object {
        const val BANK_NAME = "bank_name"
        const val BANK_ACCOUNT_TYPE = "bank_account_type"
        const val BANK_ACCOUNT_NUMBER = "bank_account_number"
        const val BANK_NUMBER = "bank_number"
        const val BANK_PHONE_NUMBER = "bank_phone_number"
        const val BANK_BRANCH_NUMBER = "bank_branch_number"
        const val EXTERNAL_TRANSFER_MEMO = "external_transfer_memo"
        const val CLABE_NUMBER = "clabe_number"
        const val CBU_NUMBER = "cbu_number"
        const val CBU_ALIAS = "cbu_alias"
        const val MOBILE_MONEY_NUMBER = "mobile_money_number"
        const val MOBILE_MONEY_PROVIDER = "mobile_money_provider"
        const val CRYPTO_ADDRESS = "crypto_address"
        const val CRYPTO_MEMO = "crypto_memo"
    }

    /**
     * Converts all financial account KYC fields to a map for SEP-9 submission.
     *
     * Only fields with non-null values are included in the result. This method
     * is used when submitting KYC information via SEP-12 or similar protocols.
     *
     * @param keyPrefix Optional prefix for field keys (e.g., "organization." for business accounts)
     * @return Map of field keys to string values for all non-null fields
     *
     * Example:
     * ```kotlin
     * val account = FinancialAccountKYCFields(
     *     bankName = "Example Bank",
     *     bankAccountNumber = "1234567890"
     * )
     *
     * val fields = account.fields()
     * // Result: {"bank_name": "Example Bank", "bank_account_number": "1234567890"}
     *
     * // With organization prefix
     * val orgFields = account.fields(keyPrefix = "organization.")
     * // Result: {"organization.bank_name": "Example Bank", ...}
     * ```
     */
    fun fields(keyPrefix: String = ""): Map<String, String> {
        val result = mutableMapOf<String, String>()

        bankName?.let { result[keyPrefix + BANK_NAME] = it }
        bankAccountType?.let { result[keyPrefix + BANK_ACCOUNT_TYPE] = it }
        bankAccountNumber?.let { result[keyPrefix + BANK_ACCOUNT_NUMBER] = it }
        bankNumber?.let { result[keyPrefix + BANK_NUMBER] = it }
        bankPhoneNumber?.let { result[keyPrefix + BANK_PHONE_NUMBER] = it }
        bankBranchNumber?.let { result[keyPrefix + BANK_BRANCH_NUMBER] = it }
        externalTransferMemo?.let { result[keyPrefix + EXTERNAL_TRANSFER_MEMO] = it }
        clabeNumber?.let { result[keyPrefix + CLABE_NUMBER] = it }
        cbuNumber?.let { result[keyPrefix + CBU_NUMBER] = it }
        cbuAlias?.let { result[keyPrefix + CBU_ALIAS] = it }
        mobileMoneyNumber?.let { result[keyPrefix + MOBILE_MONEY_NUMBER] = it }
        mobileMoneyProvider?.let { result[keyPrefix + MOBILE_MONEY_PROVIDER] = it }
        cryptoAddress?.let { result[keyPrefix + CRYPTO_ADDRESS] = it }
        @Suppress("DEPRECATION")
        cryptoMemo?.let { result[keyPrefix + CRYPTO_MEMO] = it }

        return result
    }
}
