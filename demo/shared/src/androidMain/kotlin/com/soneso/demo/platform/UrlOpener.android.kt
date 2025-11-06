package com.soneso.demo.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Android implementation of URL opener using Intent with ACTION_VIEW.
 * Requires a Context to launch the intent.
 */
private class AndroidUrlOpener(private val context: Context) : UrlOpener {
    override suspend fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual fun getUrlOpener(): UrlOpener {
    return AndroidUrlOpener(AndroidContext.get())
}
