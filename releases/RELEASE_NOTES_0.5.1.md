# Release Notes: Version 0.5.1

**Release Date**: November 25, 2025
**Status**: Released

## Overview

Version 0.5.1 introduces SEP-10 (Stellar Web Authentication) client-side support, enabling secure authentication with Stellar anchors and other services. This release also includes dependency updates to current stable versions.

## What's New

### SEP-10: Stellar Web Authentication

The SDK now provides production-ready client-side support for SEP-10 Web Authentication:

**Key Features**:
- **Secure Authentication**: Challenge-response protocol using transaction signing
- **Full Validation**: All 13 SEP-10 validation checks implemented
- **Flexible Account Types**: Support for standard accounts (G...), muxed accounts (M...), and memo IDs
- **Client Domain Verification**: Prove wallet identity to receive premium benefits
- **Multi-Signature Support**: Sign with multiple keypairs, preserving all signatures
- **Enterprise Ready**: ClientDomainSigningDelegate for wallet backend infrastructure

**API**:
- `WebAuth.fromDomain(domain, network)` - Initialize from domain's stellar.toml
- `WebAuth.jwtToken(accountId, signers)` - High-level authentication (one call)
- `AuthToken` class - JWT token with properties (account, memo, jti, isExpired, etc.)
- Low-level API available for advanced workflows

**Usage Example**:
```kotlin
// Initialize from domain's stellar.toml
val webAuth = WebAuth.fromDomain("testanchor.stellar.org", Network.TESTNET)

// Generate keypair
val userKeyPair = KeyPair.random()

// Authenticate and get JWT token
val authToken = webAuth.jwtToken(
    clientAccountId = userKeyPair.getAccountId(),
    signers = listOf(userKeyPair)
)

// Use token in API requests
val response = httpClient.get("https://testanchor.stellar.org/kyc/customer") {
    header("Authorization", "Bearer ${authToken.token}")
}
```

**Client Domain Authentication** (for wallets):
```kotlin
// Wallet proves its identity to receive benefits
val authToken = webAuth.jwtToken(
    clientAccountId = userKeyPair.getAccountId(),
    signers = listOf(userKeyPair),
    clientDomain = "wallet.example.com",
    clientDomainSigningDelegate = MyWalletSigningDelegate()
)
```

**Testing**:
- 115+ tests including integration tests against live Stellar testnet anchor
- All 13 validation checks tested with edge cases
- Signature reordering tests for enterprise HSM/custody scenarios

**Documentation**: See `docs/sep/sep-10.md` for complete usage examples

**Specification**: [SEP-10: Stellar Web Authentication](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md) (v3.4.1)

### Dependency Upgrades

Updated to current stable versions:

- **Ktor**: 2.3.8 → 3.3.2
- **kotlinx-coroutines**: 1.8.0 → 1.10.2
- **kotlinx-serialization**: 1.6.3 → 1.9.0
- **bignum**: 0.3.9 → 0.3.10

All tests passing with updated dependencies. No breaking changes to public API.

## What's Changed

### Dependency Upgrades

All dependencies upgraded to latest stable versions. See "Dependency Upgrades" section above for details. No breaking changes to public API.

### Compatibility Matrices

All compatibility matrices updated to version 0.5.1 and SEP-0010 matrix added showing 100% implementation coverage.

## What's Fixed

- Fixed Ktor HttpTimeout.INFINITE_TIMEOUT_MS deprecation in SSE streams
- Fixed SEP compatibility matrix formatting for better GitHub rendering

## Migration from 0.4.0

No code changes required. Simply update your dependency version:

```kotlin
dependencies {
    implementation("com.soneso.stellar:stellar-sdk:0.5.1")
}
```

**Dependency Note**: The Ktor 3.x upgrade is binary compatible. If you use Ktor in your application, consider upgrading to 3.x for performance benefits.

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
- **SEP-10**: Complete client-side authentication support (v3.4.1)

## Compatibility

Version 0.5.1 is fully compatible with 0.4.0. All APIs remain unchanged, with only new functionality added. The dependency upgrades are binary compatible with no breaking changes.

## Use Cases

SEP-10 enables key Stellar ecosystem integrations:

**For Application Developers**:
- Authenticate users with anchor services for deposits and withdrawals (SEP-6, SEP-24, SEP-31)
- Submit KYC information with verified account ownership (SEP-12)
- Access protected APIs requiring user authentication
- Implement multi-signature authentication workflows

**For Wallet Developers**:
- Prove wallet identity to receive premium service benefits
- Implement client domain signing with backend infrastructure
- Enable threshold account and multi-signature configurations

## Getting Help

If you encounter any issues:

1. Check the [documentation](https://github.com/Soneso/kmp-stellar-sdk)
2. Review the [SEP implementations guide](docs/sep/README.md)
3. Browse the [API reference](https://soneso.github.io/kmp-stellar-sdk/)
4. Explore the [demo app](demo/README.md)
5. Open an issue on [GitHub](https://github.com/Soneso/kmp-stellar-sdk/issues)

## Acknowledgments

This release enables secure authentication with Stellar anchors and services through production-ready SEP-10 implementation.

Built with Claude Code - AI-powered development assistant.

---

**Previous Release**: [Version 0.4.0](RELEASE_NOTES_0.4.0.md) - SEP-1 (Stellar TOML) and Documentation Improvements
