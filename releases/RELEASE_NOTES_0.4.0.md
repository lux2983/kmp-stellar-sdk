# Release Notes: Version 0.4.0

**Release Date**: November 14, 2025
**Status**: Released

## Overview

Version 0.4.0 introduces SEP-1 (Stellar TOML) support, enabling discovery of service endpoints, account verification, and currency information from domain stellar.toml files. This release also includes automated API documentation deployment, demo app performance improvements, and comprehensive documentation enhancements with no breaking changes.

## What's New

### SEP-1: Stellar TOML Implementation

The SDK now provides complete support for SEP-1 (stellar.toml) with 71 fields across 5 data classes:

**Key Features**:
- **Domain Discovery**: Fetch stellar.toml from any domain
- **Service Endpoints**: Discover SEP-6, SEP-10, SEP-24, SEP-38 endpoints
- **Account Verification**: Verify domain ownership of Stellar accounts
- **Currency Information**: Find supported currencies with detailed properties
- **Validator Discovery**: Locate validator nodes operated by organizations

**API**:
- `StellarToml.fromDomain(domain)` - Fetch stellar.toml from domain
- `StellarToml.parse(toml)` - Parse stellar.toml from string
- `StellarToml.currencyFromUrl(url)` - Load external currency TOML files

**Data Classes**:
- `GeneralInformation` - Service endpoints and network configuration (16 fields)
- `Documentation` - Organization information (17 fields)
- `PointOfContact` - Contact information for principals (8 fields)
- `Currency` - Currency/asset information (25 fields)
- `Validator` - Validator node information (5 fields)

**Usage Example**:
```kotlin
// Fetch stellar.toml from a domain
val stellarToml = StellarToml.fromDomain("example.com")

// Access service endpoints
val webAuthEndpoint = stellarToml.generalInformation.webAuthEndpoint
val transferServer = stellarToml.generalInformation.transferServerSep24

// Find supported currencies
stellarToml.currencies?.forEach { currency ->
    println("${currency.code}: ${currency.desc}")
    println("Issuer: ${currency.issuer}")
}

// Verify account ownership
val isVerified = stellarToml.generalInformation.accounts.contains("GXYZ...")
```

**Testing**:
- 33 tests including integration tests with real-world stellar.toml files from stellar.org, testanchor.stellar.org, circle.com, and stellar.moneygram.com
- Network error handling and malformed TOML edge cases

**Documentation**: See `docs/sep-implementations.md` for 7 complete usage examples

**Specification**: [SEP-1: stellar.toml](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0001.md)

### GitHub Pages API Documentation

Automated API documentation deployment via GitHub Pages:
- Dokka-generated API reference for all SDK classes
- Versioned documentation for each release
- Available at: https://soneso.github.io/kmp-stellar-sdk/

### Demo App Performance Improvements (v1.3.0)

**Performance Enhancements**:
- Memoized form validation prevents UI freezes on iOS
- Optimized recomposition behavior during keyboard animations
- Applied to 6 key screens: SendPayment, InvokeAuth, InvokeToken, TrustAsset, InvokeHelloWorld, DeployContract

**UX Improvements**:
- Smart auto-scroll reveals results without aggressive scrolling
- Incremental scroll (300px) when results appear
- Improved scrolling behavior across all 11 demo screens

## What's Changed

### Documentation Overhaul

All documentation has been updated for accuracy and professional quality:

**Documentation Strategy Guide**: New `docs/documentation-strategy.md` defines core principles:
- Progressive learning approach
- SDK code only (no app architecture or hypothetical code)
- Examples over theory with complete working code
- Platform compatibility and one framework per platform
- Quality checklist and review process

**Platform Guides**: Fixed accuracy issues across macOS, iOS, JavaScript, and JVM guides:
- Corrected SDK class/property names (Asset.native → AssetTypeNative, response.isSuccess → response.successful)
- Removed hypothetical code not in SDK
- Enforced one framework example per platform
- Removed app architecture code

**Getting Started Guide**: Enhanced structure and accuracy:
- Fixed class name Scv → Scval in contract examples
- Restructured Soroban section for clarity
- Added contract deployment section with complete example
- Updated ContractClient examples with clearer patterns

**Compatibility Reports**: Enhanced Horizon and RPC matrices:
- Conditional gaps section (only shown when gaps exist)
- SDK version display
- 100% API coverage confirmation

## What's Fixed

### Infrastructure

- **GitHub Pages**: Fixed landing page redirect to api/latest/
- **Dokka Workflow**: Updated for Dokka V2 compatibility (dokkaGenerateHtml task)
- **Gradle Wrapper**: Resolved jar tracking and gitignore rule ordering issues
- **Build Artifacts**: Removed kotlin-js-store/ and stellar_sdk.xcframework/ from repository

## Migration from 0.3.0

No code changes required. Simply update your dependency version:

```kotlin
dependencies {
    implementation("com.soneso.stellar:stellar-sdk:0.4.0")
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
- **SEP-1**: Complete stellar.toml specification support

## Compatibility

Version 0.4.0 is fully compatible with 0.3.0. All APIs remain unchanged, with only new functionality added.

## Getting Help

If you encounter any issues:

1. Check the [documentation](https://github.com/Soneso/kmp-stellar-sdk)
2. Review the [SEP implementations guide](docs/sep-implementations.md)
3. Browse the [API reference](https://soneso.github.io/kmp-stellar-sdk/)
4. Explore the [demo app](demo/README.md)
5. Open an issue on [GitHub](https://github.com/Soneso/kmp-stellar-sdk/issues)

## Acknowledgments

This release adds ecosystem interoperability through SEP-1 support and improves developer experience through enhanced documentation and automated API reference generation.

Built with Claude Code - AI-powered development assistant.

---

**Previous Release**: [Version 0.3.0](RELEASE_NOTES_0.3.0.md) - Enhanced Horizon API Coverage and Complete Type Conversion
