package com.soneso.demo.platform

import kotlinx.browser.window

/**
 * JavaScript implementation of URL opener using window.open.
 * Opens URLs in a new browser tab or window.
 */
private class JSUrlOpener : UrlOpener {
    override suspend fun openUrl(url: String): Boolean {
        return try {
            // Open URL in a new tab (_blank)
            // Use noreferrer and noopener for security
            val opened = window.open(url, "_blank", "noreferrer,noopener")
            // window.open returns null if blocked by popup blocker
            opened != null
        } catch (e: Exception) {
            console.error("URL opening error:", e)
            false
        }
    }
}

actual fun getUrlOpener(): UrlOpener = JSUrlOpener()
