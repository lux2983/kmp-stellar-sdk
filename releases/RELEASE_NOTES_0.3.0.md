# Release Notes: Version 0.3.0

**Release Date**: November 8, 2025
**Status**: Released

## Overview

Version 0.3.0 is a minor release that enhances the SDK with improved Horizon API coverage, complete type conversion support, and a modernized demo application. This release includes new features, bug fixes, and documentation improvements with no breaking changes.

## What's New

### Enhanced Horizon API Coverage

The Horizon client now includes all missing parameters to fully match the official Stellar Horizon API specification, providing complete API coverage for all supported endpoints.

### Complete Type Conversion System

- **scValToNative**: Now supports all Soroban types including error handling and contract instances
- **SCSpecType Support**: Complete implementation in ContractSpec for comprehensive type parsing and validation

### Modernized Demo Application

The demo app has received a major UI overhaul:

- **New Feature**: Invoke Token Contract demo showcasing token interactions
- **New Feature**: Info screen with centralized version display and auto-scroll functionality
- **UI Redesign**: Complete visual refresh with modern celestial-themed design
- **App Icons**: Comprehensive icon sets for all platforms (Android, iOS, macOS, Desktop, Web)
- **Enhanced Documentation**: Updated guides for all 11 demo features

## What's Fixed

### Web Application

- Fixed WASM loading issues in production deployments, ensuring reliable web app performance

### Soroban RPC

- Corrected getEvents RPC implementation to match the official Stellar RPC specification

## What's Changed

### Documentation

All documentation has been updated for improved accuracy and conciseness across platform guides and getting started materials.

## Migration from 0.2.1

No code changes are required. Simply update your dependency version:

```kotlin
dependencies {
    implementation("com.soneso.stellar:stellar-sdk:0.3.0")
}
```

## Platform Support

No changes to platform support:
- JVM (Android API 24+, Server Java 11+)
- iOS 14.0+
- macOS 11.0+
- JavaScript (Browser and Node.js 14+)

## Compatibility

Version 0.3.0 is fully compatible with 0.2.1. All APIs remain unchanged.

## Getting Help

If you encounter any issues:

1. Check the [documentation](https://github.com/Soneso/kmp-stellar-sdk)
2. Review the [Getting Started Guide](docs/getting-started.md)
3. Explore the [demo app](demo/README.md)
4. Open an issue on [GitHub](https://github.com/Soneso/kmp-stellar-sdk/issues)

## Acknowledgments

This release improves SDK functionality and developer experience through enhanced API coverage, complete type support, and a modernized demo application.

Built with Claude Code - AI-powered development assistant.

---

**Previous Release**: [Version 0.2.1](RELEASE_NOTES_0.2.1.md) - Maven Publishing and Documentation Fixes
