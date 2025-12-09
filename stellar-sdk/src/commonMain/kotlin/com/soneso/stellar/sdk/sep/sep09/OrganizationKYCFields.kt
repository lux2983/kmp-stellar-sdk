// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep09

/**
 * KYC fields for organizations (businesses).
 *
 * Contains business entity identification information for corporate customers.
 * All field keys are automatically prefixed with "organization." when serialized.
 *
 * This class supports 17 total fields:
 * - 15 text fields
 * - 2 binary fields (document images)
 *
 * Field categories:
 * - Business identity: name, registration details, VAT number
 * - Registered address: country, state, city, postal code
 * - Contact information: email, phone, website
 * - Corporate structure: directors, shareholders
 * - Documents: incorporation papers, proof of address
 * - Financial accounts: bank accounts (via nested fields)
 *
 * Example - Basic organization:
 * ```kotlin
 * val org = OrganizationKYCFields(
 *     name = "Example Corp",
 *     VATNumber = "123456789",
 *     registrationNumber = "987654321",
 *     registrationDate = "2020-01-01",
 *     addressCountryCode = "USA",
 *     directorName = "Jane Smith",
 *     email = "contact@example.com"
 * )
 *
 * // Extract fields for submission (automatically adds "organization." prefix)
 * val fields = org.fields()
 * // Result: {"organization.name": "Example Corp", "organization.VAT_number": "123456789", ...}
 * ```
 *
 * Example - With documents:
 * ```kotlin
 * val org = OrganizationKYCFields(
 *     name = "Example Corp",
 *     photoIncorporationDoc = loadImageBytes("articles_of_incorporation.pdf"),
 *     photoProofAddress = loadImageBytes("utility_bill.jpg")
 * )
 *
 * val files = org.files()
 * // Result: {"organization.photo_incorporation_doc": ByteArray(...), ...}
 * ```
 *
 * Example - With nested financial account:
 * ```kotlin
 * val org = OrganizationKYCFields(
 *     name = "Example Corp",
 *     financialAccountKYCFields = FinancialAccountKYCFields(
 *         bankName = "Corporate Bank",
 *         bankAccountNumber = "9876543210"
 *     )
 * )
 *
 * val fields = org.fields()
 * // Result: {
 * //   "organization.name": "Example Corp",
 * //   "organization.bank_name": "Corporate Bank",
 * //   "organization.bank_account_number": "9876543210"
 * // }
 * ```
 *
 * See also:
 * - [StandardKYCFields] for the parent container
 * - [FinancialAccountKYCFields] for bank account information
 * - [SEP-0009 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md)
 *
 * @property name Full organization name as on incorporation papers
 * @property VATNumber Organization VAT number
 * @property registrationNumber Organization registration number
 * @property registrationDate Date the organization was registered (as string)
 * @property registeredAddress Organization registered address
 * @property numberOfShareholders Number of shareholders
 * @property shareholderName Name of shareholder (can be recursively queried for ultimate beneficial owners)
 * @property addressCountryCode Country code for address (ISO 3166-1 alpha-3)
 * @property stateOrProvince Name of state/province/region/prefecture
 * @property city Name of city/town
 * @property postalCode Postal code identifying organization's locale
 * @property directorName Organization registered managing director
 * @property website Organization website URL
 * @property email Organization contact email
 * @property phone Organization contact phone
 * @property photoIncorporationDoc Image of incorporation documents
 * @property photoProofAddress Image of utility bill or bank statement with organization's name and address
 * @property financialAccountKYCFields Nested financial account information
 * @property cardKYCFields Nested payment card information (not commonly used for organizations)
 */
