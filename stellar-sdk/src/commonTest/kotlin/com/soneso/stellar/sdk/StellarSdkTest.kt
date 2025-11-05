package com.soneso.stellar.sdk

import kotlin.test.Test
import kotlin.test.assertEquals

class StellarSdkTest {
    @Test
    fun testVersion() {
        assertEquals("0.2.1", StellarSdk.VERSION)
    }
}