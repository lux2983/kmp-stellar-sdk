# Release Notes: Version 0.7.0

**Release Date**: December 17, 2025
**Status**: Released

## Overview

Version 0.7.0 adds SEP-38 (Anchor RFQ API) support, enabling applications to discover exchange rates and request firm quotes for exchanging on-chain Stellar assets with off-chain assets such as fiat currencies.

## What's New

### SEP-38: Anchor RFQ API

Production-ready client for anchor price quotes and exchange rate discovery.

**Key Features**:
- **Five API endpoints** for complete price discovery and quoting workflow
- **Service discovery** via stellar.toml (ANCHOR_QUOTE_SERVER)
- **SEP-10 authentication** for firm quotes
- **12 data model classes** for type-safe requests and responses
- **5 exception types** for granular error handling

**API Endpoints**:
- `info()` - Discover supported assets and delivery methods
- `prices()` - Get all available exchange prices for an asset
- `price()` - Get indicative price for a specific asset pair
- `postQuote()` - Request a firm quote with guaranteed rate
- `getQuote()` - Retrieve an existing quote by ID

**Usage Example**:
```kotlin
// Initialize from domain
val quoteService = QuoteService.fromDomain("testanchor.stellar.org")

// Discover supported assets
val info = quoteService.info()
info.assets.forEach { asset ->
    println("Asset: ${asset.asset}")
}

// Get indicative price (no authentication required)
val price = quoteService.price(
    context = "sep6",
    sellAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
    buyAsset = "iso4217:BRL",
    sellAmount = "100"
)
println("Rate: ${price.price}, Fee: ${price.fee.total}")

// Request firm quote (requires SEP-10 authentication)
val webAuth = WebAuth.fromDomain("testanchor.stellar.org", Network.TESTNET)
val jwtToken = webAuth.jwtToken(
    clientAccountId = userKeyPair.getAccountId(),
    signers = listOf(userKeyPair)
).token

val quote = quoteService.postQuote(
    Sep38QuoteRequest(
        context = "sep6",
        sellAsset = "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN",
        buyAsset = "iso4217:BRL",
        sellAmount = "100"
    ),
    jwtToken
)
println("Quote ID: ${quote.id}, Expires: ${quote.expiresAt}")
```

**Data Models**:
- `Sep38InfoResponse` - Service information with supported assets
- `Sep38PricesResponse` - List of available exchange prices
- `Sep38PriceResponse` - Single price with fee breakdown
- `Sep38QuoteRequest` - Request parameters for firm quotes
- `Sep38QuoteResponse` - Firm quote with expiration and guaranteed rate
- `Sep38Asset` - Asset information with delivery methods
- `Sep38BuyAsset`, `Sep38SellAsset` - Asset identifiers with pricing
- `Sep38DeliveryMethod` - Delivery method details (bank transfer, PIX, etc.)
- `Sep38Fee`, `Sep38FeeDetail` - Fee structure with individual components

**Exception Types**:
- `Sep38BadRequestException` - Invalid request parameters (400)
- `Sep38PermissionDeniedException` - Authentication failure (403)
- `Sep38NotFoundException` - Quote not found (404)
- `Sep38UnknownResponseException` - Unexpected response codes
- `Sep38Exception` - Base exception for general errors

**Testing**: 48 unit tests + 9 integration tests against live testnet

**Documentation**: See `docs/sep/sep-38.md` for usage examples

**Specification**: [SEP-38: Anchor RFQ API](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0038.md)

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
    implementation("com.soneso.stellar:stellar-sdk:0.7.0")
}
```

## Migration Guide

No breaking changes. SEP-38 is a new feature addition.

## Known Issues

None.

## Compatibility

- SEP-38 compatibility matrix: 100% coverage (63/63 fields)
- Full backward compatibility with 0.6.0
