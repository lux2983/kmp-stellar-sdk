package com.soneso.demo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.soneso.demo.platform.getClipboard
import com.soneso.demo.stellar.*
import com.soneso.demo.ui.FormValidation
import com.soneso.demo.ui.components.*
import com.soneso.stellar.sdk.KeyPair
import kotlinx.coroutines.launch

/**
 * Represents the state of contract loading and validation.
 */
private data class ContractState(
    val contractId: String = "",
    val isLoading: Boolean = false,
    val loaded: InvokeTokenResult.ContractLoaded? = null,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

/**
 * Represents the state of function invocation.
 */
private data class InvocationState(
    val selectedFunction: ContractFunctionInfo? = null,
    val arguments: Map<String, String> = emptyMap(),
    val isInvoking: Boolean = false,
    val result: InvokeTokenResult? = null
)

/**
 * Represents the state of signing and authorization.
 */
private data class SigningState(
    val sourceAccountId: String = "",
    val signerSeeds: List<String> = listOf(""),
    val expectedSigners: List<Pair<String, String?>> = emptyList()
)

class InvokeTokenContractScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var contractState by remember { mutableStateOf(ContractState()) }
        var invocationState by remember { mutableStateOf(InvocationState()) }
        var signingState by remember { mutableStateOf(SigningState()) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scrollState = rememberScrollState()

        // Smart auto-scroll: scroll just enough to reveal result when invocation completes
        LaunchedEffect(invocationState.result) {
            // Only scroll when we have a result, not when clearing it
            if (invocationState.result != null) {
                val currentScroll = scrollState.value
                val targetScroll = (currentScroll + 300).coerceAtMost(scrollState.maxValue)
                scrollState.animateScrollTo(targetScroll)
            }
        }

        // Update expected signers when function or args change
        LaunchedEffect(invocationState.selectedFunction, invocationState.arguments) {
            invocationState.selectedFunction?.let { func ->
                if (!func.isReadOnly) {
                    signingState = signingState.copy(
                        expectedSigners = getExpectedSigners(func.name, invocationState.arguments)
                    )
                }
            }
        }

        // Clear signing state when function changes
        LaunchedEffect(invocationState.selectedFunction) {
            invocationState.selectedFunction?.let {
                signingState = SigningState() // Reset to default values
            }
        }

        // Function to load contract
        fun loadContract() {
            val errors = mutableMapOf<String, String>()
            FormValidation.validateContractIdField(contractState.contractId)?.let {
                errors["contractId"] = it
            }

            if (errors.isNotEmpty()) {
                contractState = contractState.copy(validationErrors = errors)
                return
            }

            coroutineScope.launch {
                contractState = contractState.copy(
                    isLoading = true,
                    error = null,
                    loaded = null,
                    validationErrors = emptyMap()
                )
                invocationState = invocationState.copy(
                    selectedFunction = null,
                    arguments = emptyMap(),
                    result = null
                )

                when (val result = loadTokenContract(contractState.contractId)) {
                    is InvokeTokenResult.ContractLoaded -> {
                        contractState = contractState.copy(loaded = result)
                    }
                    is InvokeTokenResult.Error -> {
                        contractState = contractState.copy(error = result.message)
                    }
                    else -> {
                        contractState = contractState.copy(error = "Unexpected result type")
                    }
                }

                contractState = contractState.copy(isLoading = false)
            }
        }

        // Validation helper functions
        fun validateWriteFunctionInputs(
            sourceAccountId: String,
            signerSeeds: List<String>
        ): Map<String, String> {
            val errors = mutableMapOf<String, String>()

            FormValidation.validateAccountIdField(sourceAccountId)?.let {
                errors["sourceAccount"] = it
            }

            signerSeeds.forEachIndexed { index, seed ->
                if (seed.isNotBlank()) {
                    FormValidation.validateSecretSeedField(seed)?.let {
                        errors["secretSeed_$index"] = it
                    }
                }
            }

            if (signerSeeds.all { it.isBlank() }) {
                errors["signers"] = "At least one secret seed is required for write functions"
            }

            return errors
        }

        fun validateFunctionArguments(
            parameters: List<ParameterInfo>,
            arguments: Map<String, String>
        ): Map<String, String> {
            val errors = mutableMapOf<String, String>()

            parameters.forEach { param ->
                val value = arguments[param.name] ?: ""
                if (value.isBlank()) {
                    errors["arg_${param.name}"] = "Required"
                }
            }

            return errors
        }

        // Function to invoke selected function
        fun invokeFunction() {
            val contract = contractState.loaded ?: return
            val function = invocationState.selectedFunction ?: return

            // Validate inputs using extracted validation functions
            val errors = mutableMapOf<String, String>()

            if (!function.isReadOnly) {
                errors.putAll(validateWriteFunctionInputs(signingState.sourceAccountId, signingState.signerSeeds))
            }

            errors.putAll(validateFunctionArguments(function.parameters, invocationState.arguments))

            if (errors.isNotEmpty()) {
                contractState = contractState.copy(validationErrors = errors)
                return
            }

            coroutineScope.launch {
                invocationState = invocationState.copy(
                    isInvoking = true,
                    result = null
                )
                contractState = contractState.copy(validationErrors = emptyMap())

                try {
                    // Convert string arguments to appropriate types based on parameter spec
                    val typedArguments = invocationState.arguments.mapValues { (paramName, value) ->
                        // Find the parameter type from the function spec
                        val paramType = function.parameters.find { it.name == paramName }?.typeName

                        // Convert based on type
                        when {
                            paramType == null -> value // Unknown type, pass as string
                            paramType.equals("Bool", ignoreCase = true) -> {
                                // Boolean type: convert "true"/"false" strings to Boolean
                                when (value.lowercase()) {
                                    "true" -> true
                                    "false" -> false
                                    else -> value // Invalid boolean, let SDK handle error
                                }
                            }
                            paramType.equals("Address", ignoreCase = true) -> {
                                // Address type: validate format and pass as string
                                // SDK's funcArgsToXdrSCValues will handle the conversion
                                value
                            }
                            else -> {
                                // For numeric types, try to parse as Long, otherwise pass as string
                                // SDK's funcArgsToXdrSCValues will handle the conversion based on contract spec
                                value.toLongOrNull() ?: value
                            }
                        }
                    }

                    // Create KeyPairs for non-blank seeds
                    val signers = if (function.isReadOnly) {
                        emptyList()
                    } else {
                        try {
                            signingState.signerSeeds.filter { it.isNotBlank() }.map { seed ->
                                KeyPair.fromSecretSeed(seed)
                            }
                        } catch (e: Exception) {
                            invocationState = invocationState.copy(
                                result = InvokeTokenResult.Error(
                                    message = "Invalid secret seed: ${e.message}",
                                    exception = e
                                ),
                                isInvoking = false
                            )
                            return@launch
                        }
                    }

                    // Invoke the function
                    invocationState = invocationState.copy(
                        result = invokeTokenFunction(
                            client = contract.client,
                            functionName = function.name,
                            arguments = typedArguments,
                            sourceAccountId = if (function.isReadOnly) null else signingState.sourceAccountId,
                            signerKeyPairs = signers
                        )
                    )
                } catch (e: Exception) {
                    invocationState = invocationState.copy(
                        result = InvokeTokenResult.Error(
                            message = "Unexpected error: ${e.message}",
                            exception = e
                        )
                    )
                } finally {
                    invocationState = invocationState.copy(isInvoking = false)
                }
            }
        }

        // Update function arguments when selected function changes
        LaunchedEffect(invocationState.selectedFunction) {
            invocationState.selectedFunction?.let { function ->
                invocationState = invocationState.copy(
                    arguments = function.parameters.associate { it.name to "" },
                    result = null
                )
                signingState = signingState.copy(expectedSigners = emptyList())
            }
        }

        Scaffold(
            topBar = {
                StellarTopBar(
                    title = "Invoke Token Contract",
                    onNavigationClick = { navigator.pop() }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Information card - Purple
                    InfoCardMediumTitle(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Token Contract Interaction",
                        description = "Load a Stellar token contract (SEP-41 compliant) and interact with its functions. " +
                                "The demo validates the token interface, parses function signatures, and dynamically generates " +
                                "UI for parameter input. It demonstrates a hybrid signing approach: showing expected signers " +
                                "upfront for standard functions, then using dynamic discovery to detect actual requirements."
                    )

                    // Step 1: Contract ID Input - Gold Card
                    ContractLoadingCard(
                        contractId = contractState.contractId,
                        onContractIdChange = { newValue ->
                            contractState = contractState.copy(
                                contractId = newValue.trim(),
                                validationErrors = contractState.validationErrors - "contractId",
                                error = null
                            )
                        },
                        onLoadContract = { loadContract() },
                        isLoading = contractState.isLoading,
                        validationError = contractState.validationErrors["contractId"],
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Load error display
                    contractState.error?.let { error ->
                        ErrorCard(error, null)
                    }

                    // Step 2: Contract loaded - show details and function selection
                    contractState.loaded?.let { contract ->
                        // Contract info - Teal card
                        TealCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Token Contract Loaded",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Name",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                    SelectionContainer {
                                        Text(
                                            text = contract.contractName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Symbol",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                    SelectionContainer {
                                        Text(
                                            text = contract.contractSymbol,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                            // Display current ledger and function count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (contract.currentLedger > 0) {
                                            "Current Ledger: ${contract.currentLedger}"
                                        } else {
                                            "Current Ledger: Unknown"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )

                                    if (contract.currentLedger > 0) {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val clipboard = getClipboard()
                                                    val copied = clipboard.copyToClipboard(contract.currentLedger.toString())
                                                    if (copied) {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Current ledger copied to clipboard",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy current ledger",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${contract.functions.size} functions",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Step 2: Function selection - Gold card
                        FunctionSelectionCard(
                            functions = contract.functions,
                            selectedFunction = invocationState.selectedFunction,
                            onFunctionSelected = { function ->
                                invocationState = invocationState.copy(
                                    selectedFunction = function,
                                    result = null
                                )
                            },
                            functionArguments = invocationState.arguments,
                            onArgumentChange = { paramName, value ->
                                invocationState = invocationState.copy(
                                    arguments = invocationState.arguments + (paramName to value),
                                    result = null
                                )
                                contractState = contractState.copy(
                                    validationErrors = contractState.validationErrors - "arg_$paramName"
                                )
                            },
                            validationErrors = contractState.validationErrors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Step 3: Show expected signers for standard functions (before source account input)
                        invocationState.selectedFunction?.let { function ->
                            if (signingState.expectedSigners.isNotEmpty() && !function.isReadOnly) {
                                ExpectedSignersWarningCard(
                                    expectedSigners = signingState.expectedSigners,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Step 4: Source account and secret seeds (ONLY for write functions)
                        invocationState.selectedFunction?.let { function ->
                            // Show source account and secret seeds ONLY for write functions
                            if (!function.isReadOnly) {
                                SourceAccountAndSignersCard(
                                    sourceAccountId = signingState.sourceAccountId,
                                    onSourceAccountIdChange = { newValue ->
                                        signingState = signingState.copy(sourceAccountId = newValue)
                                        invocationState = invocationState.copy(result = null)
                                    },
                                    signerSeeds = signingState.signerSeeds,
                                    onSignerSeedsChange = { newSeeds ->
                                        signingState = signingState.copy(signerSeeds = newSeeds)
                                        invocationState = invocationState.copy(result = null)
                                    },
                                    validationErrors = contractState.validationErrors,
                                    onValidationErrorClear = { key ->
                                        contractState = contractState.copy(
                                            validationErrors = contractState.validationErrors - key
                                        )
                                    },
                                    onEnterPressed = { invokeFunction() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Memoized form validation for button enabled state (iOS performance optimization)
                            val isFormValid = remember(
                                invocationState.isInvoking,
                                invocationState.selectedFunction,
                                signingState.sourceAccountId,
                                signingState.signerSeeds
                            ) {
                                when {
                                    invocationState.isInvoking -> false
                                    function.isReadOnly -> true
                                    else -> signingState.sourceAccountId.isNotBlank() && signingState.signerSeeds.any { it.isNotBlank() }
                                }
                            }

                            // Invoke button
                            AnimatedButton(
                                onClick = { invokeFunction() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = isFormValid,
                                isLoading = invocationState.isInvoking,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (function.isReadOnly) {
                                        MaterialTheme.colorScheme.secondary // Green for read calls
                                    } else {
                                        Color(0xFF0A4FD6) // Blue for write calls
                                    },
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (invocationState.isInvoking) {
                                        if (function.isReadOnly) "Simulating..." else "Invoking..."
                                    } else if (function.isReadOnly) {
                                        "Simulate Read Call"
                                    } else {
                                        "Invoke Function"
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Invocation result display
                    invocationState.result?.let { result ->
                        when (result) {
                            is InvokeTokenResult.InvocationSuccess -> {
                                InvocationSuccessCard(
                                    result = result,
                                    snackbarHostState = snackbarHostState,
                                    coroutineScope = coroutineScope
                                )
                            }
                            is InvokeTokenResult.NeedsAdditionalSigners -> {
                                NeedsAdditionalSignersCard(result)
                            }
                            is InvokeTokenResult.Error -> {
                                ErrorCard(result.message, result.exception)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Troubleshooting tips
                                InfoCardMediumTitle(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Troubleshooting",
                                    description = "• Verify the contract ID is correct and deployed on testnet\n" +
                                            "• Ensure parameter types match the contract specification\n" +
                                            "• For write functions, verify the source account has sufficient XLM for fees\n" +
                                            "• Check that all required signers have provided their secret seeds\n" +
                                            "• For token operations, ensure the account has the necessary trustlines\n" +
                                            "• Check your internet connection and Soroban RPC availability"
                                )
                            }
                            else -> {
                                // Ignore other result types in this context
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvocationSuccessCard(
    result: InvokeTokenResult.InvocationSuccess,
    snackbarHostState: SnackbarHostState,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    // Result card - Green (success)
    GreenCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Function: ${result.functionName}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = "Result",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
        )

        SelectionContainer {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = if (result.result == "null") "ok" else result.result,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))

        Text(
            text = if (result.isReadOnly) {
                "Read-only call completed successfully. No transaction was submitted."
            } else {
                "Write call completed successfully. Transaction was submitted and confirmed."
            },
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        )

        result.transactionHash?.let { hash ->
            if (!result.isReadOnly) {
                HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))

                Text(
                    text = "Transaction Hash",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionContainer(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = hash,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val clipboard = getClipboard()
                                val copied = clipboard.copyToClipboard(hash)
                                if (copied) {
                                    snackbarHostState.showSnackbar(
                                        message = "Transaction hash copied to clipboard",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy transaction hash",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeedsAdditionalSignersCard(result: InvokeTokenResult.NeedsAdditionalSigners) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE) // Light red
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Additional Signatures Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "The following accounts must sign:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
            result.requiredSigners.forEach { signer ->
                Text(
                    text = "• $signer",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Please enter the secret seeds for these accounts above and invoke again.",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, exception: Throwable?) {
    // Error header - Red
    com.soneso.demo.ui.components.ErrorCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
        )

        // Technical details in white nested card
        exception?.let { ex ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Technical Details",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                        Text(
                            text = ex.message ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 20.sp
                            ),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
