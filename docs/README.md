# Stellar KMP SDK Documentation

Welcome to the documentation for the Stellar SDK for Kotlin Multiplatform. This documentation covers everything from getting started to advanced implementation details.

## Documentation Structure

### Getting Started
- **[Quick Start Guide](quick-start.md)** - Get running in 30 minutes
- **[Getting Started Guide](getting-started.md)** - Guide to SDK fundamentals

### Learning & Examples
- **[SDK Usage Examples](sdk-usage-examples.md)** - Code examples and patterns
- **[Demo App Guide](demo-app.md)** - Learn from example application

### Architecture & Design
- **[Architecture Guide](architecture.md)** - System architecture, design decisions, and security principles
- **[Platform Guides](platforms/)** - Platform-specific setup and considerations

### Advanced Topics
- **[Advanced Topics](advanced.md)** - Complex patterns and optimizations
- **[Testing Guide](testing.md)** - SDK testing (for contributors)

## Key Features

This SDK provides an implementation of the Stellar protocol with:

- **Cross-platform support** - JVM (Android/Desktop), JavaScript (Browser/Node.js), iOS, macOS
- **Production-ready cryptography** - Audited libraries on all platforms
- **Full API coverage** - Horizon, Soroban RPC, and more
- **Type-safe APIs** - Leverage Kotlin's type system
- **Async/await support** - Modern coroutines-based API
- **Smart contract interaction** - Full Soroban support

## SDK Capabilities

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
- Extensive input validation
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