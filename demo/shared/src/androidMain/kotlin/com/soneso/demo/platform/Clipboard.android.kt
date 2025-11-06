package com.soneso.demo.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Android implementation of clipboard using ClipboardManager.
 * Requires a Context to access the system clipboard service.
 */
private class AndroidClipboard(private val context: Context) : Clipboard {
    override suspend fun copyToClipboard(text: String): Boolean {
        return try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboardManager != null) {
                val clip = ClipData.newPlainText("text", text)
                clipboardManager.setPrimaryClip(clip)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * Initialize the clipboard with Android application context.
 * Should be called from Application.onCreate() or Activity.onCreate().
 */
fun initAndroidClipboard(context: Context) {
    AndroidContext.init(context)
}

actual fun getClipboard(): Clipboard {
    return AndroidClipboard(AndroidContext.get())
}
