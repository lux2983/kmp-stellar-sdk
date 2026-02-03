# Bug Report

## Pre-existing Test Compilation Errors

### 1. PriceTest.kt uses wrong property names
- **File:** `stellar-sdk/src/commonTest/kotlin/com/soneso/stellar/sdk/unitTests/PriceTest.kt`
- **Issue:** Test references `price.n` and `price.d` but the SDK `Price` class (`com.soneso.stellar.sdk.Price`) defines `numerator` and `denominator` as property names.
- **Error:** `Unresolved reference 'n'` / `Unresolved reference 'd'` (14 occurrences)
- **Fix:** Replace all `.n` references with `.numerator` and all `.d` references with `.denominator`

### 2. ClientOptionsTest.kt calls suspend function outside coroutine
- **File:** `stellar-sdk/src/commonTest/kotlin/com/soneso/stellar/sdk/unitTests/contract/ClientOptionsTest.kt`
- **Issue:** Calls `KeyPair.random()` (which is a suspend function) from non-suspend test functions.
- **Error:** `Suspend function 'suspend fun random(): KeyPair' can only be called from a coroutine or another suspend function` (lines 21, 38, 61, 79, 94)
- **Fix:** Wrap test bodies in `runTest { }` or `runBlocking { }`

### 3. XdrExtensionsTest.kt type mismatch errors
- **File:** `stellar-sdk/src/commonTest/kotlin/com/soneso/stellar/sdk/unitTests/xdr/XdrExtensionsTest.kt`
- **Issue:** Several test assertions pass `ByteArray` where `ContractIDXdr`, `HashXdr`, or `ValueXdr` are expected (and vice versa). Appears the XDR types changed from typealias to wrapper classes.
- **Errors:** Lines 233, 234, 237, 238, 245, 249 — "Argument type mismatch"
- **Fix:** Update test to use the new XDR wrapper types
