package com.example.myapplication

import androidx.test.core.app.launchActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class MainActivityTest {

    @Test
    fun shouldStart() {
        launchActivity<MainActivity>()
    }

}
