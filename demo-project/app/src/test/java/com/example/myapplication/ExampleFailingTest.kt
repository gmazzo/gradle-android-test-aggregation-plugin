package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleFailingTest private constructor(){
    @Test
    fun test() {
        assertEquals(4, 2 + 2)
    }
}
