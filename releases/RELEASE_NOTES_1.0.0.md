# Release Notes - Version 1.0.0

## Overview

Version 1.0.0 marks the first stable release of the Stellar SDK for Kotlin Multiplatform. This release adds SEP-45 (Web Authentication for Contract Accounts) support, completing the SDK's authentication capabilities for both traditional Stellar accounts (SEP-10) and Soroban smart contract accounts (SEP-45).

## What's New

### SEP-45 Web Authentication for Contract Accounts

Production-ready client for authenticating Soroban smart contract accounts (C... addresses) using a challenge-response protocol with Soroban authorization entry signing.

**WebAuthForContracts** provides the complete authentication flow:
- `fromDomain()` - Initialize from stellar.toml SEP-45 configuration
- `jwtToken()` - Complete authentication flow and receive JWT token
- `getChallenge()` - Request authorization entries from server
- `validateChallenge()` - Validate server challenge (13 security checks)
- `signAuthorizationEntries()` - Sign entries with keypair(s)
- `sendSignedChallenge()` - Submit signed entries for JWT
- `decodeAuthorizationEntries()` / `encodeAuthorizationEntries()` - XDR utilities

**Sep45AuthToken** provides JWT parsing:
- Extracts `account`, `issuedAt`, `expiresAt`, `issuer`, `clientDomain`
- `isExpired()` method for token validation
- `token` property for use with SEP-6, SEP-12, SEP-24 services

**Client Domain Signing:**
- `Sep45ClientDomainSigningDelegate` interface for remote signing
- String-based API (base64 XDR) optimized for HTTP remote signing servers

**Error Handling:**
- 22 exception types with sealed hierarchy
- `Sep45ChallengeValidationException` sealed class with 12 specific validation exceptions
- Request/response exceptions for HTTP errors

**Security:**
- 13 security validation checks matching SEP-45 specification
- Server signature verification using authorization preimage hash
- Multi-signature support for contract authentication

## Testing

- 161 unit tests with MockEngine
- 2 integration tests against live testnet (basic flow + client domain signing)
- 100% feature coverage (35/35 features)

## Documentation

- User guide: `docs/sep/sep-45.md`
- Compatibility matrix: `compatibility/sep/SEP-0045_COMPATIBILITY_MATRIX.md`
- Updated SEP README with SEP-45 entry

## Platform Support

All platforms fully supported:
- JVM (Android API 24+, Server Java 11+)
- iOS (iOS 14.0+)
- macOS (macOS 11.0+)
- JavaScript (Browser and Node.js 14+)

## SDK Features

Version 1.0.0 includes complete Stellar functionality:
- Cryptography (Ed25519 keypairs, signing, verification)
- Transaction building (all 27 operations, multi-signature)
- Horizon API client (full REST API, SSE streaming)
- Soroban RPC client (contract calls, simulation, state restoration)
- High-level ContractClient API with AssembledTransaction
- SEP Support: SEP-1, SEP-6, SEP-9, SEP-10, SEP-12, SEP-24, SEP-38, SEP-45

## Migration

No breaking changes from 0.9.0. SEP-45 is an additive feature.

## Dependencies

No dependency changes from 0.9.0.

---

**Full Changelog**: https://github.com/Soneso/kmp-stellar-sdk/compare/v0.9.0...v1.0.0
