package com.soneso.demo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * BlueCard - Stellar Blue themed card component
 *
 * Used for displaying general information, details, and data across screens.
 * Most commonly used card style in the demo app.
 *
 * Design specifications:
 * - Background: Color(0xFFE8F1FF) - Nebula Blue
 * - Border: Color(0xFF0A4FD6).copy(alpha = 0.2f) - Stellar Blue with transparency
 * - Text color: Color(0xFF0639A3) - Stellar Blue Dark
 * - Shape: MaterialTheme.shapes.medium (RoundedCornerShape(16.dp))
 * - Elevation: 2.dp
 * - Internal padding: 20.dp
 * - Content spacing: 12.dp vertical
 *
 * Common usage:
 * - Account details and balances
 * - Transaction information
 * - Contract metadata
 * - Operation details
 *
 * @param modifier Modifier for the card (typically Modifier.fillMaxWidth())
 * @param content Composable lambda for card content with 20.dp padding and 12.dp vertical spacing
 *
 * Example usage:
 * ```kotlin
 * BlueCard(modifier = Modifier.fillMaxWidth()) {
 *     Text(
 *         text = "Basic Information",
 *         style = MaterialTheme.typography.titleMedium.copy(
 *             fontWeight = FontWeight.Bold
 *         ),
 *         color = Color(0xFF0639A3)
 *     )
 *     HorizontalDivider(color = Color(0xFF0A4FD6).copy(alpha = 0.2f))
 *     DetailRow("Field", "Value")
 * }
 * ```
 */
@Composable
fun BlueCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0xFF0A4FD6).copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.medium
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F1FF) // Nebula Blue
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/**
 * GoldCard - Starlight Gold themed card component
 *
 * Used for input forms, configuration sections, and interactive content.
 * Second most common card style in the demo app.
 *
 * Design specifications:
 * - Background: Color(0xFFFFFBF0) - Warm light gold
 * - Border: Color(0xFFD97706).copy(alpha = 0.3f) - Starlight Gold with transparency
 * - Text color: Color(0xFFA85A00) - Starlight Gold Dark
 * - Shape: MaterialTheme.shapes.medium (RoundedCornerShape(16.dp))
 * - Elevation: 2.dp
 * - Internal padding: 20.dp
 * - Content spacing: 12.dp vertical
 *
 * Common usage:
 * - Input forms and text fields
 * - Configuration sections
 * - Parameter entry
 * - Account information input
 *
 * @param modifier Modifier for the card (typically Modifier.fillMaxWidth())
 * @param content Composable lambda for card content with 20.dp padding and 12.dp vertical spacing
 *
 * Example usage:
 * ```kotlin
 * GoldCard(modifier = Modifier.fillMaxWidth()) {
 *     Text(
 *         text = "Payment Configuration",
 *         style = MaterialTheme.typography.titleMedium.copy(
 *             fontWeight = FontWeight.Bold
 *         ),
 *         color = Color(0xFFA85A00)
 *     )
 *     OutlinedTextField(...)
 * }
 * ```
 */
