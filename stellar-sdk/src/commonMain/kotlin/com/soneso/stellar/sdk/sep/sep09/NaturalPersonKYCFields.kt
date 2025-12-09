// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep09

import kotlinx.datetime.LocalDate

/**
 * KYC fields for natural persons (individuals).
 *
 * Contains personal identification information for individual customers.
 * Fields follow international standards (ISO 3166, ISO 639, E.164) where applicable.
 *
 * This class supports 34 total fields:
 * - 28 text fields (including dates serialized as ISO 8601 date-only format)
 * - 6 binary fields (document images/photos)
 *
 * Field categories:
 * - Identity: name, email, phone, address
 * - Birth information: date, place, country
 * - Government ID: type, number, country, dates
 * - Tax information: tax ID and name
 * - Employment: occupation, employer details
 * - Verification documents: photos of ID, proof of residence, income, liveness
 * - Financial accounts: bank accounts, crypto addresses (via nested fields)
 * - Payment cards: card details (via nested fields)
 *
 * Example - Basic information:
 * ```kotlin
 * val person = NaturalPersonKYCFields(
 *     firstName = "John",
 *     lastName = "Doe",
 *     emailAddress = "john@example.com",
 *     birthDate = LocalDate(1990, 1, 15),
 *     addressCountryCode = "USA",
 *     idType = "passport",
 *     idNumber = "123456789"
 * )
 *
 * // Extract text fields for submission
 * val textFields = person.fields()
 * // Result: {"first_name": "John", "last_name": "Doe", "birth_date": "1990-01-15", ...}
 * ```
 *
 * Example - With documents:
 * ```kotlin
 * val person = NaturalPersonKYCFields(
 *     firstName = "John",
 *     lastName = "Doe",
 *     photoIdFront = loadImageBytes("passport_front.jpg"),
 *     photoIdBack = loadImageBytes("passport_back.jpg"),
 *     photoProofResidence = loadImageBytes("utility_bill.jpg")
 * )
 *
 * // Extract document files for multipart upload
 * val fileFields = person.files()
 * // Result: {"photo_id_front": ByteArray(...), "photo_id_back": ByteArray(...), ...}
 * ```
 *
 * Example - With nested financial account:
 * ```kotlin
 * val person = NaturalPersonKYCFields(
 *     firstName = "John",
 *     lastName = "Doe",
 *     financialAccountKYCFields = FinancialAccountKYCFields(
 *         bankName = "Example Bank",
 *         bankAccountNumber = "1234567890"
 *     )
 * )
 *
 * val fields = person.fields()
 * // Result includes both person fields and bank fields: {"first_name": "John", "bank_name": "Example Bank", ...}
 * ```
 *
 * Date field formats:
 * - `birthDate`, `idIssueDate`, `idExpirationDate` use kotlinx.datetime.LocalDate
 * - Serialized as ISO 8601 date-only format (YYYY-MM-DD) via toString()
 *
 * See also:
 * - [StandardKYCFields] for the parent container
 * - [FinancialAccountKYCFields] for bank account information
 * - [CardKYCFields] for payment card information
 * - [SEP-0009 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md)
 *
 * @property lastName Family or last name
 * @property firstName Given or first name
 * @property additionalName Middle name or other additional name
 * @property addressCountryCode Country code for current address (ISO 3166-1 alpha-3)
 * @property stateOrProvince Name of state/province/region/prefecture
 * @property city Name of city/town
 * @property postalCode Postal or other code identifying user's locale
 * @property address Entire address as a multi-line string
 * @property mobileNumber Mobile phone number with country code in E.164 format
 * @property mobileNumberFormat Expected format of mobile_number field (default: E.164)
 * @property emailAddress Email address
 * @property birthDate Date of birth (serialized as ISO 8601 date-only: YYYY-MM-DD, e.g., "1990-01-15")
 * @property birthPlace Place of birth (city, state, country as on passport)
 * @property birthCountryCode Country of birth (ISO 3166-1 alpha-3)
 * @property taxId Tax identifier (e.g., SSN in US)
 * @property taxIdName Name of the tax ID type (e.g., "SSN" or "ITIN" in US)
 * @property occupation Occupation ISCO08 code as string (3 characters, e.g., "111" for legislators)
 * @property employerName Name of employer
 * @property employerAddress Address of employer
 * @property languageCode Primary language (ISO 639-1, 2 characters)
 * @property idType Type of ID (e.g., passport, drivers_license, id_card)
 * @property idCountryCode Country issuing ID (ISO 3166-1 alpha-3)
 * @property idIssueDate ID issue date (serialized as ISO 8601 date-only: YYYY-MM-DD, e.g., "2020-06-01")
 * @property idExpirationDate ID expiration date (serialized as ISO 8601 date-only: YYYY-MM-DD, e.g., "2030-06-01")
 * @property idNumber Passport or ID number
 * @property ipAddress IP address of customer's computer
 * @property sex Gender (male, female, or other)
 * @property referralId User's origin or referral code
 * @property photoIdFront Image of front of photo ID or passport
 * @property photoIdBack Image of back of photo ID or passport
 * @property notaryApprovalOfPhotoId Image of notary's approval of photo ID
 * @property photoProofResidence Image of utility bill or bank statement with name and address
 * @property proofOfIncome Image of proof of income document
 * @property proofOfLiveness Video or image file as liveness proof
 * @property financialAccountKYCFields Nested financial account information
 * @property cardKYCFields Nested payment card information
 */
