// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep09

/**
 * Payment card information for KYC verification.
 *
 * Contains credit or debit card details for payment processing. All field keys
 * are automatically prefixed with "card." when serialized.
 *
 * Supports:
 * - Full card details (number, expiration, CVC)
 * - Tokenized card references (for PCI DSS compliance)
 * - Billing address information
 * - Card network identification
 *
 * Security considerations:
 * - Use tokenized cards when possible to avoid handling sensitive card data
 * - Never log or store full card numbers in application logs
 * - Follow PCI DSS compliance requirements when handling card data
 * - Consider encryption for card data in transit and at rest
 *
 * Example - Full card details:
 * ```kotlin
 * val card = CardKYCFields(
 *     number = "4111111111111111",
 *     expirationDate = "29-11", // YY-MM format (November 2029)
 *     cvc = "123",
 *     holderName = "John Doe",
 *     network = "Visa",
 *     postalCode = "12345",
 *     countryCode = "US"
 * )
 *
 * // Extract fields for submission (automatically adds "card." prefix)
 * val fields = card.fields()
 * // Result: {"card.number": "4111...", "card.expiration_date": "29-11", ...}
 * ```
 *
 * Example - Tokenized card:
 * ```kotlin
 * val card = CardKYCFields(
 *     token = "tok_visa_1234",
 *     holderName = "John Doe",
 *     countryCode = "US"
 * )
 * ```
 *
 * See also:
 * - [NaturalPersonKYCFields] for individual customer fields
 * - [FinancialAccountKYCFields] for bank account information
 * - [SEP-0009 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md)
 *
 * @property number Card number (use tokenized alternative when possible)
 * @property expirationDate Expiration in YY-MM format (e.g., "29-11" for November 2029)
 * @property cvc CVC/CVV security code from back of card
 * @property holderName Name of the card holder as it appears on the card
 * @property network Card brand/network (e.g., Visa, Mastercard, AmEx)
 * @property postalCode Billing address postal code
 * @property countryCode Billing address country in ISO 3166-1 alpha-2 (e.g., "US")
 * @property stateOrProvince Billing address state/province in ISO 3166-2 format
 * @property city Billing address city
 * @property address Full billing address as multi-line string
 * @property token Token representation from external payment system (e.g., Stripe)
 */
data class CardKYCFields(
    val number: String? = null,
    val expirationDate: String? = null,
    val cvc: String? = null,
    val holderName: String? = null,
    val network: String? = null,
    val postalCode: String? = null,
    val countryCode: String? = null,
    val stateOrProvince: String? = null,
    val city: String? = null,
    val address: String? = null,
    val token: String? = null
) {
    companion object {
        private const val KEY_PREFIX = "card."
        const val NUMBER = KEY_PREFIX + "number"
        const val EXPIRATION_DATE = KEY_PREFIX + "expiration_date"
        const val CVC = KEY_PREFIX + "cvc"
        const val HOLDER_NAME = KEY_PREFIX + "holder_name"
        const val NETWORK = KEY_PREFIX + "network"
        const val POSTAL_CODE = KEY_PREFIX + "postal_code"
        const val COUNTRY_CODE = KEY_PREFIX + "country_code"
        const val STATE_OR_PROVINCE = KEY_PREFIX + "state_or_province"
        const val CITY = KEY_PREFIX + "city"
        const val ADDRESS = KEY_PREFIX + "address"
        const val TOKEN = KEY_PREFIX + "token"
    }

    /**
     * Converts all card KYC fields to a map for SEP-9 submission.
     *
     * Only fields with non-null values are included in the result. All keys
     * are automatically prefixed with "card." as required by SEP-9.
     *
     * @return Map of field keys (with "card." prefix) to string values for all non-null fields
     *
     * Example:
     * ```kotlin
     * val card = CardKYCFields(
     *     number = "4111111111111111",
     *     expirationDate = "29-11",
     *     cvc = "123"
     * )
     *
     * val fields = card.fields()
     * // Result: {
     * //   "card.number": "4111111111111111",
     * //   "card.expiration_date": "29-11",
     * //   "card.cvc": "123"
     * // }
     * ```
     */
    fun fields(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        number?.let { result[NUMBER] = it }
        expirationDate?.let { result[EXPIRATION_DATE] = it }
        cvc?.let { result[CVC] = it }
        holderName?.let { result[HOLDER_NAME] = it }
        network?.let { result[NETWORK] = it }
        postalCode?.let { result[POSTAL_CODE] = it }
        countryCode?.let { result[COUNTRY_CODE] = it }
        stateOrProvince?.let { result[STATE_OR_PROVINCE] = it }
        city?.let { result[CITY] = it }
        address?.let { result[ADDRESS] = it }
        token?.let { result[TOKEN] = it }

        return result
    }
}
