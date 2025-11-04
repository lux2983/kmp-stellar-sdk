package com.soneso.demo.stellar

import com.soneso.stellar.sdk.KeyPair
import com.soneso.stellar.sdk.Network
import com.soneso.stellar.sdk.contract.ContractClient
import com.soneso.stellar.sdk.xdr.SCValXdr
import com.soneso.stellar.sdk.xdr.SCSpecTypeDefXdr
import com.soneso.stellar.sdk.xdr.SCSpecTypeXdr
import com.soneso.demo.util.StellarValidation

/**
 * Dummy account ID used for read-only contract calls.
 * This is a well-formed Stellar address that is used only for simulation purposes.
 * Read-only calls don't require a real funded account since they don't submit transactions.
 */
private const val DUMMY_ACCOUNT_ID = "GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWHF"

/**
 * Result type for token contract operations.
 *
 * This sealed class represents the outcome of token contract interactions,
 * including contract loading, function invocation, and validation.
 */
sealed class InvokeTokenResult {
    /**
     * Successfully loaded contract with token interface validation.
     *
     * @property contractName The name of the token contract
     * @property contractSymbol The symbol of the token
     * @property functions List of available functions from the contract spec
     * @property client The ContractClient instance for subsequent invocations
     * @property currentLedger The current ledger sequence number at time of loading
     */
    data class ContractLoaded(
        val contractName: String,
        val contractSymbol: String,
        val functions: List<ContractFunctionInfo>,
        val client: ContractClient,
        val currentLedger: Long
    ) : InvokeTokenResult()

    /**
     * Successfully invoked a token contract function.
     *
     * @property functionName The function that was invoked
     * @property result The parsed result (native Kotlin type or formatted string)
     * @property transactionHash The transaction hash (null for read-only calls)
     * @property isReadOnly Whether this was a read-only call
     */
    data class InvocationSuccess(
        val functionName: String,
        val result: String,
        val transactionHash: String?,
        val isReadOnly: Boolean
    ) : InvokeTokenResult()

    /**
     * Additional signers are required to complete the transaction.
     * This happens when simulation reveals that accounts other than the source need to sign.
     *
     * @property functionName The function being invoked
     * @property requiredSigners List of account IDs that need to provide signatures
     * @property message User-friendly message explaining what's needed
     */
    data class NeedsAdditionalSigners(
        val functionName: String,
        val requiredSigners: List<String>,
        val message: String
    ) : InvokeTokenResult()

