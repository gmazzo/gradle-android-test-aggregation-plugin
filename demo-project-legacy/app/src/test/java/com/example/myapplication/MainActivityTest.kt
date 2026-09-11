package com.example.myapplication

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class MainActivityTest {

    @Test
    fun shouldStart() {
        Robolectric.buildActivity(MainActivity::class.java).use { controller ->
            controller.setup() // Moves Activity to RESUMED state
        }
    }

}
