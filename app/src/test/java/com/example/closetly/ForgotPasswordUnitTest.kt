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

class ForgotPasswordUnitTest {

    @Test
    fun forgotPassword_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Link sent to test@gmail.com")
            null
        }.`when`(repo).forgotPassword(eq("test@gmail.com"), any())

        var successResult = false
        var messageResult = ""

        viewModel.forgotPassword("test@gmail.com") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Link sent to test@gmail.com", messageResult)

        verify(repo).forgotPassword(eq("test@gmail.com"), any())
    }

    @Test
    fun forgotPassword_failure_email_not_found_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "There is no user record corresponding to this identifier")
            null
        }.`when`(repo).forgotPassword(eq("notexist@gmail.com"), any())

        var successResult = true
        var messageResult = ""

        viewModel.forgotPassword("notexist@gmail.com") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("There is no user record corresponding to this identifier", messageResult)

        verify(repo).forgotPassword(eq("notexist@gmail.com"), any())
    }

    @Test
    fun forgotPassword_failure_empty_email_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Email cannot be empty")
            null
        }.`when`(repo).forgotPassword(eq(""), any())

        var successResult = true
        var messageResult = ""

        viewModel.forgotPassword("") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Email cannot be empty", messageResult)

        verify(repo).forgotPassword(eq(""), any())
    }

    @Test
    fun forgotPassword_failure_invalid_email_format_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "The email address is badly formatted")
            null
        }.`when`(repo).forgotPassword(eq("invalidemail"), any())

        var successResult = true
        var messageResult = ""

        viewModel.forgotPassword("invalidemail") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("The email address is badly formatted", messageResult)

        verify(repo).forgotPassword(eq("invalidemail"), any())
    }
}
