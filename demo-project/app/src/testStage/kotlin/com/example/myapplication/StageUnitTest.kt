package com.example.myapplication

import androidx.fragment.app.testing.launchFragmentInContainer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class StageUnitTest {

    @Test
    fun shouldStart() {
        launchFragmentInContainer<SecondFragment>()
    }

}
