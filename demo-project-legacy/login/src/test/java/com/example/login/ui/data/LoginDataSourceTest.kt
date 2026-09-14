package com.example.login.ui.data

import org.junit.Assert.assertNotNull
import org.junit.Test

class LoginDataSourceTest {

    @Test
    fun testLogin() {
        val result = LoginDataSource().login("user", "pass") as? Result.Success

        assertNotNull(result?.data?.userId)
    }

}
