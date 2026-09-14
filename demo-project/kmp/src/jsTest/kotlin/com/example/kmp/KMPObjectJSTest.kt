package com.example.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class KMPObjectJSTest {

    @Test
    fun testKMPObject() {
        assertEquals(PLATFORM, KMPObjectJS.platform)
    }

}
