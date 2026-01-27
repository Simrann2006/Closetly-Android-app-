package com.example.closetly

import com.example.closetly.repository.UserRepo
import com.example.closetly.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LoginUnitTest {

    @Test
    fun login_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login success")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.login("test@gmail.com", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Login success", messageResult)

        verify(repo).login(eq("test@gmail.com"), eq("123456"), any())
    }

    @Test
    fun login_failure_invalid_credentials_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Invalid email or password")
            null
        }.`when`(repo).login(eq("wrong@gmail.com"), eq("wrongpass"), any())

        var successResult = true
        var messageResult = ""

        viewModel.login("wrong@gmail.com", "wrongpass") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Invalid email or password", messageResult)

        verify(repo).login(eq("wrong@gmail.com"), eq("wrongpass"), any())
    }

    @Test
    fun login_failure_empty_email_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Email cannot be empty")
            null
        }.`when`(repo).login(eq(""), eq("123456"), any())

        var successResult = true
        var messageResult = ""

        viewModel.login("", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Email cannot be empty", messageResult)

        verify(repo).login(eq(""), eq("123456"), any())
    }

    @Test
    fun login_failure_empty_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Password cannot be empty")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq(""), any())

        var successResult = true
        var messageResult = ""

        viewModel.login("test@gmail.com", "") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Password cannot be empty", messageResult)

        verify(repo).login(eq("test@gmail.com"), eq(""), any())
    }

    @Test
    fun login_failure_invalid_email_format_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Invalid email format")
            null
        }.`when`(repo).login(eq("invalidemail"), eq("123456"), any())

        var successResult = true
        var messageResult = ""

        viewModel.login("invalidemail", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Invalid email format", messageResult)

        verify(repo).login(eq("invalidemail"), eq("123456"), any())
    }
}