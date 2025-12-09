# Release Notes: Version 0.6.0

**Release Date**: December 9, 2025
**Status**: Released

## Overview

Version 0.6.0 adds Know Your Customer (KYC) functionality through SEP-09 and SEP-12 implementations. These features enable applications to collect, submit, and manage customer verification data when interacting with Stellar anchors and regulated services.

## What's New

### SEP-09: Standard KYC Fields

Type-safe data classes for standardized KYC information across the Stellar ecosystem.

**Key Features**:
- **76 standardized fields** across four categories
- **Type safety**: `LocalDate` for dates, `ByteArray` for binary data
- **Automatic field prefixing**: Organization and card fields use correct SEP-09 prefixes
- **Composable design**: Combine field types as needed

**Data Classes**:
- `NaturalPersonKYCFields` - 34 fields for individual customers (name, address, ID documents, etc.)
- `OrganizationKYCFields` - 17 fields for business customers
- `FinancialAccountKYCFields` - 14 fields for bank account information
- `CardKYCFields` - 11 fields for payment card data
- `StandardKYCFields` - Composite class combining all field types

**Usage Example**:
```kotlin
val kycFields = NaturalPersonKYCFields(
    firstName = "John",
    lastName = "Doe",
    emailAddress = "john.doe@example.com",
    birthDate = LocalDate(1990, 1, 15),
    idType = "passport",
    idNumber = "AB123456",
    idCountryCode = "USA"
)

// Convert to map for API submission
val fieldsMap = kycFields.toMap()
```

**Testing**: 128 unit tests covering all field types and edge cases

**Documentation**: See `docs/sep/sep-09.md` for usage examples

**Specification**: [SEP-09: Standard KYC Fields](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0009.md) (v1.18.0)

### SEP-12: KYC API

Production-ready client for interacting with anchor KYC services.

**Key Features**:
- **Seven API endpoints** for complete customer lifecycle management
- **SEP-10 authentication** integrated across all endpoints
- **File uploads** via multipart/form-data (photos, documents)
- **Callback verification** for webhook signature validation
- **Granular error handling** with six exception types

**API Endpoints**:
- `getCustomer()` - Retrieve customer status and required fields
- `putCustomer()` - Submit or update customer information with file uploads
- `putCustomerVerification()` - Submit verification codes (email, phone, etc.)
- `deleteCustomer()` - Request customer data deletion (GDPR compliance)
- `getCustomerFiles()` - List previously uploaded files
- `putCustomerCallback()` - Register status change callback URLs

**Usage Example**:
```kotlin
// Initialize KYC service from domain
val kycService = KYCService.fromDomain("anchor.example.com")

// Authenticate with SEP-10
val webAuth = WebAuth.fromDomain("anchor.example.com", Network.TESTNET)
val authToken = webAuth.jwtToken(userKeyPair.getAccountId(), listOf(userKeyPair))

// Check customer status
val customer = kycService.getCustomer(
    request = GetCustomerInfoRequest(account = userKeyPair.getAccountId()),
    jwtToken = authToken.token
)

// Submit KYC information with file upload
val photoBytes = loadPhotoFromFile()
val putRequest = PutCustomerInfoRequest(
    account = userKeyPair.getAccountId(),
    kycFields = NaturalPersonKYCFields(
        firstName = "John",
        lastName = "Doe",
        emailAddress = "john@example.com"
    ),
    photoIdFront = photoBytes
)
val response = kycService.putCustomer(putRequest, authToken.token)
```

**Exception Types**:
- `CustomerNotFoundException` - Customer record not found (404)
- `CustomerAlreadyExistsException` - Duplicate registration attempt (409)
- `InvalidFieldException` - Field validation errors with details (400)
- `FileTooLargeException` - File exceeds size limits (413)
- `UnauthorizedException` - Authentication failures (401/403)
- `KYCException` - Base exception for other errors

**Callback Verification**:
```kotlin
val verifier = CallbackSignatureVerifier(anchorSigningKey)
val isValid = verifier.verify(
    signature = request.getHeader("Signature"),
    timestamp = request.getHeader("X-Stellar-Timestamp"),
    body = request.body
)
```

**Testing**: 105 unit tests + 13 integration tests against live Stellar testnet

**Documentation**: See `docs/sep/sep-12.md` for usage examples

**Specification**: [SEP-12: KYC API](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0012.md) (v1.15.0)

## Migration from 0.5.1

No code changes required. Simply update your dependency version:

```kotlin
dependencies {
    implementation("com.soneso.stellar:stellar-sdk:0.6.0")
}
```

## Platform Support

No changes to platform support:
- JVM (Android API 24+, Server Java 11+)
- iOS 14.0+
- macOS 11.0+
- JavaScript (Browser and Node.js 14+)

## API Coverage

The SDK maintains 100% API coverage:
- **Horizon API**: All endpoints with complete parameter support
- **Soroban RPC**: Full RPC method coverage
- **SEP-1**: Complete stellar.toml specification support (v2.7.0)
- **SEP-9**: All 76 standard KYC fields (v1.18.0)
- **SEP-10**: Complete client-side authentication support (v3.4.1)
- **SEP-12**: Full KYC API coverage (v1.15.0)

## Use Cases

SEP-09 and SEP-12 enable key regulatory compliance workflows:

**For Application Developers**:
- Collect required KYC information from users
- Submit verification documents to anchors
- Track customer verification status
- Handle verification code flows (email, phone)
- Implement GDPR-compliant data deletion

**For Wallet Developers**:
- Streamlined onboarding for anchor services
- Reusable KYC data across multiple anchors
- File upload support for identity documents
- Real-time status updates via callbacks

**For Anchor Integrations**:
- SEP-6 (Deposit/Withdrawal) - KYC required for fiat on/off ramps
- SEP-24 (Interactive) - KYC for interactive deposit/withdrawal flows
- SEP-31 (Cross-Border) - KYC for international payments

## Getting Help

If you encounter any issues:

1. Check the [documentation](https://github.com/Soneso/kmp-stellar-sdk)
2. Review the [SEP implementations guide](docs/sep/README.md)
3. Browse the [API reference](https://soneso.github.io/kmp-stellar-sdk/)
4. Explore the [demo app](demo/README.md)
5. Open an issue on [GitHub](https://github.com/Soneso/kmp-stellar-sdk/issues)

## Acknowledgments

This release enables regulatory-compliant KYC workflows for Stellar applications through production-ready SEP-09 and SEP-12 implementations.

Built with Claude Code - AI-powered development assistant.

---

**Previous Release**: [Version 0.5.1](RELEASE_NOTES_0.5.1.md) - SEP-10 (Web Authentication) and Dependency Updates
