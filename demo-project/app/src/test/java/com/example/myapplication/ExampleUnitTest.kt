package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun failing_test() {
        fail("this is a sample failure")
    }

    @Test
    @Ignore
    fun ignored_test() {
        fail("this is a sample failure")
    }
}
