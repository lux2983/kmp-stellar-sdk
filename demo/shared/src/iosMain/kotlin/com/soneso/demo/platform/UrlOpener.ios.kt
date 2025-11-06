package com.soneso.demo.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

/**
 * iOS implementation of URL opener using UIApplication.shared.open.
 * Opens URLs in Safari or the appropriate app based on the URL scheme.
 */
private class IosUrlOpener : UrlOpener {
    override suspend fun openUrl(url: String): Boolean {
        return try {
            val nsUrl = NSURL.URLWithString(url) ?: return false
            val application = UIApplication.sharedApplication

            // Check if the URL can be opened
            if (!application.canOpenURL(nsUrl)) {
                return false
            }

            // Use the modern async API with completion handler
            suspendCancellableCoroutine { continuation ->
                application.openURL(
                    nsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { success ->
                        continuation.resume(success)
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual fun getUrlOpener(): UrlOpener = IosUrlOpener()
