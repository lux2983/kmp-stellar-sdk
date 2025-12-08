// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep12.exceptions

/**
 * Exception thrown when field validation fails (HTTP 400).
 *
 * Indicates that one or more fields contain invalid data. The server has rejected
 * the request due to validation errors. This typically occurs when:
 * - Field format is incorrect (e.g., invalid email, phone number)
 * - Field value is out of range or doesn't match expected pattern
 * - Required field is missing
 * - Field value doesn't match allowed choices
 * - Date format is incorrect
 *
 * The exception contains information about which field failed and why, allowing
 * applications to provide specific feedback to users.
 *
 * Example - Handle validation errors:
 * ```kotlin
 * suspend fun submitCustomerInfo(
 *     kycService: KYCService,
 *     request: PutCustomerInfoRequest
 * ) {
 *     try {
 *         kycService.putCustomerInfo(request)
 *     } catch (e: InvalidFieldException) {
 *         val fieldName = e.fieldName ?: "unknown field"
 *         val error = e.fieldError ?: "validation failed"
 *
 *         println("Invalid field: $fieldName")
 *         println("Error: $error")
 *
 *         // Show user-friendly error message
 *         showFieldError(fieldName, error)
 *     }
 * }
 *
 * fun showFieldError(fieldName: String, error: String) {
 *     when (fieldName) {
 *         "email_address" -> showError("Invalid email address: $error")
 *         "mobile_number" -> showError("Invalid phone number: $error")
 *         "birth_date" -> showError("Invalid birth date: $error")
 *         else -> showError("Invalid $fieldName: $error")
 *     }
 * }
 * ```
 *
 * Example - Validate before submission:
 * ```kotlin
 * data class ValidationError(val field: String, val message: String)
 *
 * fun validateCustomerData(fields: NaturalPersonKYCFields): List<ValidationError> {
 *     val errors = mutableListOf<ValidationError>()
 *
 *     // Validate email format
 *     fields.emailAddress?.let { email ->
 *         if (!email.contains("@")) {
 *             errors.add(ValidationError("email_address", "Invalid email format"))
 *         }
 *     }
 *
 *     // Validate phone format (E.164)
 *     fields.mobileNumber?.let { phone ->
 *         if (!phone.matches(Regex("^\\+[1-9]\\d{1,14}$"))) {
 *             errors.add(ValidationError("mobile_number", "Invalid phone format (use E.164)"))
 *         }
 *     }
 *
 *     // Validate birth date (must be over 18)
 *     fields.birthDate?.let { birthDate ->
 *         val age = LocalDate.now().year - birthDate.year
 *         if (age < 18) {
 *             errors.add(ValidationError("birth_date", "Must be at least 18 years old"))
 *         }
 *     }
 *
 *     return errors
 * }
 *
 * suspend fun submitWithValidation(
 *     kycService: KYCService,
 *     fields: NaturalPersonKYCFields,
 *     jwt: String
 * ) {
 *     // Pre-validate locally
 *     val errors = validateCustomerData(fields)
 *     if (errors.isNotEmpty()) {
 *         println("Validation errors:")
 *         errors.forEach { println("  ${it.field}: ${it.message}") }
 *         return
 *     }
 *
 *     // Submit to server
 *     try {
 *         kycService.putCustomerInfo(
 *             PutCustomerInfoRequest(
 *                 jwt = jwt,
 *                 kycFields = StandardKYCFields(naturalPersonKYCFields = fields)
 *             )
 *         )
 *     } catch (e: InvalidFieldException) {
 *         println("Server validation failed: ${e.fieldName} - ${e.fieldError}")
 *     }
 * }
 * ```
 *
 * Example - Handle multiple field errors:
 * ```kotlin
 * suspend fun submitCustomerWithRetry(
 *     kycService: KYCService,
 *     fields: NaturalPersonKYCFields,
 *     jwt: String
 * ) {
 *     var currentFields = fields
 *
 *     while (true) {
 *         try {
 *             kycService.putCustomerInfo(
 *                 PutCustomerInfoRequest(
 *                     jwt = jwt,
 *                     kycFields = StandardKYCFields(naturalPersonKYCFields = currentFields)
 *                 )
 *             )
 *             break // Success
 *         } catch (e: InvalidFieldException) {
 *             println("Field error: ${e.fieldName} - ${e.fieldError}")
 *
 *             // Fix the specific field and retry
 *             currentFields = fixField(currentFields, e.fieldName, e.fieldError)
 *                 ?: break // Cannot fix, give up
 *         }
 *     }
 * }
 *
 * fun fixField(
 *     fields: NaturalPersonKYCFields,
 *     fieldName: String?,
 *     error: String?
 * ): NaturalPersonKYCFields? {
 *     return when (fieldName) {
 *         "mobile_number" -> {
 *             // Format phone number correctly
 *             val formattedPhone = formatPhoneToE164(fields.mobileNumber ?: "")
 *             fields.copy(mobileNumber = formattedPhone)
 *         }
 *         "email_address" -> {
 *             // Ask user to correct email
 *             val newEmail = promptUserForEmail()
 *             fields.copy(emailAddress = newEmail)
 *         }
 *         else -> null // Cannot auto-fix
 *     }
 * }
 * ```
 *
 * See also:
 * - [KYCException] base class
 * - [GetCustomerInfoResponse] for field requirements
 * - [PutCustomerInfoRequest] for submitting customer data
 * - [SEP-0009 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md) for field formats
 * - [SEP-0012 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md)
 *
 * @property fieldName The name of the field that failed validation (null if not specified)
 * @property fieldError Human-readable description of the validation error (null if not specified)
 */
class InvalidFieldException(
    val fieldName: String? = null,
    val fieldError: String? = null
) : KYCException(
    message = buildString {
        append("Invalid field data (400 Bad Request). ")
        if (fieldName != null) {
            append("Field: '$fieldName'. ")
        }
        if (fieldError != null) {
            append("Error: $fieldError")
        } else {
            append("Please check the field format and value.")
        }
    }
)
