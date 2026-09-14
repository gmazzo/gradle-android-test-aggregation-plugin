package com.example.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class KMPObjectAndroidTest {

    @Test
    fun testKMPObject() {
        assertEquals(PLATFORM, KMPObjectAndroid.platform)
    }

}
