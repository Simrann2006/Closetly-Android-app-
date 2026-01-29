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

class RegistrationUnitTest {

    @Test
    fun registration_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(true, "Registration successful", "user123")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())

        var successResult = false
        var messageResult = ""
        var userIdResult = ""

        viewModel.register("test@gmail.com", "123456", "John Doe", "9876543210") { success, msg, userId ->
            successResult = success
            messageResult = msg
            userIdResult = userId
        }

        assertTrue(successResult)
        assertEquals("Registration successful", messageResult)
        assertEquals("user123", userIdResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())
    }

    @Test
    fun registration_failure_email_already_exists_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "The email address is already in use by another account", "")
            null
        }.`when`(repo).register(eq("existing@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())

        var successResult = true
        var messageResult = ""
        var userIdResult = ""

        viewModel.register("existing@gmail.com", "123456", "John Doe", "9876543210") { success, msg, userId ->
            successResult = success
            messageResult = msg
            userIdResult = userId
        }

        assertFalse(successResult)
        assertEquals("The email address is already in use by another account", messageResult)
        assertEquals("", userIdResult)

        verify(repo).register(eq("existing@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())
    }

    @Test
    fun registration_failure_empty_email_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "Email cannot be empty", "")
            null
        }.`when`(repo).register(eq(""), eq("123456"), eq("John Doe"), eq("9876543210"), any())

        var successResult = true
        var messageResult = ""
        var userIdResult = ""

        viewModel.register("", "123456", "John Doe", "9876543210") { success, msg, userId ->
            successResult = success
            messageResult = msg
            userIdResult = userId
        }

        assertFalse(successResult)
        assertEquals("Email cannot be empty", messageResult)

        verify(repo).register(eq(""), eq("123456"), eq("John Doe"), eq("9876543210"), any())
    }

    @Test
    fun registration_failure_empty_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "Password cannot be empty", "")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq(""), eq("John Doe"), eq("9876543210"), any())

        var successResult = true
        var messageResult = ""

        viewModel.register("test@gmail.com", "", "John Doe", "9876543210") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Password cannot be empty", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq(""), eq("John Doe"), eq("9876543210"), any())
    }

    @Test
    fun registration_failure_empty_fullname_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "Full name cannot be empty", "")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq(""), eq("9876543210"), any())

        var successResult = true
        var messageResult = ""

        viewModel.register("test@gmail.com", "123456", "", "9876543210") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Full name cannot be empty", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq(""), eq("9876543210"), any())
    }

    @Test
    fun registration_failure_empty_phone_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "Phone number cannot be empty", "")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq(""), any())

        var successResult = true
        var messageResult = ""

        viewModel.register("test@gmail.com", "123456", "John Doe", "") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Phone number cannot be empty", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq(""), any())
    }

    @Test
    fun registration_failure_invalid_email_format_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "The email address is badly formatted", "")
            null
        }.`when`(repo).register(eq("invalidemail"), eq("123456"), eq("John Doe"), eq("9876543210"), any())

        var successResult = true
        var messageResult = ""

        viewModel.register("invalidemail", "123456", "John Doe", "9876543210") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("The email address is badly formatted", messageResult)

        verify(repo).register(eq("invalidemail"), eq("123456"), eq("John Doe"), eq("9876543210"), any())
    }

    @Test
    fun registration_failure_weak_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "Password should be at least 6 characters", "")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123"), eq("John Doe"), eq("9876543210"), any())

        var successResult = true
        var messageResult = ""

        viewModel.register("test@gmail.com", "123", "John Doe", "9876543210") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Password should be at least 6 characters", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq("123"), eq("John Doe"), eq("9876543210"), any())
    }

    @Test
    fun registration_failure_invalid_phone_number_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "Please enter a valid phone number", "")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("123"), any())

        var successResult = true
        var messageResult = ""

        viewModel.register("test@gmail.com", "123456", "John Doe", "123") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Please enter a valid phone number", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("123"), any())
    }

    @Test
    fun registration_failure_network_error_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(false, "Network error. Please try again", "")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())

        var successResult = true
        var messageResult = ""

        viewModel.register("test@gmail.com", "123456", "John Doe", "9876543210") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Network error. Please try again", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())
    }

    @Test
    fun registration_with_valid_phone_number_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(true, "Registration successful", "user456")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq("Jane Doe"), eq("1234567890"), any())

        var successResult = false
        var messageResult = ""
        var userIdResult = ""

        viewModel.register("test@gmail.com", "123456", "Jane Doe", "1234567890") { success, msg, userId ->
            successResult = success
            messageResult = msg
            userIdResult = userId
        }

        assertTrue(successResult)
        assertEquals("Registration successful", messageResult)
        assertEquals("user456", userIdResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq("Jane Doe"), eq("1234567890"), any())
    }

    @Test
    fun registration_with_minimum_valid_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(4)
            callback(true, "Registration successful", "user789")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())

        var successResult = false
        var messageResult = ""

        viewModel.register("test@gmail.com", "123456", "John Doe", "9876543210") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Registration successful", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq("John Doe"), eq("9876543210"), any())
    }
}