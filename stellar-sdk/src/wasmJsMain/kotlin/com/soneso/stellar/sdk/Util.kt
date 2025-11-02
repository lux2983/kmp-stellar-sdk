package com.soneso.stellar.sdk

/**
 * WebAssembly/JS implementation of currentTimeMillis using Date.now().
 */
@JsFun("() => Date.now()")
private external fun dateNow(): Double

internal actual fun currentTimeMillis(): Long = dateNow().toLong()