data class OrganizationKYCFields(
    // Text fields - Identity
    val name: String? = null,
    val VATNumber: String? = null,
    val registrationNumber: String? = null,
    val registrationDate: String? = null,
    val registeredAddress: String? = null,

    // Text fields - Corporate structure
    val numberOfShareholders: Int? = null,
    val shareholderName: String? = null,

    // Text fields - Address
    val addressCountryCode: String? = null,
    val stateOrProvince: String? = null,
    val city: String? = null,
    val postalCode: String? = null,

    // Text fields - Management and contact
    val directorName: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phone: String? = null,

    // Binary fields - Documents
    val photoIncorporationDoc: ByteArray? = null,
    val photoProofAddress: ByteArray? = null,

    // Nested fields
    val financialAccountKYCFields: FinancialAccountKYCFields? = null,
    val cardKYCFields: CardKYCFields? = null
) {
    companion object {
        private const val KEY_PREFIX = "organization."

        // Text field keys
        const val NAME = KEY_PREFIX + "name"
        const val VAT_NUMBER = KEY_PREFIX + "VAT_number"
        const val REGISTRATION_NUMBER = KEY_PREFIX + "registration_number"
        const val REGISTRATION_DATE = KEY_PREFIX + "registration_date"
        const val REGISTERED_ADDRESS = KEY_PREFIX + "registered_address"
        const val NUMBER_OF_SHAREHOLDERS = KEY_PREFIX + "number_of_shareholders"
        const val SHAREHOLDER_NAME = KEY_PREFIX + "shareholder_name"
        const val ADDRESS_COUNTRY_CODE = KEY_PREFIX + "address_country_code"
        const val STATE_OR_PROVINCE = KEY_PREFIX + "state_or_province"
        const val CITY = KEY_PREFIX + "city"
        const val POSTAL_CODE = KEY_PREFIX + "postal_code"
        const val DIRECTOR_NAME = KEY_PREFIX + "director_name"
        const val WEBSITE = KEY_PREFIX + "website"
        const val EMAIL = KEY_PREFIX + "email"
        const val PHONE = KEY_PREFIX + "phone"

        // Binary field keys
        const val PHOTO_INCORPORATION_DOC = KEY_PREFIX + "photo_incorporation_doc"
        const val PHOTO_PROOF_ADDRESS = KEY_PREFIX + "photo_proof_address"
    }

    /**
     * Converts all organization text KYC fields to a map for SEP-9 submission.
     *
     * Only fields with non-null values are included in the result. All keys
     * are automatically prefixed with "organization." as required by SEP-9.
     * Nested financial account fields are also prefixed with "organization."
     *
     * @return Map of field keys (with "organization." prefix) to string values for all non-null text fields
     *
     * Example:
     * ```kotlin
     * val org = OrganizationKYCFields(
     *     name = "Example Corp",
     *     VATNumber = "123456789",
     *     numberOfShareholders = 5
     * )
     *
     * val fields = org.fields()
     * // Result: {
     * //   "organization.name": "Example Corp",
     * //   "organization.VAT_number": "123456789",
     * //   "organization.number_of_shareholders": "5"
     * // }
     * ```
     */
    fun fields(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        name?.let { result[NAME] = it }
        VATNumber?.let { result[VAT_NUMBER] = it }
        registrationNumber?.let { result[REGISTRATION_NUMBER] = it }
        registrationDate?.let { result[REGISTRATION_DATE] = it }
        registeredAddress?.let { result[REGISTERED_ADDRESS] = it }
        numberOfShareholders?.let { result[NUMBER_OF_SHAREHOLDERS] = it.toString() }
        shareholderName?.let { result[SHAREHOLDER_NAME] = it }
        addressCountryCode?.let { result[ADDRESS_COUNTRY_CODE] = it }
        stateOrProvince?.let { result[STATE_OR_PROVINCE] = it }
        city?.let { result[CITY] = it }
        postalCode?.let { result[POSTAL_CODE] = it }
        directorName?.let { result[DIRECTOR_NAME] = it }
        website?.let { result[WEBSITE] = it }
        email?.let { result[EMAIL] = it }
        phone?.let { result[PHONE] = it }

        // Include nested fields with organization prefix
        financialAccountKYCFields?.fields(keyPrefix = KEY_PREFIX)?.let { result.putAll(it) }
        cardKYCFields?.fields()?.let { result.putAll(it) }

        return result
    }

    /**
     * Converts all organization binary KYC fields to a map for SEP-9 submission.
     *
     * Only fields with non-null values are included in the result. All keys
     * are automatically prefixed with "organization." as required by SEP-9.
     * These are typically document images submitted via multipart/form-data.
     *
     * @return Map of field keys (with "organization." prefix) to byte arrays for all non-null binary fields
     *
     * Example:
     * ```kotlin
     * val org = OrganizationKYCFields(
     *     photoIncorporationDoc = loadImageBytes("incorporation.pdf"),
     *     photoProofAddress = loadImageBytes("utility_bill.jpg")
     * )
     *
     * val files = org.files()
     * // Result: {
     * //   "organization.photo_incorporation_doc": ByteArray(...),
     * //   "organization.photo_proof_address": ByteArray(...)
     * // }
     * ```
     */
    fun files(): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()

        photoIncorporationDoc?.let { result[PHOTO_INCORPORATION_DOC] = it }
        photoProofAddress?.let { result[PHOTO_PROOF_ADDRESS] = it }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as OrganizationKYCFields

        if (name != other.name) return false
        if (VATNumber != other.VATNumber) return false
        if (registrationNumber != other.registrationNumber) return false
        if (registrationDate != other.registrationDate) return false
        if (registeredAddress != other.registeredAddress) return false
        if (numberOfShareholders != other.numberOfShareholders) return false
        if (shareholderName != other.shareholderName) return false
        if (addressCountryCode != other.addressCountryCode) return false
        if (stateOrProvince != other.stateOrProvince) return false
        if (city != other.city) return false
        if (postalCode != other.postalCode) return false
        if (directorName != other.directorName) return false
        if (website != other.website) return false
        if (email != other.email) return false
        if (phone != other.phone) return false
        if (photoIncorporationDoc != null) {
            if (other.photoIncorporationDoc == null) return false
            if (!photoIncorporationDoc.contentEquals(other.photoIncorporationDoc)) return false
        } else if (other.photoIncorporationDoc != null) return false
        if (photoProofAddress != null) {
            if (other.photoProofAddress == null) return false
            if (!photoProofAddress.contentEquals(other.photoProofAddress)) return false
        } else if (other.photoProofAddress != null) return false
        if (financialAccountKYCFields != other.financialAccountKYCFields) return false
        if (cardKYCFields != other.cardKYCFields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (VATNumber?.hashCode() ?: 0)
        result = 31 * result + (registrationNumber?.hashCode() ?: 0)
        result = 31 * result + (registrationDate?.hashCode() ?: 0)
        result = 31 * result + (registeredAddress?.hashCode() ?: 0)
        result = 31 * result + (numberOfShareholders ?: 0)
        result = 31 * result + (shareholderName?.hashCode() ?: 0)
        result = 31 * result + (addressCountryCode?.hashCode() ?: 0)
        result = 31 * result + (stateOrProvince?.hashCode() ?: 0)
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (postalCode?.hashCode() ?: 0)
        result = 31 * result + (directorName?.hashCode() ?: 0)
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (photoIncorporationDoc?.contentHashCode() ?: 0)
        result = 31 * result + (photoProofAddress?.contentHashCode() ?: 0)
        result = 31 * result + (financialAccountKYCFields?.hashCode() ?: 0)
        result = 31 * result + (cardKYCFields?.hashCode() ?: 0)
        return result
    }
}
