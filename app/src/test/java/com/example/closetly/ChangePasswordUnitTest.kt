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

class ChangePasswordUnitTest {

    @Test
    fun changePassword_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(true, "Password changed successfully!")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass123"), any())

        var successResult = false
        var messageResult = ""

        viewModel.changePassword("currentPass123", "newPass123", "newPass123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Password changed successfully!", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass123"), any())
    }

    @Test
    fun changePassword_failure_empty_current_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "Please enter current password")
            null
        }.`when`(repo).changePassword(eq(""), eq("newPass123"), eq("newPass123"), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("", "newPass123", "newPass123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Please enter current password", messageResult)

        verify(repo).changePassword(eq(""), eq("newPass123"), eq("newPass123"), any())
    }

    @Test
    fun changePassword_failure_empty_new_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "Please enter new password")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq(""), eq(""), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("currentPass123", "", "") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Please enter new password", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq(""), eq(""), any())
    }

    @Test
    fun changePassword_failure_empty_confirm_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "Please confirm your password")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq(""), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("currentPass123", "newPass123", "") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Please confirm your password", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq(""), any())
    }

    @Test
    fun changePassword_failure_password_too_short_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "Password must be at least 6 characters")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq("123"), eq("123"), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("currentPass123", "123", "123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Password must be at least 6 characters", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq("123"), eq("123"), any())
    }

    @Test
    fun changePassword_failure_passwords_do_not_match_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "Passwords do not match")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass456"), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("currentPass123", "newPass123", "newPass456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Passwords do not match", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass456"), any())
    }

    @Test
    fun changePassword_failure_same_as_current_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "New password must be different")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq("currentPass123"), eq("currentPass123"), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("currentPass123", "currentPass123", "currentPass123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("New password must be different", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq("currentPass123"), eq("currentPass123"), any())
    }

    @Test
    fun changePassword_failure_incorrect_current_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "Current password is incorrect")
            null
        }.`when`(repo).changePassword(eq("wrongPass"), eq("newPass123"), eq("newPass123"), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("wrongPass", "newPass123", "newPass123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Current password is incorrect", messageResult)

        verify(repo).changePassword(eq("wrongPass"), eq("newPass123"), eq("newPass123"), any())
    }

    @Test
    fun changePassword_failure_user_not_found_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "User not found. Please login again.")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass123"), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("currentPass123", "newPass123", "newPass123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("User not found. Please login again.", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass123"), any())
    }

    @Test
    fun changePassword_failure_network_error_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(3)
            callback(false, "Failed to update password: Network error")
            null
        }.`when`(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass123"), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("currentPass123", "newPass123", "newPass123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Failed to update password: Network error", messageResult)

        verify(repo).changePassword(eq("currentPass123"), eq("newPass123"), eq("newPass123"), any())
    }
}