# Add test coverage infrastructure, comprehensive unit tests, and cross-platform bug fixes

### Summary

Adds code coverage tooling, 133 new unit test files (~3,984 tests across 5 platforms), cross-platform test infrastructure, and fixes production bugs in JS and native targets discovered during testing.

### Test Infrastructure
- **Test reorganization:** Split `commonTest` into `unitTests/` and `integrationTests/` directories
- **Kover coverage:** Added Kover plugin for JVM code coverage with HTML and XML reports
- **Codecov integration:** CI uploads coverage to Codecov (requires `CODECOV_TOKEN` repo secret); badge added to README
- **CI workflow expanded:** JVM tests across JDK 17/21/25 (push + PR), JS Node tests (push + PR), macOS native tests (PR only)
- **Integration test exclusion:** `-PexcludeIntegrationTests` flag to skip integration tests in CI across all platforms
- **Real wall-clock delays:** Added `platformDelay()` and `realDelay()` utilities for integration tests that need actual waiting (bypasses `runTest` virtual time)

### Unit Tests
- 133 new unit test files covering: crypto, StrKey, KeyPair, transactions, operations, assets, accounts, memos, Horizon request builders, Horizon response deserialization, all operation response types, all effect types, Soroban RPC, contract client, assembled transactions, SEP-1/6/9/10/12/24/38/45, XDR round-trips, and more
- Merged 16 redundant test files into their base test classes (zero coverage lost)
- ~3,984 unit tests passing on JVM, JS Node, JS Browser, macOS native, and iOS simulator

### SDK Bug Fixes
- **BigInteger two's complement (JS & Native):** `bigIntegerToBytesSigned()` used magnitude-only encoding instead of proper two's complement — negative Int128/Int256 values were silently corrupted on JS and native targets. JVM was unaffected.
- **Native empty data crypto:** SHA-256 of empty data crashed (invalid assertion); Ed25519 sign/verify of empty data crashed (`addressOf(0)` on empty ByteArray)
- **SorobanServer.pollTransaction():** Added `withContext(Dispatchers.Default)` for real wall-clock delay during polling, fixing too-fast polling on JS and native

### Test Fixes
- **JS WASM resource loading:** Fixed path resolution for test resources in both Node.js and Browser environments
- **JS Browser test bundling:** Fixed Karma config injection and WASM file serving for webpack compatibility
- **Native test compilation:** Replaced JVM-only APIs (`System.currentTimeMillis()`, `String.format()`, `String.toByteArray()`) with multiplatform alternatives in test code
- **JS `@BeforeTest` + `runTest`:** Kotlin/JS doesn't await the Promise returned by `@BeforeTest`, causing uninitialized properties — replaced with explicit `suspend setup()` calls

### Build Changes
- Bumped `jvmTarget` from JVM 11 to JVM 17 (Android AGP 8.x and Gradle 8+ require 17)
- CI JDK matrix: 17, 21, 25 (three LTS versions)

### Documentation
- Updated `docs/testing.md` with all platforms, integration test timing, iOS simulator SSL limitation
- Updated `CLAUDE.md` files with current project state
