package com.soneso.demo.platform

import android.content.Context

/**
 * Shared Android context holder for platform-specific features.
 * This context is initialized once in MainActivity and used by clipboard, URL opener, etc.
 */
internal object AndroidContext {
    private var context: Context? = null

    /**
     * Initialize the Android context.
     * Should be called from Application.onCreate() or Activity.onCreate().
     */
    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    /**
     * Get the application context.
     * Throws IllegalStateException if not initialized.
     */
    fun get(): Context {
        return context ?: throw IllegalStateException(
            "Android context not initialized. Call AndroidContext.init(context) first."
        )
    }

    /**
     * Get the application context, or null if not initialized.
     */
    fun getOrNull(): Context? = context
}
