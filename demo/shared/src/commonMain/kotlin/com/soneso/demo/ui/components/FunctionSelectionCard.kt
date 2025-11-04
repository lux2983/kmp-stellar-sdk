package com.soneso.demo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soneso.demo.stellar.ContractFunctionInfo
import com.soneso.demo.stellar.ParameterInfo

/**
 * Function selection card component for token contract interaction.
 *
 * Displays a gold-themed card with:
 * - Function dropdown with descriptions and read/write indicators
 * - Dynamic parameter input fields based on selected function
 * - Function type information (read-only vs write)
 * - Validation error messages
 *
 * This component encapsulates Step 2 of the token contract interaction flow:
 * selecting a function and providing its arguments.
 *
 * Design specifications:
 * - Background: Color(0xFFFFFBF0) - Warm Gold
 * - Text color: Color(0xFFA85A00) - Amber
 * - Border: 1.dp with 0.3f alpha gold
 * - Shape: MaterialTheme.shapes.medium
 * - Elevation: 2.dp
 * - Read functions: Teal indicator (0xFF0F766E)
 * - Write functions: Gold indicator (0xFFD97706)
 *
 * Example usage:
 * ```kotlin
 * FunctionSelectionCard(
 *     functions = contract.functions,
 *     selectedFunction = selectedFunction,
 *     onFunctionSelected = { selectedFunction = it },
 *     functionArguments = functionArguments,
 *     onArgumentChange = { name, value ->
 *         functionArguments = functionArguments + (name to value)
 *     },
 *     validationErrors = validationErrors,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param functions List of available contract functions
 * @param selectedFunction Currently selected function, or null if none selected
 * @param onFunctionSelected Callback when a function is selected from the dropdown
 * @param functionArguments Map of parameter names to their current values
 * @param onArgumentChange Callback when a parameter value changes
 * @param validationErrors Map of parameter names to error messages (keyed as "arg_paramName")
 * @param modifier Modifier for the card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionSelectionCard(
    functions: List<ContractFunctionInfo>,
    selectedFunction: ContractFunctionInfo?,
    onFunctionSelected: (ContractFunctionInfo) -> Unit,
    functionArguments: Map<String, String>,
    onArgumentChange: (String, String) -> Unit,
    validationErrors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF0)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Step 2: Select Function",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            // Function dropdown with descriptions
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedFunction?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Function") },
                    placeholder = { Text("Select a function") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    functions.forEach { function ->
                        DropdownMenuItem(
                            text = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = function.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        Text(
                                            text = if (function.isReadOnly) "READ" else "WRITE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (function.isReadOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Text(
                                        text = function.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            onClick = {
                                onFunctionSelected(function)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Show function details when selected
            selectedFunction?.let { function ->
                HorizontalDivider(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))

                Text(
                    text = "Function Type: ${if (function.isReadOnly) "Read-Only" else "Write (requires signing)"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )

                // Display function description
                Text(
                    text = "Description: ${function.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (function.parameters.isNotEmpty()) {
                    Text(
                        text = "Parameters (${function.parameters.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    // Dynamic parameter inputs
                    function.parameters.forEach { param ->
                        OutlinedTextField(
                            value = functionArguments[param.name] ?: "",
                            onValueChange = { value ->
                                onArgumentChange(param.name, value)
                            },
                            label = { Text("${param.name} (${param.typeName})") },
                            placeholder = { Text("Enter ${param.typeName}") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = validationErrors.containsKey("arg_${param.name}"),
                            supportingText = validationErrors["arg_${param.name}"]?.let { error ->
                                {
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.tertiary
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = if (param == function.parameters.last() && function.isReadOnly) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions.Default
                        )
                    }
                } else {
                    Text(
                        text = "No parameters required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
