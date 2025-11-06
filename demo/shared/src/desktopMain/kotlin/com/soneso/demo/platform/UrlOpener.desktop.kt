package com.soneso.demo.platform

import java.awt.Desktop
import java.net.URI

/**
 * Desktop/JVM implementation of URL opener using java.awt.Desktop.
 * Works on Windows, macOS, and Linux.
 */
private class DesktopUrlOpener : UrlOpener {
    override suspend fun openUrl(url: String): Boolean {
        return try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI(url))
                    true
                } else {
                    // Fallback to ProcessBuilder for systems without Desktop support
                    openUrlWithProcessBuilder(url)
                }
            } else {
                // Fallback to ProcessBuilder
                openUrlWithProcessBuilder(url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun openUrlWithProcessBuilder(url: String): Boolean {
        return try {
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                os.contains("win") -> listOf("cmd", "/c", "start", url)
                os.contains("mac") -> listOf("open", url)
                else -> listOf("xdg-open", url) // Linux
            }
            ProcessBuilder(command).start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual fun getUrlOpener(): UrlOpener = DesktopUrlOpener()
