package com.soneso.demo.platform

/**
 * Platform-agnostic URL opener interface for opening URLs in the default browser or email client.
 * Implementations use platform-specific URL opening APIs:
 * - Android: Intent with ACTION_VIEW
 * - iOS: UIApplication.shared.open
 * - macOS: NSWorkspace.shared.open
 * - Desktop/JVM: Desktop.browse() or ProcessBuilder
 * - Web/JS: window.open()
 */
interface UrlOpener {
    /**
     * Open a URL in the default browser or application.
     *
     * @param url The URL to open (http://, https://, mailto:, etc.)
     * @return true if the operation succeeded, false otherwise
     */
    suspend fun openUrl(url: String): Boolean
}

/**
 * Get the platform-specific URL opener implementation.
 */
expect fun getUrlOpener(): UrlOpener
