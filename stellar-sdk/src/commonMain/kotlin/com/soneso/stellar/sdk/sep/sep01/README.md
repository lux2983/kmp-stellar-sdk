# SEP-1: Stellar TOML Support

This package provides support for SEP-1 (Stellar TOML), a standardized way for organizations to publish information about their Stellar integration.

## Overview

SEP-1 defines the `stellar.toml` file format that organizations publish at `https://DOMAIN/.well-known/stellar.toml` to provide:

- Service endpoints (SEP-10 WebAuth, SEP-24 transfers, etc.)
- Organization information and contact details
- Supported currencies/assets
- Validator node information
- Stellar account associations

## Usage

### Fetch from Domain

```kotlin
// Fetch and parse stellar.toml from a domain
val stellarToml = StellarToml.fromDomain("example.com")

// Access general information
println("WebAuth endpoint: ${stellarToml.generalInformation.webAuthEndpoint}")
println("Transfer server: ${stellarToml.generalInformation.transferServer}")

// Access organization documentation
stellarToml.documentation?.let { doc ->
    println("Organization: ${doc.orgName}")
    println("Support email: ${doc.orgSupportEmail}")
}

// Iterate through supported currencies
stellarToml.currencies?.forEach { currency ->
    println("Currency: ${currency.code} issued by ${currency.issuer}")
    println("Description: ${currency.desc}")
}
```

### Parse from String

```kotlin
val tomlContent = """
    NETWORK_PASSPHRASE = "Public Global Stellar Network ; September 2015"
    WEB_AUTH_ENDPOINT = "https://example.com/auth"
    SIGNING_KEY = "GBWMCCC3NHSKLAOJDBKKYW7SSH2PFTTNVFKWSGLWGDLEBKLOVP5JLBBP"

    [DOCUMENTATION]
    ORG_NAME = "Example Organization"
    ORG_URL = "https://example.com"
""".trimIndent()

val stellarToml = StellarToml.parse(tomlContent)
println(stellarToml.generalInformation.webAuthEndpoint)
```

### Load External Currency TOML

```kotlin
// Some currencies may be defined in separate files
val currency = stellarToml.currencies?.firstOrNull()
currency?.toml?.let { url ->
    val fullCurrency = StellarToml.currencyFromUrl(url)
    println("Full currency details: ${fullCurrency.desc}")
}
```

## Common Use Cases

### Currency Lookup

```kotlin
// Find a specific currency by code
val stellarToml = StellarToml.fromDomain("example.com")
val usdCurrency = stellarToml.currencies?.find { it.code == "USD" }
if (usdCurrency != null) {
    println("Issuer: ${usdCurrency.issuer}")
    println("Contract: ${usdCurrency.contract}")
    println("Display decimals: ${usdCurrency.displayDecimals}")
}
```

### Domain Discovery Pattern

```kotlin
// Discover what services an anchor provides
val stellarToml = StellarToml.fromDomain("example.com")

// Check for SEP-6 support (transfer server)
val supportsSEP6 = stellarToml.generalInformation.transferServer != null

// Check for SEP-24 support (interactive transfer)
val supportsSEP24 = stellarToml.generalInformation.transferServerSep24 != null

// Check for SEP-10 support (web auth)
val supportsSEP10 = stellarToml.generalInformation.webAuthEndpoint != null

// Check for SEP-38 support (quotes)
val supportsSEP38 = stellarToml.generalInformation.anchorQuoteServer != null
```

### Account Verification

```kotlin
// Verify an account belongs to a domain
val stellarToml = StellarToml.fromDomain("example.com")
val accountToVerify = "GXYZ..."

val isVerified = stellarToml.generalInformation.accounts.contains(accountToVerify)
if (isVerified) {
    println("Account $accountToVerify is verified by example.com")
}
```

### Validator Discovery

```kotlin
// Find validators for a network
val stellarToml = StellarToml.fromDomain("example.com")
stellarToml.validators?.forEach { validator ->
    println("${validator.displayName}: ${validator.publicKey}")
    println("Host: ${validator.host}")
    println("History: ${validator.history}")
}
```

## Data Structures

### GeneralInformation

Contains service endpoints and network configuration:
- `version`: SEP-1 version
- `networkPassphrase`: Network identifier
- `federationServer`: SEP-2 federation endpoint
- `authServer`: SEP-3 compliance endpoint
- `transferServer`: SEP-6 anchor server
- `transferServerSep24`: SEP-24 interactive server
- `kycServer`: SEP-12 KYC server
- `webAuthEndpoint`: SEP-10 authentication endpoint
- `signingKey`: Signing key for SEP-10
- `horizonUrl`: Public Horizon instance
- `accounts`: List of Stellar accounts
- `uriRequestSigningKey`: SEP-7 signing key
- `directPaymentServer`: SEP-31 direct payment server
- `anchorQuoteServer`: SEP-38 quote server
- `webAuthForContractsEndpoint`: SEP-45 contract auth endpoint
- `webAuthContractId`: SEP-45 contract ID

### Documentation

Organization information:
- `orgName`: Legal name
- `orgUrl`: Official URL
- `orgDescription`: Organization description
- `orgPhysicalAddress`: Physical address
- `orgPhoneNumber`: Phone number (E.164 format)
- `orgKeybase`: Keybase account
- `orgTwitter`: Twitter account
- `orgGithub`: Github account
- `orgOfficialEmail`: Official contact email
- `orgSupportEmail`: Support email

### Currency

Asset/currency information:
- `code`: Asset code
- `issuer`: Issuer account ID
- `contract`: Smart contract ID (for Soroban tokens)
- `status`: live, dead, test, or private
- `displayDecimals`: Display precision
- `name`: Short name
- `desc`: Description
- `image`: Logo URL
- `isAssetAnchored`: Whether asset is redeemable
- `anchorAssetType`: Type (fiat, crypto, etc.)
- `anchorAsset`: Underlying asset
- `regulated`: SEP-8 compliance
- `approvalServer`: SEP-8 approval server

### PointOfContact

Principal/contact information:
- `name`: Full legal name
- `email`: Business email
- `keybase`: Personal Keybase
- `telegram`: Telegram account
- `twitter`: Twitter account
- `github`: Github account

### Validator

Validator node information:
- `alias`: Node alias
- `displayName`: Human-readable name
- `publicKey`: Node public key
- `host`: Connection endpoint
- `history`: History archive URL

## Error Handling

The implementation automatically handles common TOML formatting errors:

- `[ACCOUNTS]` → `[[ACCOUNTS]]` (corrects to array of tables)
- `[[DOCUMENTATION]]` → `[DOCUMENTATION]` (corrects to single table)
- `[PRINCIPALS]` → `[[PRINCIPALS]]` (corrects to array of tables)
- `[CURRENCIES]` → `[[CURRENCIES]]` (corrects to array of tables)
- `[VALIDATORS]` → `[[VALIDATORS]]` (corrects to array of tables)

Warnings are logged when corrections are applied.

## Reference

- [SEP-1 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0001.md)
- [Example stellar.toml](https://www.stellar.org/.well-known/stellar.toml)

## Platform Support

SEP-1 is fully supported on all platforms:
- JVM (Android, Server)
- JavaScript (Browser, Node.js)
- Native (iOS, macOS)

## Dependencies

No external dependencies required - uses a custom TOML parser optimized for stellar.toml files.