data class NaturalPersonKYCFields(
    // Text fields - Identity
    val lastName: String? = null,
    val firstName: String? = null,
    val additionalName: String? = null,

    // Text fields - Address
    val addressCountryCode: String? = null,
    val stateOrProvince: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val address: String? = null,

    // Text fields - Contact
    val mobileNumber: String? = null,
    val mobileNumberFormat: String? = null,
    val emailAddress: String? = null,

    // Date fields
    val birthDate: LocalDate? = null,
    val idIssueDate: LocalDate? = null,
    val idExpirationDate: LocalDate? = null,

    // Text fields - Birth information
    val birthPlace: String? = null,
    val birthCountryCode: String? = null,

    // Text fields - Tax
    val taxId: String? = null,
    val taxIdName: String? = null,

    // Text fields - Employment
    val occupation: String? = null,
    val employerName: String? = null,
    val employerAddress: String? = null,

    // Text fields - Language
    val languageCode: String? = null,

    // Text fields - ID
    val idType: String? = null,
    val idCountryCode: String? = null,
    val idNumber: String? = null,

    // Text fields - Other
    val ipAddress: String? = null,
    val sex: String? = null,
    val referralId: String? = null,

    // Binary fields - Documents
    val photoIdFront: ByteArray? = null,
    val photoIdBack: ByteArray? = null,
    val notaryApprovalOfPhotoId: ByteArray? = null,
    val photoProofResidence: ByteArray? = null,
    val proofOfIncome: ByteArray? = null,
    val proofOfLiveness: ByteArray? = null,

    // Nested fields
    val financialAccountKYCFields: FinancialAccountKYCFields? = null,
    val cardKYCFields: CardKYCFields? = null
) {
    companion object {
        // Text field keys
        const val LAST_NAME = "last_name"
        const val FIRST_NAME = "first_name"
        const val ADDITIONAL_NAME = "additional_name"
        const val ADDRESS_COUNTRY_CODE = "address_country_code"
        const val STATE_OR_PROVINCE = "state_or_province"
        const val CITY = "city"
        const val POSTAL_CODE = "postal_code"
        const val ADDRESS = "address"
        const val MOBILE_NUMBER = "mobile_number"
        const val MOBILE_NUMBER_FORMAT = "mobile_number_format"
        const val EMAIL_ADDRESS = "email_address"
        const val BIRTH_DATE = "birth_date"
        const val BIRTH_PLACE = "birth_place"
        const val BIRTH_COUNTRY_CODE = "birth_country_code"
        const val TAX_ID = "tax_id"
        const val TAX_ID_NAME = "tax_id_name"
        const val OCCUPATION = "occupation"
        const val EMPLOYER_NAME = "employer_name"
        const val EMPLOYER_ADDRESS = "employer_address"
        const val LANGUAGE_CODE = "language_code"
        const val ID_TYPE = "id_type"
        const val ID_COUNTRY_CODE = "id_country_code"
        const val ID_ISSUE_DATE = "id_issue_date"
        const val ID_EXPIRATION_DATE = "id_expiration_date"
        const val ID_NUMBER = "id_number"
        const val IP_ADDRESS = "ip_address"
        const val SEX = "sex"
        const val REFERRAL_ID = "referral_id"

        // Binary field keys
        const val PHOTO_ID_FRONT = "photo_id_front"
        const val PHOTO_ID_BACK = "photo_id_back"
        const val NOTARY_APPROVAL_OF_PHOTO_ID = "notary_approval_of_photo_id"
        const val PHOTO_PROOF_RESIDENCE = "photo_proof_residence"
        const val PROOF_OF_INCOME = "proof_of_income"
        const val PROOF_OF_LIVENESS = "proof_of_liveness"
    }

    /**
     * Converts all natural person text KYC fields to a map for SEP-9 submission.
     *
     * Only fields with non-null values are included in the result. Date fields
     * are serialized as ISO 8601 date-only format (YYYY-MM-DD) using LocalDate.toString().
     * Nested financial account and card fields are automatically included.
     *
     * @return Map of field keys to string values for all non-null text fields
     *
     * Example:
     * ```kotlin
     * val person = NaturalPersonKYCFields(
     *     firstName = "John",
     *     lastName = "Doe",
     *     birthDate = LocalDate(1990, 1, 15),
     *     emailAddress = "john@example.com"
     * )
     *
     * val fields = person.fields()
     * // Result: {
     * //   "first_name": "John",
     * //   "last_name": "Doe",
     * //   "birth_date": "1990-01-15",
     * //   "email_address": "john@example.com"
     * // }
     * ```
     */
    fun fields(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        lastName?.let { result[LAST_NAME] = it }
        firstName?.let { result[FIRST_NAME] = it }
        additionalName?.let { result[ADDITIONAL_NAME] = it }
        addressCountryCode?.let { result[ADDRESS_COUNTRY_CODE] = it }
        stateOrProvince?.let { result[STATE_OR_PROVINCE] = it }
        city?.let { result[CITY] = it }
        postalCode?.let { result[POSTAL_CODE] = it }
        address?.let { result[ADDRESS] = it }
        mobileNumber?.let { result[MOBILE_NUMBER] = it }
        mobileNumberFormat?.let { result[MOBILE_NUMBER_FORMAT] = it }
        emailAddress?.let { result[EMAIL_ADDRESS] = it }
        birthDate?.let { result[BIRTH_DATE] = it.toString() }
        birthPlace?.let { result[BIRTH_PLACE] = it }
        birthCountryCode?.let { result[BIRTH_COUNTRY_CODE] = it }
        taxId?.let { result[TAX_ID] = it }
        taxIdName?.let { result[TAX_ID_NAME] = it }
        occupation?.let { result[OCCUPATION] = it }
        employerName?.let { result[EMPLOYER_NAME] = it }
        employerAddress?.let { result[EMPLOYER_ADDRESS] = it }
        languageCode?.let { result[LANGUAGE_CODE] = it }
        idType?.let { result[ID_TYPE] = it }
        idCountryCode?.let { result[ID_COUNTRY_CODE] = it }
        idIssueDate?.let { result[ID_ISSUE_DATE] = it.toString() }
        idExpirationDate?.let { result[ID_EXPIRATION_DATE] = it.toString() }
        idNumber?.let { result[ID_NUMBER] = it }
        ipAddress?.let { result[IP_ADDRESS] = it }
        sex?.let { result[SEX] = it }
        referralId?.let { result[REFERRAL_ID] = it }

        // Include nested fields
        financialAccountKYCFields?.fields()?.let { result.putAll(it) }
        cardKYCFields?.fields()?.let { result.putAll(it) }

        return result
    }

    /**
     * Converts all natural person binary KYC fields to a map for SEP-9 submission.
     *
     * Only fields with non-null values are included in the result. These are typically
     * document images submitted via multipart/form-data.
     *
     * @return Map of field keys to byte arrays for all non-null binary fields
     *
     * Example:
     * ```kotlin
     * val person = NaturalPersonKYCFields(
     *     photoIdFront = loadImageBytes("passport_front.jpg"),
     *     photoIdBack = loadImageBytes("passport_back.jpg")
     * )
     *
     * val files = person.files()
     * // Result: {
     * //   "photo_id_front": ByteArray(...),
     * //   "photo_id_back": ByteArray(...)
     * // }
     * ```
     */
    fun files(): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()

        photoIdFront?.let { result[PHOTO_ID_FRONT] = it }
        photoIdBack?.let { result[PHOTO_ID_BACK] = it }
        notaryApprovalOfPhotoId?.let { result[NOTARY_APPROVAL_OF_PHOTO_ID] = it }
        photoProofResidence?.let { result[PHOTO_PROOF_RESIDENCE] = it }
        proofOfIncome?.let { result[PROOF_OF_INCOME] = it }
        proofOfLiveness?.let { result[PROOF_OF_LIVENESS] = it }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NaturalPersonKYCFields

        if (lastName != other.lastName) return false
        if (firstName != other.firstName) return false
        if (additionalName != other.additionalName) return false
        if (addressCountryCode != other.addressCountryCode) return false
        if (stateOrProvince != other.stateOrProvince) return false
        if (city != other.city) return false
        if (postalCode != other.postalCode) return false
        if (address != other.address) return false
        if (mobileNumber != other.mobileNumber) return false
        if (mobileNumberFormat != other.mobileNumberFormat) return false
        if (emailAddress != other.emailAddress) return false
        if (birthDate != other.birthDate) return false
        if (idIssueDate != other.idIssueDate) return false
        if (idExpirationDate != other.idExpirationDate) return false
        if (birthPlace != other.birthPlace) return false
        if (birthCountryCode != other.birthCountryCode) return false
        if (taxId != other.taxId) return false
        if (taxIdName != other.taxIdName) return false
        if (occupation != other.occupation) return false
        if (employerName != other.employerName) return false
        if (employerAddress != other.employerAddress) return false
        if (languageCode != other.languageCode) return false
        if (idType != other.idType) return false
        if (idCountryCode != other.idCountryCode) return false
        if (idNumber != other.idNumber) return false
        if (ipAddress != other.ipAddress) return false
        if (sex != other.sex) return false
        if (referralId != other.referralId) return false
        if (photoIdFront != null) {
            if (other.photoIdFront == null) return false
            if (!photoIdFront.contentEquals(other.photoIdFront)) return false
        } else if (other.photoIdFront != null) return false
        if (photoIdBack != null) {
            if (other.photoIdBack == null) return false
            if (!photoIdBack.contentEquals(other.photoIdBack)) return false
        } else if (other.photoIdBack != null) return false
        if (notaryApprovalOfPhotoId != null) {
            if (other.notaryApprovalOfPhotoId == null) return false
            if (!notaryApprovalOfPhotoId.contentEquals(other.notaryApprovalOfPhotoId)) return false
        } else if (other.notaryApprovalOfPhotoId != null) return false
        if (photoProofResidence != null) {
            if (other.photoProofResidence == null) return false
            if (!photoProofResidence.contentEquals(other.photoProofResidence)) return false
        } else if (other.photoProofResidence != null) return false
        if (proofOfIncome != null) {
            if (other.proofOfIncome == null) return false
            if (!proofOfIncome.contentEquals(other.proofOfIncome)) return false
        } else if (other.proofOfIncome != null) return false
        if (proofOfLiveness != null) {
            if (other.proofOfLiveness == null) return false
            if (!proofOfLiveness.contentEquals(other.proofOfLiveness)) return false
        } else if (other.proofOfLiveness != null) return false
        if (financialAccountKYCFields != other.financialAccountKYCFields) return false
        if (cardKYCFields != other.cardKYCFields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastName?.hashCode() ?: 0
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (additionalName?.hashCode() ?: 0)
        result = 31 * result + (addressCountryCode?.hashCode() ?: 0)
        result = 31 * result + (stateOrProvince?.hashCode() ?: 0)
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (postalCode?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (mobileNumber?.hashCode() ?: 0)
        result = 31 * result + (mobileNumberFormat?.hashCode() ?: 0)
        result = 31 * result + (emailAddress?.hashCode() ?: 0)
        result = 31 * result + (birthDate?.hashCode() ?: 0)
        result = 31 * result + (idIssueDate?.hashCode() ?: 0)
        result = 31 * result + (idExpirationDate?.hashCode() ?: 0)
        result = 31 * result + (birthPlace?.hashCode() ?: 0)
        result = 31 * result + (birthCountryCode?.hashCode() ?: 0)
        result = 31 * result + (taxId?.hashCode() ?: 0)
        result = 31 * result + (taxIdName?.hashCode() ?: 0)
        result = 31 * result + (occupation?.hashCode() ?: 0)
        result = 31 * result + (employerName?.hashCode() ?: 0)
        result = 31 * result + (employerAddress?.hashCode() ?: 0)
        result = 31 * result + (languageCode?.hashCode() ?: 0)
        result = 31 * result + (idType?.hashCode() ?: 0)
        result = 31 * result + (idCountryCode?.hashCode() ?: 0)
        result = 31 * result + (idNumber?.hashCode() ?: 0)
        result = 31 * result + (ipAddress?.hashCode() ?: 0)
        result = 31 * result + (sex?.hashCode() ?: 0)
        result = 31 * result + (referralId?.hashCode() ?: 0)
        result = 31 * result + (photoIdFront?.contentHashCode() ?: 0)
        result = 31 * result + (photoIdBack?.contentHashCode() ?: 0)
        result = 31 * result + (notaryApprovalOfPhotoId?.contentHashCode() ?: 0)
        result = 31 * result + (photoProofResidence?.contentHashCode() ?: 0)
        result = 31 * result + (proofOfIncome?.contentHashCode() ?: 0)
        result = 31 * result + (proofOfLiveness?.contentHashCode() ?: 0)
        result = 31 * result + (financialAccountKYCFields?.hashCode() ?: 0)
        result = 31 * result + (cardKYCFields?.hashCode() ?: 0)
        return result
    }
}
