package com.soneso.demo.platform

import platform.Foundation.NSURL
import platform.AppKit.NSWorkspace

/**
 * macOS implementation of URL opener using NSWorkspace.shared.open.
 * Opens URLs in the default browser or appropriate application.
 */
private class MacosUrlOpener : UrlOpener {
    override suspend fun openUrl(url: String): Boolean {
        return try {
            val nsUrl = NSURL.URLWithString(url) ?: return false
            val workspace = NSWorkspace.sharedWorkspace

            // Use openURL to open the URL in the default application
            workspace.openURL(nsUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual fun getUrlOpener(): UrlOpener = MacosUrlOpener()
