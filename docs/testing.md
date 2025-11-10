# Testing Guide

The SDK includes comprehensive test coverage across all supported platforms.

## Running All Tests

```bash
./gradlew test
```

## Platform-Specific Tests

### JVM Tests

```bash
# All JVM tests
./gradlew :stellar-sdk:jvmTest

# Specific test class
./gradlew :stellar-sdk:jvmTest --tests "KeyPairTest"

# Pattern matching
./gradlew :stellar-sdk:jvmTest --tests "*Key*"
```

### JavaScript Tests

**Node.js:**
```bash
# Specific test class
./gradlew :stellar-sdk:jsNodeTest --tests "KeyPairTest"

# Pattern matching
./gradlew :stellar-sdk:jsNodeTest --tests "*Key*"
```

**Browser:**
```bash
# Specific test class (requires Chrome)
./gradlew :stellar-sdk:jsBrowserTest --tests "KeyPairTest"

# Pattern matching
./gradlew :stellar-sdk:jsBrowserTest --tests "*Key*"
```

Note: Running all JavaScript tests together is not supported due to Kotlin/JS bundling limitations. Use test filtering to run specific test classes or patterns.

### Native Tests

**macOS:**
```bash
./gradlew :stellar-sdk:macosArm64Test
./gradlew :stellar-sdk:macosX64Test
```

**iOS Simulator:**
```bash
./gradlew :stellar-sdk:iosSimulatorArm64Test
./gradlew :stellar-sdk:iosX64Test
```

## Integration Tests

Integration tests validate against live Stellar Testnet:

```bash
./gradlew :stellar-sdk:jvmTest --tests "ContractClientIntegrationTest"
```

Test accounts are automatically funded via Friendbot. Requires testnet connectivity to `https://soroban-testnet.stellar.org`.
