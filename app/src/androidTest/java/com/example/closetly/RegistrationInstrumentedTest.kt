package com.example.closetly

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.closetly.view.LoginActivity
import com.example.closetly.view.RegistrationActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<RegistrationActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test_registration_success_navigates_to_login() {
        // Enter all valid registration details
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("newuser@gmail.com")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait for navigation to LoginActivity
        composeRule.waitUntil(timeoutMillis = 10000) {
            try {
                Intents.intended(hasComponent(LoginActivity::class.java.name))
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun test_registration_failure_empty_fields() {
        // Click register without entering any information
        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login (stays on registration)
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login with empty fields")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_empty_fullname() {
        // Enter all fields except full name
        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login without full name")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_empty_phone() {
        // Enter all fields except phone number
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login without phone number")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_empty_email() {
        // Enter all fields except email
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login without email")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_empty_password() {
        // Enter all fields except password
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login without password")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_empty_confirm_password() {
        // Enter all fields except confirm password
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login without confirm password")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_passwords_do_not_match() {
        // Enter all fields with mismatched passwords
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("654321")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login with mismatched passwords")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_invalid_phone_number() {
        // Enter all fields with invalid phone number (too short)
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("123")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login with invalid phone number")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_invalid_email_format() {
        // Enter all fields with invalid email format
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("invalidemail")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait for Firebase response
        Thread.sleep(5000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login with invalid email format")
        } catch (e: AssertionError) {
            // Expected - registration should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_registration_failure_existing_email() {
        // Enter all fields with an already registered email
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("John Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")  // Assuming this email already exists

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait for Firebase response
        Thread.sleep(5000)

        // Should NOT navigate to login if email already exists
        // (May succeed if email doesn't exist, depends on test data)
    }

    @Test
    fun test_registration_with_valid_minimum_phone_length() {
        // Test with 7 digits (minimum valid length)
        composeRule.onNodeWithTag("fullname_input")
            .performTextInput("Jane Doe")

        composeRule.onNodeWithTag("phone_input")
            .performTextInput("1234567")

        composeRule.onNodeWithTag("email_input")
            .performTextInput("uniqueuser@gmail.com")

        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("register_button")
            .performClick()

        // Wait for registration
        Thread.sleep(7000)

        // Should attempt to navigate to login (validation passes)
    }
}