package com.soneso.demo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.soneso.demo.DemoVersion
import com.soneso.demo.platform.getClipboard
import com.soneso.demo.platform.getUrlOpener
import com.soneso.demo.ui.components.StellarTopBar
import com.soneso.demo.ui.components.InfoCard
import com.soneso.demo.ui.components.BlueCard
import com.soneso.demo.ui.components.GoldCard
import com.soneso.demo.ui.components.TealCard
import kotlinx.coroutines.launch

class InfoScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val urlOpener = remember { getUrlOpener() }

        var snackbarMessage by remember { mutableStateOf<String?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }

        // Show snackbar when message changes
        LaunchedEffect(snackbarMessage) {
            snackbarMessage?.let { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                snackbarMessage = null
            }
        }

        Scaffold(
            topBar = {
                StellarTopBar(
                    title = "Info",
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
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // App version and description
                    InfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "KMP Stellar SDK Demo",
                        description = "Version ${DemoVersion.VERSION}"
                    )

                    // About the SDK
                    BlueCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color(0xFF0A4FD6),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "About This App",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF0639A3)
                            )
                        }

                        HorizontalDivider(color = Color(0xFF0A4FD6).copy(alpha = 0.2f))

                        Text(
                            text = "This application was built with the Kotlin Multiplatform Stellar SDK (kmp-stellar-sdk). " +
                                    "It demonstrates comprehensive SDK functionality across all supported platforms: " +
                                    "Android, iOS, macOS, Desktop (JVM), and Web.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0639A3).copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )

                        Text(
                            text = "All features interact with the Stellar live testnet, providing real-world examples of " +
                                    "blockchain operations without using real funds.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0639A3).copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )
                    }

                    // GitHub repository
                    GoldCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "GitHub Repository",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFA85A00)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFD97706).copy(alpha = 0.3f))

                        Text(
                            text = "SDK Repository",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFA85A00).copy(alpha = 0.7f)
                        )

                        // Clickable GitHub URL
                        SelectionContainer {
                            Text(
                                text = "https://github.com/Soneso/kmp-stellar-sdk",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = TextDecoration.Underline
                                ),
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF0A4FD6), // Blue color to indicate link
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            val success = urlOpener.openUrl("https://github.com/Soneso/kmp-stellar-sdk")
                                            if (!success) {
                                                snackbarMessage = "Failed to open URL"
                                            }
                                        }
                                    }
                                    .pointerHoverIcon(PointerIcon.Hand),
                                lineHeight = 24.sp
                            )
                        }

                        Text(
                            text = "This demo app is part of the SDK repository.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA85A00).copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                    }

                    // Star on GitHub
                    TealCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Support the Project",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF0F766E)
                            )
                        }

                        HorizontalDivider(color = Color(0xFF0F766E).copy(alpha = 0.3f))

                        Text(
                            text = "If you find the Kotlin Multiplatform Stellar SDK useful, please consider starring " +
                                    "the repository on GitHub. Your support helps with maintenance and development, " +
                                    "and helps other developers discover this SDK.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0F766E).copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )

                        Text(
                            text = "Every star counts and motivates continued improvement of the SDK.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF0F766E).copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }

                    // Feedback
                    BlueCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF0A4FD6),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Feedback",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF0639A3)
                            )
                        }

                        HorizontalDivider(color = Color(0xFF0A4FD6).copy(alpha = 0.2f))

                        Text(
                            text = "We value your feedback and suggestions. If you have questions, feature requests, " +
                                    "or encounter any issues, please reach out to us.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0639A3).copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Contact:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF0639A3).copy(alpha = 0.7f)
                            )

                            // Clickable email address
                            SelectionContainer {
                                Text(
                                    text = "info@soneso.com",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF0A4FD6), // Blue color to indicate link
                                    modifier = Modifier
                                        .clickable {
                                            coroutineScope.launch {
                                                val success = urlOpener.openUrl("mailto:info@soneso.com")
                                                if (!success) {
                                                    snackbarMessage = "Failed to open email client"
                                                }
                                            }
                                        }
                                        .pointerHoverIcon(PointerIcon.Hand)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
