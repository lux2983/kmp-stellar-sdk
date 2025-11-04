package com.soneso.demo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Expected signers warning card component for token contract interaction.
 *
 * Displays a yellow warning card showing:
 * - Warning icon and "Signing Required" header
 * - List of expected signers (either filled account IDs or empty parameter names)
 * - Disclaimer about prediction vs actual requirements
 *
 * This component provides user feedback about which accounts will need to sign
 * a transaction before it's submitted. It shows upfront predictions based on
 * function signatures (hybrid signing approach).
 *
 * Design specifications:
 * - Background: Color(0xFFFFF9C4) - Light yellow/warning
 * - Icon color: Color(0xFFF57C00) - Orange warning
 * - Text: Default MaterialTheme
 * - No border or elevation
 *
 * Example usage:
 * ```kotlin
 * ExpectedSignersWarningCard(
 *     expectedSigners = listOf(
 *         "from" to "GABCD...",
 *         "to" to null // Parameter not filled yet
 *     ),
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param expectedSigners List of parameter name to account ID pairs (null if parameter not filled)
 * @param modifier Modifier for the card
 */
@Composable
fun ExpectedSignersWarningCard(
    expectedSigners: List<Pair<String, String?>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9C4)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Signing Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The following account(s) are expected to sign this transaction:",
                style = MaterialTheme.typography.bodyMedium
            )
            expectedSigners.forEach { (paramName, accountId) ->
                if (accountId != null) {
                    // Parameter filled: show "paramName: ACCOUNT_ID"
                    Row(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                        Text(
                            text = "• $paramName: ",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = accountId,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    // Parameter empty: show "• (paramName)"
                    Text(
                        text = "• ($paramName)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Note: This is a prediction based on the function signature. Actual requirements will be verified after simulation.",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
            )
        }
    }
}
