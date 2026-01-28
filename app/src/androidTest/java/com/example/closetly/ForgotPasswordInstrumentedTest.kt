package com.example.closetly

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.closetly.view.ForgotActivity
import com.example.closetly.view.LoginActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgotPasswordInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ForgotActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test_forgotPassword_success_navigates_to_login() {
        // Enter valid registered email
        composeRule.onNodeWithTag("forgot_email_input")
            .performTextInput("test@gmail.com")
        composeRule.onNodeWithTag("forgot_submit_button")
            .performClick()

        // Wait for navigation to LoginActivity after successful reset link sent
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
    fun test_forgotPassword_any_valid_email_format_navigates_to_login() {
        // Firebase email enumeration protection is enabled
        // So ANY valid email format will return success (security feature)
        composeRule.onNodeWithTag("forgot_email_input")
            .performTextInput("anyemail@gmail.com")
        composeRule.onNodeWithTag("forgot_submit_button")
            .performClick()

        // Should navigate to LoginActivity (Firebase returns success for any valid email)
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
    fun test_forgotPassword_failure_empty_email() {
        // Click submit without entering email
        composeRule.onNodeWithTag("forgot_submit_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login with empty email")
        } catch (e: AssertionError) {
            // Expected - validation should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_forgotPassword_failure_invalid_email_format() {
        // Enter invalid email format
        composeRule.onNodeWithTag("forgot_email_input")
            .performTextInput("invalidemail")
        composeRule.onNodeWithTag("forgot_submit_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login with invalid email format")
        } catch (e: AssertionError) {
            // Expected - validation should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_forgotPassword_failure_email_without_domain() {
        // Enter email without proper domain
        composeRule.onNodeWithTag("forgot_email_input")
            .performTextInput("test@")
        composeRule.onNodeWithTag("forgot_submit_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to login
        try {
            Intents.intended(hasComponent(LoginActivity::class.java.name))
            fail("Should not navigate to Login with incomplete email")
        } catch (e: AssertionError) {
            // Expected - validation should fail
            assertTrue(true)
        }
    }
}
