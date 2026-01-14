# Release Notes: Version 0.8.0

**Release Date**: January 14, 2026
**Status**: Released

## Overview

Version 0.8.0 adds SEP-24 (Hosted Deposit and Withdrawal) support, enabling applications to integrate with anchors for interactive deposit and withdrawal flows using a hosted web interface.

## What's New

### SEP-24: Hosted Deposit and Withdrawal

Production-ready client for interactive anchor transfers.

**Key Features**:
- **Seven API endpoints** for complete deposit/withdrawal workflow
- **Service discovery** via stellar.toml (TRANSFER_SERVER_SEP0024)
- **SEP-10 authentication** for all endpoints
- **SEP-38 quote integration** for cross-asset transfers
- **Transaction polling** with configurable intervals and callbacks
- **16 transaction statuses** including terminal states
- **5 exception types** for granular error handling

**API Endpoints**:
- `info()` - Discover anchor capabilities and supported assets
- `deposit()` - Initiate interactive deposit flow
- `withdraw()` - Initiate interactive withdrawal flow
- `fee()` - Query deposit/withdrawal fees (deprecated endpoint)
- `transactions()` - Retrieve transaction history
- `transaction()` - Get single transaction details
- `pollTransaction()` - Poll transaction until terminal status

**Usage Example**:
```kotlin
// Initialize from domain
val sep24Service = Sep24Service.fromDomain("testanchor.stellar.org")

// Get SEP-10 authentication token
val webAuth = WebAuth.fromDomain("testanchor.stellar.org", Network.TESTNET)
val jwtToken = webAuth.jwtToken(
    clientAccountId = userKeyPair.getAccountId(),
    signers = listOf(userKeyPair)
).token

// Initiate interactive deposit
val depositResponse = sep24Service.deposit(
    Sep24DepositRequest(
        assetCode = "USDC",
        jwt = jwtToken,
        account = userKeyPair.getAccountId(),
        amount = "100"
    )
)

// Open the interactive URL in a browser
println("Open: ${depositResponse.url}")
println("Transaction ID: ${depositResponse.id}")

// Poll for transaction completion
sep24Service.pollTransaction(
    request = Sep24TransactionRequest(id = depositResponse.id),
    jwt = jwtToken,
    pollIntervalMs = 5000,
    maxPolls = 60
) { transaction ->
    println("Status: ${transaction.status}")
    if (transaction.status == Sep24TransactionStatus.COMPLETED) {
        println("Deposit completed! Stellar TX: ${transaction.stellarTransactionId}")
    }
}
```

**Data Models**:
- `Sep24InfoResponse` - Service information with supported assets
- `Sep24AssetInfo` - Asset configuration (min/max amounts, fees)
- `Sep24InteractiveResponse` - Interactive URL and transaction ID
- `Sep24Transaction` - Full transaction details with status
- `Sep24FeeDetails`, `Sep24FeeDetail` - Fee breakdown
- `Sep24Refunds`, `Sep24RefundPayment` - Refund information

**Transaction Statuses**:
- Pending: `incomplete`, `pending_user_transfer_start`, `pending_user_transfer_complete`, `pending_external`, `pending_anchor`, `pending_stellar`, `pending_trust`, `pending_user`, `on_hold`
- Terminal: `completed`, `refunded`, `expired`, `error`, `no_market`, `too_small`, `too_large`

**Exception Types**:
- `Sep24AuthenticationRequiredException` - JWT token required (403)
- `Sep24InvalidRequestException` - Invalid request parameters (400)
- `Sep24ServerErrorException` - Server-side errors (500+)
- `Sep24TransactionNotFoundException` - Transaction not found (404)
- `Sep24Exception` - Base exception for general errors

**Testing**: 38 unit tests + 9 integration tests against live testnet

**Documentation**: See `docs/sep/sep-24.md` for usage examples

**Specification**: [SEP-24: Hosted Deposit and Withdrawal](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0024.md)

## Platform Support

All platforms supported:
- **JVM**: Android API 24+, Server applications (Java 11+)
- **iOS**: iOS 14.0+ (iosX64, iosArm64, iosSimulatorArm64)
- **macOS**: macOS 11.0+ (macosX64, macosArm64)
- **JavaScript**: Browser and Node.js 14+

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.soneso.stellar:stellar-sdk:0.8.0")
}
```

## Migration Guide

No breaking changes. SEP-24 is a new feature addition.

## Known Issues

None.

## Compatibility

- SEP-24 compatibility matrix: 100% coverage (128/128 fields)
- Full backward compatibility with 0.7.0
