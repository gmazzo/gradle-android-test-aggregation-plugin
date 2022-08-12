package com.example.myapplication

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Test
    fun shouldStart() {
        Robolectric.buildActivity(MainActivity::class.java).use { controller ->
            controller.setup() // Moves Activity to RESUMED state
        }
    }

}