    /**
     * Failed operation with error details.
     *
     * @property message Human-readable error message
     * @property exception The underlying exception, if any
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : InvokeTokenResult()
}

/**
 * Information about a contract function extracted from the contract specification.
 *
 * @property name The function name
 * @property description Human-readable description of the function
 * @property parameters List of parameters with their names and types
 * @property isReadOnly Whether the function is read-only (no authorization required)
 */
data class ContractFunctionInfo(
    val name: String,
    val description: String,
    val parameters: List<ParameterInfo>,
    val isReadOnly: Boolean
)

/**
 * Information about a function parameter.
 *
 * @property name The parameter name
 * @property typeName A human-readable type name (e.g., "Address", "i128", "String")
 */
data class ParameterInfo(
    val name: String,
    val typeName: String
)

/**
 * Specification for a parameter that may have multiple valid alternatives across protocol versions.
 *
 * @property names List of acceptable parameter names (e.g., ["live_until_ledger", "expiration_ledger"])
 * @property types List of acceptable parameter types (e.g., ["Address", "MuxedAddress"])
 */
data class ParameterAlternatives(
    val names: List<String>,
    val types: List<String>
) {
    constructor(name: String, type: String) : this(listOf(name), listOf(type))
}

/**
 * Expected function signature for token interface validation.
 *
 * Supports protocol version variations where parameters may have different names or types
 * across protocol versions (e.g., pre-protocol 23 vs protocol 23+).
 *
 * @property name Function name
 * @property parameters List of expected parameter alternatives (each parameter can have multiple valid forms)
 * @property returnType Expected return type (null for void functions)
 */
data class TokenInterfaceFunction(
    val name: String,
    val parameters: List<ParameterAlternatives>,
    val returnType: String? // null for void returns
) {
    companion object {
        /**
         * Helper to create a function with simple (non-alternative) parameters.
         * Used for functions that haven't changed across protocol versions.
         */
        fun simple(name: String, parameters: List<Pair<String, String>>, returnType: String?): TokenInterfaceFunction {
            return TokenInterfaceFunction(
                name = name,
                parameters = parameters.map { (paramName, paramType) ->
                    ParameterAlternatives(paramName, paramType)
                },
                returnType = returnType
            )
        }
    }
}

/**
 * Standard Stellar token interface functions as per SEP-41.
 *
 * This defines the standard functions that all token contracts must implement.
 * ALL 10 functions are REQUIRED - there are no optional functions in the standard interface.
 * Used for validation, UI display, and ordering.
 *
 * ## Protocol Version Compatibility
 *
 * Some functions have parameter variations across protocol versions:
 *
 * ### approve function (parameter 3):
 * - **NEW (protocol 23+)**: `live_until_ledger: u32`
 * - **OLD (pre-protocol 23)**: `expiration_ledger: u32`
 * - Both are accepted to support contracts deployed on different protocol versions
 *
 * ### transfer function (parameter 1):
 * - **NEW (protocol 23+)**: `to_muxed: MuxedAddress` (supports multiplexed addresses)
 * - **OLD (pre-protocol 23)**: `to: Address` (basic addresses only)
 * - Both are accepted to ensure backward compatibility
 */
object TokenInterface {
    /**
     * ALL 10 standard token interface functions are REQUIRED.
     * These are checked with strict parameter and return type validation.
     *
     * Standard Token Interface (SEP-41):
     * - allowance(from: Address, spender: Address) -> i128
     * - approve(from: Address, spender: Address, amount: i128, live_until_ledger|expiration_ledger: u32) -> ()
     * - balance(id: Address) -> i128
     * - transfer(from: Address, to|to_muxed: Address|MuxedAddress, amount: i128) -> ()
     * - transfer_from(spender: Address, from: Address, to: Address, amount: i128) -> ()
     * - burn(from: Address, amount: i128) -> ()
     * - burn_from(spender: Address, from: Address, amount: i128) -> ()
     * - decimals() -> u32
     * - name() -> String
     * - symbol() -> String
     *
     * Note: Types must match EXACTLY as defined in the contract spec (case-insensitive).
     * Void returns are represented as null in returnType.
     */
    val REQUIRED_FUNCTIONS = listOf(
        TokenInterfaceFunction.simple("allowance", listOf("from" to "Address", "spender" to "Address"), "i128"),

        // approve: Parameter 3 supports both protocol versions
        TokenInterfaceFunction(
            name = "approve",
            parameters = listOf(
                ParameterAlternatives("from", "Address"),
                ParameterAlternatives("spender", "Address"),
                ParameterAlternatives("amount", "i128"),
                // Protocol version alternatives: NEW (protocol 23+) OR OLD (pre-protocol 23)
                ParameterAlternatives(
                    names = listOf("live_until_ledger", "expiration_ledger"),
                    types = listOf("u32") // Type is same for both versions
                )
            ),
            returnType = null
        ),

        TokenInterfaceFunction.simple("balance", listOf("id" to "Address"), "i128"),

        // transfer: Parameter 1 supports both protocol versions
        TokenInterfaceFunction(
            name = "transfer",
            parameters = listOf(
                ParameterAlternatives("from", "Address"),
                // Protocol version alternatives: NEW (protocol 23+) OR OLD (pre-protocol 23)
                ParameterAlternatives(
                    names = listOf("to", "to_muxed"),
                    types = listOf("Address", "MuxedAddress")
                ),
                ParameterAlternatives("amount", "i128")
            ),
            returnType = null
        ),

        TokenInterfaceFunction.simple("transfer_from", listOf("spender" to "Address", "from" to "Address", "to" to "Address", "amount" to "i128"), null),
        TokenInterfaceFunction.simple("burn", listOf("from" to "Address", "amount" to "i128"), null),
        TokenInterfaceFunction.simple("burn_from", listOf("spender" to "Address", "from" to "Address", "amount" to "i128"), null),
        TokenInterfaceFunction.simple("decimals", emptyList(), "u32"),
        TokenInterfaceFunction.simple("name", emptyList(), "String"),
        TokenInterfaceFunction.simple("symbol", emptyList(), "String")
    )

    /**
     * All standard token functions.
     */
    val ALL_FUNCTIONS_SET = REQUIRED_FUNCTIONS.map { it.name }.toSet()

