# Testing Guide

The SDK includes comprehensive test coverage across all supported platforms.

## Test Organization

Tests are organized into two categories under `commonTest`:

- **`unitTests/`** — Pure unit tests with no network dependencies. These run by default on all platforms.
- **`integrationTests/`** — Tests that validate against the live Stellar Testnet. Excluded by default and must be opted into explicitly.

## Running Unit Tests

By default, only unit tests run. Integration tests are excluded on all platforms.

### JVM

```bash
# All unit tests
./gradlew :stellar-sdk:jvmTest

# Specific test class
./gradlew :stellar-sdk:jvmTest --tests "KeyPairTest"

# Pattern matching
./gradlew :stellar-sdk:jvmTest --tests "*Key*"
```

### JavaScript (Node.js)

```bash
# All unit tests
./gradlew :stellar-sdk:jsNodeTest

# Specific test class
./gradlew :stellar-sdk:jsNodeTest --tests "KeyPairTest"

# Pattern matching
./gradlew :stellar-sdk:jsNodeTest --tests "*Key*"
```

### JavaScript (Browser)

```bash
# Specific test class (requires Chrome)
./gradlew :stellar-sdk:jsBrowserTest --tests "KeyPairTest"
```

### Native (macOS)

```bash
./gradlew :stellar-sdk:macosArm64Test
./gradlew :stellar-sdk:macosX64Test
```

## Integration Tests

Integration tests run by default in local/IDE environments. They require network access to the Stellar Testnet. In CI, they are excluded via `-PexcludeIntegrationTests`.

```bash
# Run all tests including integration tests (default)
./gradlew :stellar-sdk:jvmTest

# Specific integration test class
./gradlew :stellar-sdk:jvmTest --tests "*SorobanIntegrationTest"

# Exclude integration tests (used in CI)
./gradlew :stellar-sdk:jvmTest -PexcludeIntegrationTests
```

Test accounts are automatically funded via Friendbot. Requires connectivity to `https://soroban-testnet.stellar.org`.

## Code Coverage

Coverage is collected via [Kover](https://github.com/Kotlin/kotlinx-kover) on JVM tests:

```bash
# Generate HTML and XML coverage reports
./gradlew :stellar-sdk:koverHtmlReport :stellar-sdk:koverXmlReport
```

Reports are generated at:
- HTML: `stellar-sdk/build/reports/kover/html/index.html`
- XML: `stellar-sdk/build/reports/kover/report.xml`

Coverage is uploaded to [Codecov](https://codecov.io) automatically in CI (requires `CODECOV_TOKEN` secret).

## CI Workflow

The GitHub Actions workflow (`.github/workflows/tests.yml`) runs JVM unit tests across JDK 17, 21, and 25. Coverage is collected on JDK 25 and uploaded to Codecov.
