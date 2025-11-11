# Stellar KMP SDK Documentation

Welcome to the comprehensive documentation for the Stellar SDK for Kotlin Multiplatform. This documentation covers everything from getting started to advanced implementation details.

**This SDK is in active development (v0.3.0). While production-ready cryptography and comprehensive testing are in place, the API may evolve before the 1.0 release. Use with appropriate testing and caution in production environments.**

## Documentation Structure

### Quick Start
- **[Quick Start Guide](quick-start.md)** - Get running in 30 minutes
- **[Getting Started Guide](getting-started.md)** - Platform details and best practices
- **[SDK Usage Examples](sdk-usage-examples.md)** - Complete SDK documentation with examples

### Architecture & Design
- **[Architecture Guide](architecture.md)** - System architecture, design decisions, and security principles
- **[Platform Guides](platforms/)** - Platform-specific setup and considerations
  - [JVM/Android](platforms/jvm.md)
  - [JavaScript/Web](platforms/javascript.md)
  - [iOS](platforms/ios.md)
  - [macOS](platforms/macos.md)

### Development
- **[Demo App Guide](demo-app.md)** - Learn from complete example application

### Advanced Topics
- **[Advanced Topics](advanced.md)** - Hardware wallets, multi-sig, performance optimization, advanced network operations

## Key Features

This SDK provides a complete implementation of the Stellar protocol with:

- **Cross-platform support** - JVM, JavaScript, iOS, macOS
- **Production-ready cryptography** - Audited libraries on all platforms
- **Comprehensive API coverage** - Horizon, Soroban RPC, and more
- **Type-safe APIs** - Leverage Kotlin's type system
- **Async/await support** - Modern coroutines-based API
- **Smart contract interaction** - Full Soroban support

## Quick Links

### By Use Case

**Building Transactions**
- [Transaction Building](sdk-usage-examples.md#transaction-building)
- [Operation Types](sdk-usage-examples.md#operations)
- [Signing Transactions](sdk-usage-examples.md#signing-transactions)

**Account Management**
- [KeyPair Generation](sdk-usage-examples.md#keypair)
- [Account Creation](getting-started.md#creating-accounts)
- [Multi-signature Accounts](advanced.md#multi-signature-accounts)

**Smart Contracts**
- [ContractClient](sdk-usage-examples.md#contractclient)
- [AssembledTransaction](sdk-usage-examples.md#assembledtransaction)
- [Soroban Authorization](sdk-usage-examples.md#auth)

**Network Communication**
- [Horizon API](sdk-usage-examples.md#horizon-server)
- [Soroban RPC](sdk-usage-examples.md#soroban-server)
- [Server-Sent Events](sdk-usage-examples.md#server-sent-events)

### By Platform

**Mobile Development**
- [Android Setup](platforms/jvm.md#android-setup)
- [iOS Setup](platforms/ios.md)
- [macOS Setup](platforms/macos.md)
- [Demo Android App](demo-app.md#android)
- [Demo iOS App](demo-app.md#ios)
- [Demo macOS App](demo-app.md#macos-native)

**Desktop Development**
- [JVM/Desktop Setup](platforms/jvm.md)
- [Demo Desktop App](demo-app.md#desktop-jvm)

**Web Development**
- [Browser Setup](platforms/javascript.md#browser-setup)
- [Node.js Setup](platforms/javascript.md#nodejs-setup)
- [Demo Web App](demo-app.md#web-javascript)

**Server Development**
- [JVM Server Setup](platforms/jvm.md#server-setup)
- [Transaction Submission](sdk-usage-examples.md#submitting-transactions)

## Current Implementation Status

### Core Features
- Ed25519 keypair generation and management
- StrKey encoding/decoding (G..., S..., C..., M... addresses)
- Transaction building and signing
- Fee bump transactions
- Multi-signature support
- All 27 Stellar operations
- Complete XDR serialization/deserialization

### Horizon API
- Accounts, Assets, Claimable Balances
- Ledgers, Liquidity Pools, Offers
- Operations, Payments, Trades
- Order Books, Path Payments
- Fee Statistics, Health Monitoring
- Server-Sent Events (SSE) streaming

### Soroban RPC API
- Transaction simulation and resource estimation
- Contract invocation and deployment
- Event queries and filtering
- Ledger entries and contract data retrieval
- Network information and versioning
- Health monitoring and status checks

### Soroban (Smart Contracts)
- ContractClient for easy interaction
- AssembledTransaction lifecycle management
- Authorization handling
- State restoration
- Transaction simulation
- Result parsing with type safety

### Security Features
- Production-ready crypto libraries
- Constant-time operations
- Memory-safe implementations
- Comprehensive input validation
- Network replay protection

## Getting Help

- **[GitHub Issues](https://github.com/Soneso/kmp-stellar-sdk/issues)** - Bug reports and feature requests
- **[Stellar Developers Discord](https://discord.gg/stellardev)** - Community support

## Contributing

See the main [README.md](../README.md) for contribution guidelines.

## License

This SDK is licensed under the Apache License 2.0. See [LICENSE](../LICENSE) for details.

---

**Navigation**: [Quick Start →](quick-start.md)