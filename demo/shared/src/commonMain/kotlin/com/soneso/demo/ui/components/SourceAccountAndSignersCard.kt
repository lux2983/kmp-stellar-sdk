package com.soneso.demo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Source account and signers input card component for token contract interaction.
 *
 * Displays a gold-themed card with:
 * - Source account ID input field
 * - Multiple signer secret seed input fields (password-protected)
 * - Add/remove signer buttons
 * - Validation error messages
 *
 * This component encapsulates Step 3 of the token contract interaction flow:
 * providing the source account and signing credentials for write functions.
 *
 * Design specifications:
 * - Background: Color(0xFFFFFBF0) - Warm Gold
 * - Text color: Color(0xFFA85A00) - Amber
 * - Border: 1.dp with 0.3f alpha gold
 * - Shape: MaterialTheme.shapes.medium
 * - Elevation: 2.dp
 * - Delete button: Red (0xFF991B1B)
 *
 * Example usage:
 * ```kotlin
 * SourceAccountAndSignersCard(
 *     sourceAccountId = sourceAccountId,
 *     onSourceAccountIdChange = { sourceAccountId = it },
 *     signerSeeds = signerSeeds,
 *     onSignerSeedsChange = { signerSeeds = it },
 *     validationErrors = validationErrors,
 *     onValidationErrorClear = { key ->
 *         validationErrors = validationErrors - key
 *     },
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param sourceAccountId Current source account ID value
 * @param onSourceAccountIdChange Callback when source account ID changes
 * @param signerSeeds List of signer secret seeds
 * @param onSignerSeedsChange Callback when the list of signer seeds changes
 * @param validationErrors Map of error keys to error messages
 * @param onValidationErrorClear Callback to clear a specific validation error
 * @param onEnterPressed Callback when enter is pressed on the last signer field (optional)
 * @param modifier Modifier for the card
 */
@Composable
fun SourceAccountAndSignersCard(
    sourceAccountId: String,
    onSourceAccountIdChange: (String) -> Unit,
    signerSeeds: List<String>,
    onSignerSeedsChange: (List<String>) -> Unit,
    validationErrors: Map<String, String>,
    onValidationErrorClear: (String) -> Unit,
    onEnterPressed: (() -> Unit)? = null,
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
                text = "Step 3: Source Account & Signers",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            OutlinedTextField(
                value = sourceAccountId,
                onValueChange = { value ->
                    onSourceAccountIdChange(value.trim())
                    onValidationErrorClear("sourceAccount")
                },
                label = { Text("Source Account ID") },
                placeholder = { Text("G...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = validationErrors.containsKey("sourceAccount"),
                supportingText = validationErrors["sourceAccount"]?.let { error ->
                    {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } ?: {
                    Text(
                        text = "Account that will submit the transaction",
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))

            // Multiple signer inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Secret Seeds (Signers)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            signerSeeds.forEachIndexed { index, seed ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = seed,
                        onValueChange = { newSeed ->
                            val updatedSeeds = signerSeeds.toMutableList().apply {
                                this[index] = newSeed.trim()
                            }
                            onSignerSeedsChange(updatedSeeds)
                            onValidationErrorClear("secretSeed_$index")
                            onValidationErrorClear("signers")
                        },
                        label = { Text("Secret Seed ${index + 1}") },
                        placeholder = { Text("S...") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = validationErrors.containsKey("secretSeed_$index") ||
                                  validationErrors.containsKey("signers"),
                        supportingText = validationErrors["secretSeed_$index"]?.let { error ->
                            {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } ?: if (index == 0 && validationErrors.containsKey("signers")) {
                            {
                                Text(
                                    text = validationErrors["signers"]!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            {
                                Text(
                                    text = if (index == 0) "Required for signing (source or auth)" else "Optional additional signer",
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = if (index == signerSeeds.lastIndex) ImeAction.Done else ImeAction.Next
                        ),
                        keyboardActions = if (index == signerSeeds.lastIndex && onEnterPressed != null) {
                            KeyboardActions(
                                onDone = {
                                    onEnterPressed()
                                }
                            )
                        } else {
                            KeyboardActions.Default
                        }
                    )

                    // Delete button (only show if more than one signer field)
                    if (signerSeeds.size > 1) {
                        IconButton(
                            onClick = {
                                val updatedSeeds = signerSeeds.toMutableList().apply {
                                    removeAt(index)
                                }
                                onSignerSeedsChange(updatedSeeds)
                            },
                            modifier = Modifier.size(48.dp).padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove signer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Add signer button
            TextButton(
                onClick = {
                    onSignerSeedsChange(signerSeeds + "")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Additional Signer")
            }
        }
    }
}