@Composable
fun GoldCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0xFFD97706).copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF0) // Warm gold background
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/**
 * TealCard - Nebula Teal themed card component
 *
 * Used for success messages, confirmation headers, and positive status indicators.
 *
 * Design specifications:
 * - Background: Color(0xFFF0FDFA) - Nebula Teal Container
 * - Border: Color(0xFF0F766E).copy(alpha = 0.3f) - Nebula Teal Dark with transparency
 * - Text color: Color(0xFF0F766E) - Nebula Teal Dark
 * - Shape: MaterialTheme.shapes.medium (RoundedCornerShape(16.dp))
 * - Elevation: 2.dp
 * - Internal padding: 20.dp
 * - Content spacing: 12.dp vertical
 *
 * Common usage:
 * - Success headers
 * - Confirmation messages
 * - Positive status indicators
 * - Completed operation results
 *
 * @param modifier Modifier for the card (typically Modifier.fillMaxWidth())
 * @param content Composable lambda for card content with 20.dp padding and 12.dp vertical spacing
 *
 * Example usage:
 * ```kotlin
 * TealCard(modifier = Modifier.fillMaxWidth()) {
 *     Row(
 *         verticalAlignment = Alignment.CenterVertically,
 *         horizontalArrangement = Arrangement.spacedBy(12.dp)
 *     ) {
 *         Icon(
 *             imageVector = Icons.Default.Check,
 *             contentDescription = null,
 *             tint = Color(0xFF0F766E),
 *             modifier = Modifier.size(28.dp)
 *         )
 *         Text(
 *             text = "Success",
 *             style = MaterialTheme.typography.titleLarge.copy(
 *                 fontWeight = FontWeight.Bold
 *             ),
 *             color = Color(0xFF0F766E)
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun TealCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0xFF0F766E).copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDFA) // Nebula Teal Container
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/**
 * ErrorCard - Nova Red themed card component
 *
 * Used for error messages, failure states, and warning notifications.
 *
 * Design specifications:
 * - Background: Color(0xFFFEF2F2) - Nova Red Container
 * - Border: Color(0xFF991B1B).copy(alpha = 0.3f) - Nova Red Dark with transparency
 * - Text color: Color(0xFF991B1B) - Nova Red Dark
 * - Shape: MaterialTheme.shapes.medium (RoundedCornerShape(16.dp))
 * - Elevation: 2.dp
 * - Internal padding: 20.dp
 * - Content spacing: 12.dp vertical
 *
 * Common usage:
 * - Error messages
 * - Failure notifications
 * - Validation errors
 * - Operation failures
 *
 * @param modifier Modifier for the card (typically Modifier.fillMaxWidth())
 * @param content Composable lambda for card content with 20.dp padding and 12.dp vertical spacing
 *
 * Example usage:
 * ```kotlin
 * ErrorCard(modifier = Modifier.fillMaxWidth()) {
 *     Row(
 *         verticalAlignment = Alignment.CenterVertically,
 *         horizontalArrangement = Arrangement.spacedBy(12.dp)
 *     ) {
 *         Icon(
 *             imageVector = Icons.Default.Error,
 *             contentDescription = null,
 *             tint = Color(0xFF991B1B),
 *             modifier = Modifier.size(28.dp)
 *         )
 *         Text(
 *             text = "Error",
 *             style = MaterialTheme.typography.titleLarge.copy(
 *                 fontWeight = FontWeight.Bold
 *             ),
 *             color = Color(0xFF991B1B)
 *         )
 *     }
 *     Text(
 *         text = "Error message here...",
 *         style = MaterialTheme.typography.bodyMedium,
 *         color = Color(0xFF991B1B).copy(alpha = 0.9f)
 *     )
 * }
 * ```
 */
@Composable
fun ErrorCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0xFF991B1B).copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF2F2) // Nova Red Container
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/**
 * GreenCard - Success themed card component
 *
 * Used for successful operation results, particularly in token contract invocations.
 *
 * Design specifications:
 * - Background: Color(0xFFE8F5E9) - Light green
 * - Border: Color(0xFF2E7D32).copy(alpha = 0.2f) - Dark green with transparency
 * - Text color: Color(0xFF2E7D32) - Dark green
 * - Shape: MaterialTheme.shapes.medium (RoundedCornerShape(16.dp))
 * - Elevation: 2.dp
 * - Internal padding: 20.dp
 * - Content spacing: 12.dp vertical
 *
 * Common usage:
 * - Contract invocation results
 * - Read-only call success
 * - Operation completion messages
 *
 * @param modifier Modifier for the card (typically Modifier.fillMaxWidth())
 * @param content Composable lambda for card content with 20.dp padding and 12.dp vertical spacing
 *
 * Example usage:
 * ```kotlin
 * GreenCard(modifier = Modifier.fillMaxWidth()) {
 *     Text(
 *         text = "Function: transfer",
 *         style = MaterialTheme.typography.titleMedium.copy(
 *             fontWeight = FontWeight.Bold
 *         ),
 *         color = Color(0xFF2E7D32)
 *     )
 *     Text(
 *         text = "Result: Success",
 *         style = MaterialTheme.typography.bodyMedium,
 *         color = Color(0xFF2E7D32).copy(alpha = 0.8f)
 *     )
 * }
 * ```
 */
@Composable
fun GreenCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0xFF2E7D32).copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.medium
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9) // Light green
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}