    /**
     * Standard function order from Stellar Token Interface (SEP-41).
     * Functions appear in this order in the UI, followed by any custom functions.
     */
    val STANDARD_FUNCTION_ORDER = listOf(
        "allowance",
        "approve",
        "balance",
        "transfer",
        "transfer_from",
        "burn",
        "burn_from",
        "decimals",
        "name",
        "symbol"
    )

    /**
     * Read-only functions that don't require authorization.
     */
    val READ_ONLY_FUNCTIONS = setOf(
        "allowance",
        "balance",
        "decimals",
        "name",
        "symbol"
    )

    /**
     * Hardcoded descriptions for standard token interface functions from SEP-41.
     * Used as fallback when contract spec doesn't provide descriptions.
     */
    val FUNCTION_DESCRIPTIONS = mapOf(
        "allowance" to "Returns the allowance for `spender` to transfer from `from`",
        "approve" to "Set the allowance by `amount` for `spender` to transfer/burn from `from`",
        "balance" to "Returns the balance of `id`",
        "transfer" to "Transfer `amount` from `from` to `to`",
        "transfer_from" to "Transfer `amount` from `from` to `to`, consuming the allowance that `spender` has on `from`'s balance",
        "burn" to "Burn `amount` from `from`",
        "burn_from" to "Burn `amount` from `from`, consuming the allowance of `spender`",
        "decimals" to "Returns the number of decimals used to represent amounts of this token",
        "name" to "Returns the name for this token",
        "symbol" to "Returns the symbol for this token"
    )
}

/**
 * Determines which accounts need to sign for a token contract function based on hardcoded knowledge
 * of the standard token interface (SEP-41).
 *
 * For standard functions, we can predict which accounts need to sign based on the function
 * parameters. For custom functions, this returns empty (dynamic discovery will be used).
 *
 * This is a **business logic heuristic** - it provides upfront guidance to users but is not
 * authoritative. The actual signing requirements are determined by `needsNonInvokerSigningBy()`
 * after simulation.
 *
 * @param functionName The contract function name
 * @param args The function arguments (parameter name → value)
 * @return List of pairs: parameter name to account ID (nullable). If account ID is null,
 *         parameter is not yet filled. Empty list means no signing expected or unknown function.
 */
fun getExpectedSigners(functionName: String, args: Map<String, String>): List<Pair<String, String?>> {
    return when (functionName) {
        // Transfer: 'from' account must sign
        "transfer" -> listOf("from" to args["from"])

        // Approve: 'from' account must sign
        "approve" -> listOf("from" to args["from"])

        // Transfer from: 'spender' account must sign
        "transfer_from" -> listOf("spender" to args["spender"])

        // Burn: 'from' account must sign
        "burn" -> listOf("from" to args["from"])

        // Burn from: 'spender' account must sign
        "burn_from" -> listOf("spender" to args["spender"])

        // Read-only functions: no signing needed
        "balance", "allowance", "decimals", "name", "symbol" -> emptyList()

        // Unknown/custom functions: use dynamic discovery
        else -> emptyList()
    }
}

/**
 * Validates that a contract function matches the expected token interface signature.
 *
 * This performs comprehensive validation including:
 * - Parameter count verification
 * - Parameter name matching (supports alternatives for protocol version compatibility)
 * - Parameter type matching (supports alternatives for protocol version compatibility)
 * - Return type matching (case-insensitive, null = void)
 *
 * ## Protocol Version Compatibility
 *
 * Some parameters accept multiple valid names/types to support different protocol versions:
 * - `approve` parameter 3: Accepts "live_until_ledger" (protocol 23+) OR "expiration_ledger" (pre-protocol 23)
 * - `transfer` parameter 1: Accepts "to_muxed: MuxedAddress" (protocol 23+) OR "to: Address" (pre-protocol 23)
 *
 * @param contractFunc The function from the loaded contract
 * @param expected The expected function signature from TokenInterface (with alternatives)
 * @param contractReturnType The return type from the contract spec (null for void)
 * @return null if valid, error message if validation fails
 */
private fun validateFunctionSignature(
    contractFunc: ContractFunctionInfo,
    expected: TokenInterfaceFunction,
    contractReturnType: String?
): String? {
    // Check parameter count
    if (contractFunc.parameters.size != expected.parameters.size) {
        return "Function '${expected.name}' has wrong number of parameters. Expected ${expected.parameters.size}, got ${contractFunc.parameters.size}"
    }

    // Check each parameter (supports alternatives for protocol version compatibility)
    contractFunc.parameters.forEachIndexed { index, param ->
        val expectedAlternatives = expected.parameters[index]

        // Check if parameter name matches ANY of the allowed alternatives (case-insensitive)
        val nameMatches = expectedAlternatives.names.any { expectedName ->
            param.name.lowercase() == expectedName.lowercase()
        }

        if (!nameMatches) {
            val expectedNamesStr = expectedAlternatives.names.joinToString(" OR ")
            return "Function '${expected.name}' parameter $index: expected name '$expectedNamesStr', got '${param.name}'"
        }

        // Check if parameter type matches ANY of the allowed alternatives (case-insensitive)
        val typeMatches = expectedAlternatives.types.any { expectedType ->
            param.typeName.lowercase() == expectedType.lowercase()
        }

        if (!typeMatches) {
            val expectedTypesStr = expectedAlternatives.types.joinToString(" OR ")
            return "Function '${expected.name}' parameter '${param.name}': expected type '$expectedTypesStr', got '${param.typeName}'"
        }
    }

    // Check return type (case-insensitive)
    val expectedReturn = expected.returnType?.lowercase()
    val actualReturn = contractReturnType?.lowercase()

    // Both null = void, both non-null = must match
    when {
        expectedReturn == null && actualReturn == null -> {
            // Both void - OK
        }
        expectedReturn == null && actualReturn != null -> {
            return "Function '${expected.name}' return type: expected void, got '$contractReturnType'"
        }
        expectedReturn != null && actualReturn == null -> {
            return "Function '${expected.name}' return type: expected '$expected.returnType', got void"
        }
        expectedReturn != actualReturn -> {
            return "Function '${expected.name}' return type: expected '$expected.returnType', got '$contractReturnType'"
        }
    }

    return null
}

/**
 * Loads a token contract from the network and validates that it implements the standard token interface.
 *
 * This function demonstrates:
 * - Loading contract specifications from the network
 * - Comprehensive token interface validation (SEP-41)
 * - Function signature validation (names, types, parameters, return types)
 * - Protocol version compatibility (supports both pre-protocol 23 and protocol 23+ contracts)
 * - Parsing contract metadata (name, symbol)
 * - Extracting function signatures from contract spec
 * - Filtering out constructor functions
 * - Sorting functions by standard token interface order
 * - Fetching current ledger information for expiration parameters
 *
 * ## Token Interface Validation
 *
 * The function performs comprehensive validation according to SEP-41.
 * ALL 10 standard functions are REQUIRED:
 *
 * ### Required Functions (strict validation with protocol version support):
 * - allowance(from: Address, spender: Address) -> i128
 * - approve(from: Address, spender: Address, amount: i128, live_until_ledger|expiration_ledger: u32) -> ()
 * - balance(id: Address) -> i128
 * - transfer(from: Address, to|to_muxed: Address|MuxedAddress, amount: i128) -> ()
 * - transfer_from(spender: Address, from: Address, to: Address, amount: i128) -> ()
 * - burn(from: Address, amount: i128) -> ()
 * - burn_from(spender: Address, from: Address, amount: i128) -> ()
 * - decimals() -> u32
 * - name() -> String
 * - symbol() -> String
 *
 * ## Protocol Version Compatibility
 *
 * The validation accepts multiple parameter forms to support contracts deployed across different protocol versions:
 * - **approve**: Accepts both "live_until_ledger" (protocol 23+) and "expiration_ledger" (pre-protocol 23)
 * - **transfer**: Accepts both "to_muxed: MuxedAddress" (protocol 23+) and "to: Address" (pre-protocol 23)
 *
 * ## Function Parameter Extraction
 *
 * The contract specification provides detailed information about each function:
 * - Function name
 * - Function description (from spec or hardcoded)
 * - Parameter names and types
 * - Return type
 *
 * This information is used to:
 * 1. Dynamically generate UI input fields
 * 2. Validate user inputs
 * 3. Convert inputs to the correct XDR types
 *
 * ## Current Ledger Information
 *
 * The current ledger sequence is fetched to help users calculate expiration ledger values
 * for time-sensitive operations like `approve`. This is particularly useful because:
 * - Expiration ledgers must be in the future
 * - Users need a reference point for calculating expiration
 * - The ledger number increments approximately every 5 seconds
 *
 * @param contractId The contract ID (C... format, 56 characters)
 * @return InvokeTokenResult.ContractLoaded with contract details if successful, InvokeTokenResult.Error if failed
 */
suspend fun loadTokenContract(contractId: String): InvokeTokenResult {
    return try {
        // Step 1: Validate contract ID format
        StellarValidation.validateContractId(contractId)?.let { error ->
            return InvokeTokenResult.Error(message = error)
        }

        // Step 2: Determine network and RPC URL for testnet
        val network = Network.TESTNET
        val rpcUrl = "https://soroban-testnet.stellar.org:443"

        // Step 3: Load contract specification from the network
        val client = try {
            ContractClient.forContract(
                contractId = contractId,
                rpcUrl = rpcUrl,
                network = network
            )
        } catch (e: Exception) {
            return InvokeTokenResult.Error(
                message = "Failed to load contract: ${e.message}",
                exception = e
            )
        }

        // Step 4: Extract function information from contract spec
        val spec = client.getContractSpec()
            ?: return InvokeTokenResult.Error(
                message = "Failed to load contract specification"
            )
        val functions = parseFunctionsFromSpec(spec)

        // Step 5: Validate that this is a token contract (supports both old and new protocol versions)
        val functionsByName = functions.associateBy { it.name }
        val validationErrors = mutableListOf<String>()

        // Validate all 10 required functions
        for (requiredFunc in TokenInterface.REQUIRED_FUNCTIONS) {
            val contractFunc = functionsByName[requiredFunc.name]

            if (contractFunc == null) {
                validationErrors.add("Missing required function: ${requiredFunc.name}")
            } else {
                // Get return type from spec
                val specFunc = spec.funcs().find { it.name.value == requiredFunc.name }
                val contractReturnType = specFunc?.outputs?.firstOrNull()?.let { formatTypeName(it) }

                // Validate function signature (parameters + return type)
                // Validation now supports protocol version alternatives
                validateFunctionSignature(contractFunc, requiredFunc, contractReturnType)?.let { error ->
                    validationErrors.add(error)
                }
            }
        }

        // If there are validation errors, return error with details
        if (validationErrors.isNotEmpty()) {
            return InvokeTokenResult.Error(
                message = "Contract does not implement the standard token interface:\n${validationErrors.joinToString("\n")}"
            )
        }

        // Step 6: Query token metadata (name and symbol)
        // These are read-only calls, so we use a dummy account (no real account needed)
        val dummySource = try {
            KeyPair.fromAccountId(DUMMY_ACCOUNT_ID)
        } catch (e: Exception) {
            return InvokeTokenResult.Error(
                message = "Failed to create dummy source for read-only calls: ${e.message}",
                exception = e
            )
        }

        val tokenName = try {
            val nameXdr = client.invoke<SCValXdr>(
                functionName = "name",
                arguments = emptyMap(),
                source = dummySource.getAccountId(),
                signer = null // Read-only call
            )
            client.funcResToNative("name", nameXdr)?.toString() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }

        val tokenSymbol = try {
            val symbolXdr = client.invoke<SCValXdr>(
                functionName = "symbol",
                arguments = emptyMap(),
                source = dummySource.getAccountId(),
                signer = null // Read-only call
            )
            client.funcResToNative("symbol", symbolXdr)?.toString() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }

        // Step 7: Fetch current ledger from network
        // This helps users know what value to use for expiration ledger parameters
        val currentLedger = try {
            val latestLedger = client.server.getLatestLedger()
            latestLedger.sequence
        } catch (e: Exception) {
            0L // Default to 0 if fetch fails
        }

        // Step 8: Return success with contract details
        InvokeTokenResult.ContractLoaded(
            contractName = tokenName,
            contractSymbol = tokenSymbol,
            functions = functions,
            client = client,
            currentLedger = currentLedger
        )

    } catch (e: Exception) {
        InvokeTokenResult.Error(
            message = "Unexpected error: ${e.message ?: "Unknown error"}",
            exception = e
        )
    }
}

/**
 * Invokes a token contract function with the provided arguments and signers.
 *
 * This function demonstrates a **hybrid signing approach** that combines:
 * 1. **Hardcoded signing requirements** - Expected signers for standard token functions (upfront guidance)
 * 2. **Dynamic signer discovery** - Actual signing requirements discovered via `needsNonInvokerSigningBy()` after simulation
 *
 * ## Signing Workflow
 *
 * ### For Read-Only Functions
 * - No signing required
 * - Uses dummy account automatically
 * - Returns simulated result immediately
 *
 * ### For Write Functions
 * - Builds transaction using `buildInvoke()` (power API)
 * - Simulates to discover actual signing requirements
 * - Checks if all required signers are provided
 * - Returns `NeedsAdditionalSigners` if missing signatures
 * - Signs with all provided signers and submits
 *
 * ## Multi-Signature Support
 *
 * The function accepts a list of signer keypairs, enabling:
 * - Source account signing (transaction submitter)
 * - Additional account signing (authorization entries)
 * - Example: `transfer(from=Alice, to=Bob)` submitted by Charlie requires Alice's signature
 *
 * @param client The ContractClient instance (from loadTokenContract)
 * @param functionName The name of the function to invoke
 * @param arguments Map of argument names to values (native Kotlin types)
 * @param sourceAccountId The source account ID for write transactions (ignored for read-only calls, can be null)
 * @param signerKeyPairs List of KeyPairs for signing (source + additional auth signers)
 * @return InvokeTokenResult based on outcome
 */
suspend fun invokeTokenFunction(
    client: ContractClient,
    functionName: String,
    arguments: Map<String, Any?>,
    sourceAccountId: String?,
    signerKeyPairs: List<KeyPair>
): InvokeTokenResult {
    return try {
        // Step 1: Determine if this is a read-only function
        val isReadOnly = TokenInterface.READ_ONLY_FUNCTIONS.contains(functionName)

        // Step 2: Use dummy account for read-only calls, user-provided account for writes
        val actualSourceAccountId = if (isReadOnly) {
            // Read-only: Use dummy account automatically (no validation needed)
            DUMMY_ACCOUNT_ID
        } else {
            // Write: Validate user-provided source account
            sourceAccountId?.let { accountId ->
                StellarValidation.validateAccountId(accountId)?.let { error ->
                    return InvokeTokenResult.Error(message = error)
                }
                accountId
            } ?: return InvokeTokenResult.Error(
                message = "Source account required for write function '$functionName'"
            )
        }

        // Step 3: Validate that we have at least one signer for write functions
        if (!isReadOnly && signerKeyPairs.isEmpty()) {
            return InvokeTokenResult.Error(
                message = "Write function '$functionName' requires at least one signer (source account). Please provide secret seeds."
            )
        }

        // Step 4: For write functions, use buildInvoke() to get control over signing
        if (!isReadOnly) {
            // Build the transaction (simulates automatically)
            // Use SCValXdr as type parameter since we'll parse manually later
            val assembledTx = try {
                client.buildInvoke<SCValXdr>(
                    functionName = functionName,
                    arguments = arguments,
                    source = actualSourceAccountId,
                    signer = signerKeyPairs.firstOrNull(), // Source signer
                    parseResultXdrFn = null // We'll parse manually
                )
            } catch (e: Exception) {
                return InvokeTokenResult.Error(
                    message = "Failed to build transaction for '$functionName': ${e.message}",
                    exception = e
                )
            }

            // Check if additional signers are needed beyond the invoker
            val additionalSignersNeeded = try {
                assembledTx.needsNonInvokerSigningBy()
            } catch (e: Exception) {
                return InvokeTokenResult.Error(
                    message = "Failed to check signing requirements: ${e.message}",
                    exception = e
                )
            }

            // If additional signers are needed, check if we have them
            if (additionalSignersNeeded.isNotEmpty()) {
                // Get account IDs from provided signers
                val providedSignerIds = signerKeyPairs.map { it.getAccountId() }.toSet()

                // Check for missing signers
                val missingSigners = additionalSignersNeeded.toList().filter { signer ->
                    !providedSignerIds.contains(signer)
                }

                if (missingSigners.isNotEmpty()) {
                    // Return result indicating more signatures needed
                    return InvokeTokenResult.NeedsAdditionalSigners(
                        functionName = functionName,
                        requiredSigners = missingSigners,
                        message = "Function '$functionName' requires signatures from the following account(s): ${missingSigners.joinToString(", ")}. Please provide their secret seeds and invoke again."
                    )
                }

                // We have all required signers - sign auth entries for each additional signer
                for (signer in signerKeyPairs) {
                    val signerId = signer.getAccountId()
                    if (additionalSignersNeeded.contains(signerId)) {
                        try {
                            assembledTx.signAuthEntries(signer)
                        } catch (e: Exception) {
                            return InvokeTokenResult.Error(
                                message = "Failed to sign authorization entries for $signerId: ${e.message}",
                                exception = e
                            )
                        }
                    }
                }
            }

            // Submit the transaction
            val resultXdr = try {
                assembledTx.signAndSubmit(signerKeyPairs.firstOrNull())
            } catch (e: Exception) {
                return InvokeTokenResult.Error(
                    message = "Failed to submit transaction for '$functionName': ${e.message}",
                    exception = e
                )
            }

            // Parse the result
            val result = try {
                val nativeResult = client.funcResToNative(functionName, resultXdr)
                formatResult(nativeResult)
            } catch (e: Exception) {
                // Fallback to XDR string representation if parsing fails
                resultXdr.toString()
            }

            // Get transaction hash
            val transactionHash = assembledTx.sendTransactionResponse?.hash ?: "Unknown"

            return InvokeTokenResult.InvocationSuccess(
                functionName = functionName,
                result = result,
                transactionHash = transactionHash,
                isReadOnly = false
            )
        }

        // Step 5: For read-only functions, use invoke() (simpler API)
        val resultXdr = try {
            client.invoke<SCValXdr>(
                functionName = functionName,
                arguments = arguments,
                source = actualSourceAccountId,
                signer = null // Read-only call
            )
        } catch (e: Exception) {
            return InvokeTokenResult.Error(
                message = "Failed to invoke function '$functionName': ${e.message}",
                exception = e
            )
        }

        // Step 6: Parse the result using funcResToNative()
        val result = try {
            val nativeResult = client.funcResToNative(functionName, resultXdr)
            formatResult(nativeResult)
        } catch (e: Exception) {
            // Fallback to XDR string representation if parsing fails
            resultXdr.toString()
        }

        // Step 7: Return success
        InvokeTokenResult.InvocationSuccess(
            functionName = functionName,
            result = result,
            transactionHash = null, // No transaction for read-only
            isReadOnly = true
        )

    } catch (e: Exception) {
        InvokeTokenResult.Error(
            message = "Unexpected error: ${e.message ?: "Unknown error"}",
            exception = e
        )
    }
}

/**
 * Parses function information from a contract specification.
 *
 * This extracts the function names, descriptions, parameter types, and determines if each function
 * is read-only based on the standard token interface. Functions are filtered to exclude
 * constructors and sorted by standard token interface order.
 *
 * @param spec The contract specification
 * @return List of ContractFunctionInfo objects, sorted by standard interface order
 */
private fun parseFunctionsFromSpec(spec: com.soneso.stellar.sdk.contract.ContractSpec): List<ContractFunctionInfo> {
    return spec.funcs()
        // Filter out constructor function - it's not needed since contract is already deployed
        .filter { function -> function.name.value != "__constructor" }
        .map { function ->
            val parameters = function.inputs.map { param ->
                ParameterInfo(
                    name = param.name,
                    typeName = formatTypeName(param.type)
                )
            }

            // Get description from spec if available, otherwise use hardcoded description
            val description = function.doc.takeIf { it.isNotBlank() }
                ?: TokenInterface.FUNCTION_DESCRIPTIONS[function.name.value]
                ?: if (TokenInterface.ALL_FUNCTIONS_SET.contains(function.name.value)) {
                    "Standard token function"
                } else {
                    "Custom function"
                }

            ContractFunctionInfo(
                name = function.name.value,
                description = description,
                parameters = parameters,
                isReadOnly = TokenInterface.READ_ONLY_FUNCTIONS.contains(function.name.value)
            )
        }
        // Sort by standard interface order, with custom functions at the end (alphabetically)
        .sortedWith(compareBy(
            { function ->
                // First sort key: standard functions (0) before custom functions (1)
                val standardIndex = TokenInterface.STANDARD_FUNCTION_ORDER.indexOf(function.name)
                if (standardIndex >= 0) 0 else 1
            },
            { function ->
                // Second sort key: within standard group, use defined order
                val standardIndex = TokenInterface.STANDARD_FUNCTION_ORDER.indexOf(function.name)
                if (standardIndex >= 0) standardIndex else Int.MAX_VALUE
            },
            { function ->
                // Third sort key: within custom group, sort alphabetically
                function.name.lowercase()
            }
        ))
}

/**
 * Formats a contract type name for human-readable display.
 *
 * This function inspects the XDR discriminant to determine the actual type
 * and returns a human-readable name. For complex types (Option, Vec, Map),
 * it recursively formats nested types.
 *
 * @param type The SCSpecTypeDefXdr from the contract spec
 * @return A human-readable type name (e.g., "Address", "Option<String>", "Vec<i128>")
 */
private fun formatTypeName(type: SCSpecTypeDefXdr): String {
    return when (type.discriminant) {
        SCSpecTypeXdr.SC_SPEC_TYPE_VAL -> "Val"
        SCSpecTypeXdr.SC_SPEC_TYPE_BOOL -> "Bool"
        SCSpecTypeXdr.SC_SPEC_TYPE_VOID -> "Void"
        SCSpecTypeXdr.SC_SPEC_TYPE_ERROR -> "Error"
        SCSpecTypeXdr.SC_SPEC_TYPE_U32 -> "u32"
        SCSpecTypeXdr.SC_SPEC_TYPE_I32 -> "i32"
        SCSpecTypeXdr.SC_SPEC_TYPE_U64 -> "u64"
        SCSpecTypeXdr.SC_SPEC_TYPE_I64 -> "i64"
        SCSpecTypeXdr.SC_SPEC_TYPE_TIMEPOINT -> "Timepoint"
        SCSpecTypeXdr.SC_SPEC_TYPE_DURATION -> "Duration"
        SCSpecTypeXdr.SC_SPEC_TYPE_U128 -> "u128"
        SCSpecTypeXdr.SC_SPEC_TYPE_I128 -> "i128"
        SCSpecTypeXdr.SC_SPEC_TYPE_U256 -> "u256"
        SCSpecTypeXdr.SC_SPEC_TYPE_I256 -> "i256"
        SCSpecTypeXdr.SC_SPEC_TYPE_BYTES -> "Bytes"
        SCSpecTypeXdr.SC_SPEC_TYPE_STRING -> "String"
        SCSpecTypeXdr.SC_SPEC_TYPE_SYMBOL -> "Symbol"
        SCSpecTypeXdr.SC_SPEC_TYPE_ADDRESS -> "Address"
        SCSpecTypeXdr.SC_SPEC_TYPE_MUXED_ADDRESS -> "MuxedAddress"
        SCSpecTypeXdr.SC_SPEC_TYPE_OPTION -> {
            // For option types, show "Option<InnerType>"
            val inner = (type as? SCSpecTypeDefXdr.Option)?.value?.valueType
            if (inner != null) "Option<${formatTypeName(inner)}>" else "Option"
        }
        SCSpecTypeXdr.SC_SPEC_TYPE_RESULT -> {
            // For result types, could show "Result<OkType, ErrType>" but simplified for now
            "Result"
        }
        SCSpecTypeXdr.SC_SPEC_TYPE_VEC -> {
            // For vector types, show "Vec<ElementType>"
            val element = (type as? SCSpecTypeDefXdr.Vec)?.value?.elementType
            if (element != null) "Vec<${formatTypeName(element)}>" else "Vec"
        }
        SCSpecTypeXdr.SC_SPEC_TYPE_MAP -> {
            // For map types, show "Map<KeyType, ValueType>"
            val mapType = type as? SCSpecTypeDefXdr.Map
            val key = mapType?.value?.keyType
            val value = mapType?.value?.valueType
            if (key != null && value != null) {
                "Map<${formatTypeName(key)}, ${formatTypeName(value)}>"
            } else "Map"
        }
        SCSpecTypeXdr.SC_SPEC_TYPE_TUPLE -> "Tuple"
        SCSpecTypeXdr.SC_SPEC_TYPE_BYTES_N -> {
            // For fixed-size byte arrays, show "Bytes32", "Bytes64", etc.
            val n = (type as? SCSpecTypeDefXdr.BytesN)?.value?.n?.value
            if (n != null) "Bytes$n" else "BytesN"
        }
        SCSpecTypeXdr.SC_SPEC_TYPE_UDT -> {
            // User-defined type - use the name from the spec
            (type as? SCSpecTypeDefXdr.Udt)?.value?.name ?: "UDT"
        }
        else -> {
            // Fallback for any unhandled types
            type.discriminant.name.removePrefix("SC_SPEC_TYPE_")
        }
    }
}

/**
 * Formats a result value for display in the UI.
 *
 * @param result The native Kotlin result from funcResToNative()
 * @return A formatted string representation
 */
private fun formatResult(result: Any?): String {
    return when (result) {
        null -> "null"
        is List<*> -> result.joinToString(", ") { formatResult(it) }
        is Map<*, *> -> result.entries.joinToString(", ") { "${it.key}: ${formatResult(it.value)}" }
        is ByteArray -> result.joinToString("") { byte ->
            val hex = byte.toInt() and 0xFF
            if (hex < 16) "0${hex.toString(16)}" else hex.toString(16)
        }
        else -> result.toString()
    }
}
