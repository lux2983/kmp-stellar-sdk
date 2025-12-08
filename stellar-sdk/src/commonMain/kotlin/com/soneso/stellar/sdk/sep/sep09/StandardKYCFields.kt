// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep09

/**
 * Implements SEP-0009 - Standard KYC Fields for Stellar Ecosystem.
 *
 * Defines standardized Know Your Customer (KYC) and Anti-Money Laundering (AML)
 * fields for use across the Stellar ecosystem. Anchors, exchanges, and other
 * regulated entities use these fields for consistent identity verification.
 *
 * This class serves as the top-level container for all SEP-09 KYC information,
 * supporting both individual and business customers.
 *
 * Implementation version: SEP-0009 v1.18.0
 *
 * Total fields: 76 across 4 categories
 * - Natural person: 34 fields (28 text + 6 binary)
 * - Organization: 17 fields (15 text + 2 binary)
 * - Financial account: 14 fields
 * - Card payment: 11 fields
 *
 * Field categories:
 * - Natural person fields (individuals) - [NaturalPersonKYCFields]
 * - Organization fields (businesses) - [OrganizationKYCFields]
 * - Financial account fields (bank accounts, crypto addresses) - [FinancialAccountKYCFields]
 * - Card payment fields (credit/debit cards) - [CardKYCFields]
 *
 * Use cases:
 * - Anchor deposit/withdrawal identity verification
 * - Exchange account registration
 * - Compliance requirements for regulated transfers
 * - Cross-border payment identity checks
 *
 * Example - Natural person:
 * ```kotlin
 * // Create KYC data for individual customer
 * val kyc = StandardKYCFields(
 *     naturalPersonKYCFields = NaturalPersonKYCFields(
 *         firstName = "John",
 *         lastName = "Doe",
 *         emailAddress = "john@example.com",
 *         birthDate = LocalDate(1990, 1, 15),
 *         idType = "passport",
 *         idNumber = "123456789",
 *         financialAccountKYCFields = FinancialAccountKYCFields(
 *             bankName = "Example Bank",
 *             bankAccountNumber = "1234567890"
 *         )
 *     )
 * )
 *
 * // Extract all fields for submission
 * val allFields = kyc.fields()
 * val allFiles = kyc.files()
 * ```
 *
 * Example - Organization:
 * ```kotlin
 * // Create KYC data for business customer
 * val kyc = StandardKYCFields(
 *     organizationKYCFields = OrganizationKYCFields(
 *         name = "Example Corp",
 *         VATNumber = "123456789",
 *         registrationNumber = "987654321",
 *         directorName = "Jane Smith",
 *         email = "contact@example.com",
 *         financialAccountKYCFields = FinancialAccountKYCFields(
 *             bankName = "Corporate Bank",
 *             bankAccountNumber = "9876543210"
 *         )
 *     )
 * )
 * ```
 *
 * Example - With SEP-12 submission:
 * ```kotlin
 * // Create KYC fields
 * val kycFields = StandardKYCFields(
 *     naturalPersonKYCFields = NaturalPersonKYCFields(
 *         firstName = "John",
 *         lastName = "Doe",
 *         emailAddress = "john@example.com"
 *     )
 * )
 *
 * // Submit to SEP-12 KYC service
 * val request = PutCustomerInfoRequest(
 *     jwt = authToken,
 *     kycFields = kycFields
 * )
 * val response = kycService.putCustomerInfo(request)
 * ```
 *
 * Field format standards:
 * - Date fields: ISO 8601 date-only (YYYY-MM-DD) via kotlinx.datetime.LocalDate
 * - Country codes: ISO 3166-1 alpha-3 (3 characters, e.g., "USA")
 * - Language codes: ISO 639-1 (2 characters, e.g., "en")
 * - Phone numbers: E.164 format (e.g., "+14155551234")
 * - Occupation: ISCO08 code (3 characters)
 * - Binary fields: ByteArray for document images (JPEG, PNG)
 *
 * Important notes:
 * - Fields follow ISO standards where applicable
 * - Document images should be in common formats (JPEG, PNG, PDF)
 * - Some fields may be required or optional depending on jurisdiction
 * - Organization fields use "organization." prefix
 * - Card fields use "card." prefix
 *
 * See also:
 * - [NaturalPersonKYCFields] for individual customer fields
 * - [OrganizationKYCFields] for business entity fields
 * - [FinancialAccountKYCFields] for bank account information
 * - [CardKYCFields] for payment card information
 * - [SEP-0009 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md)
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md) for Customer Info API
 *
 * @property naturalPersonKYCFields KYC fields for natural persons (individuals)
 * @property organizationKYCFields KYC fields for organizations (businesses)
 */
data class StandardKYCFields(
    val naturalPersonKYCFields: NaturalPersonKYCFields? = null,
    val organizationKYCFields: OrganizationKYCFields? = null
) {
    /**
     * Converts all text KYC fields to a map for SEP-9 submission.
     *
     * Aggregates fields from both natural person and organization fields,
     * if present. Only non-null fields are included. This method is typically
     * used when submitting KYC information via SEP-12 or similar protocols.
     *
     * @return Map of field keys to string values for all non-null text fields
     *
     * Example:
     * ```kotlin
     * val kyc = StandardKYCFields(
     *     naturalPersonKYCFields = NaturalPersonKYCFields(
     *         firstName = "John",
     *         lastName = "Doe"
     *     )
     * )
     *
     * val fields = kyc.fields()
     * // Result: {"first_name": "John", "last_name": "Doe"}
     * ```
     */
    fun fields(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        naturalPersonKYCFields?.fields()?.let { result.putAll(it) }
        organizationKYCFields?.fields()?.let { result.putAll(it) }

        return result
    }

    /**
     * Converts all binary KYC fields to a map for SEP-9 submission.
     *
     * Aggregates binary fields (document images) from both natural person and
     * organization fields, if present. Only non-null fields are included.
     * These are typically submitted via multipart/form-data.
     *
     * @return Map of field keys to byte arrays for all non-null binary fields
     *
     * Example:
     * ```kotlin
     * val kyc = StandardKYCFields(
     *     naturalPersonKYCFields = NaturalPersonKYCFields(
     *         photoIdFront = loadImageBytes("passport_front.jpg"),
     *         photoIdBack = loadImageBytes("passport_back.jpg")
     *     )
     * )
     *
     * val files = kyc.files()
     * // Result: {"photo_id_front": ByteArray(...), "photo_id_back": ByteArray(...)}
     * ```
     */
    fun files(): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()

        naturalPersonKYCFields?.files()?.let { result.putAll(it) }
        organizationKYCFields?.files()?.let { result.putAll(it) }

        return result
    }
}
