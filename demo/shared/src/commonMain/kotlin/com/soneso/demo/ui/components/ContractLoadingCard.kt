package com.soneso.demo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Contract loading card component for token contract interaction.
 *
 * Displays a gold-themed card with:
 * - Contract ID input field with validation
 * - Load contract button with loading state
 * - Helpful placeholder text and error messages
 *
 * This component encapsulates Step 1 of the token contract interaction flow:
 * loading and validating the contract's SEP-41 compliance.
 *
 * Design specifications:
 * - Background: Color(0xFFFFFBF0) - Warm Gold
 * - Text color: Color(0xFFA85A00) - Amber
 * - Border: 1.dp with 0.3f alpha gold
 * - Shape: MaterialTheme.shapes.medium
 * - Elevation: 2.dp
 * - Button: Gold (0xFFD97706)
 *
 * Example usage:
 * ```kotlin
 * ContractLoadingCard(
 *     contractId = contractId,
 *     onContractIdChange = { contractId = it },
 *     onLoadContract = { loadContract() },
 *     isLoading = isLoadingContract,
 *     validationError = validationErrors["contractId"],
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param contractId Current contract ID value
 * @param onContractIdChange Callback when contract ID changes
 * @param onLoadContract Callback when load button is clicked or enter is pressed
 * @param isLoading Whether the contract is currently being loaded
 * @param validationError Error message to display, or null if no error
 * @param modifier Modifier for the card
 */
@Composable
fun ContractLoadingCard(
    contractId: String,
    onContractIdChange: (String) -> Unit,
    onLoadContract: () -> Unit,
    isLoading: Boolean,
    validationError: String?,
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
                text = "Step 1: Load Token Contract",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            OutlinedTextField(
                value = contractId,
                onValueChange = onContractIdChange,
                label = { Text("Contract ID") },
                placeholder = { Text("C...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = validationError != null,
                supportingText = validationError?.let { error ->
                    {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } ?: {
                    Text(
                        text = "Enter a deployed token contract ID (you can deploy a token contract in the Deploy Smart Contract demo)",
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onLoadContract() }
                )
            )

            AnimatedButton(
                onClick = onLoadContract,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = contractId.isNotBlank() && !isLoading,
                isLoading = isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Load Contract",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
