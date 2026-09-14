package com.example.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class KMPObjectJVMTest {

    @Test
    fun testKMPObject() {
        assertEquals(PLATFORM, KMPObjectJVM.platform)
    }

}
