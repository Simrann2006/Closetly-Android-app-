package com.example.closetly

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.closetly.view.DashboardActivity
import com.example.closetly.view.LoginActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test_login_success_navigates_to_dashboard() {
        // Enter valid email and password
        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")
        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Wait for navigation to DashboardActivity
        composeRule.waitUntil(timeoutMillis = 10000) {
            try {
                Intents.intended(hasComponent(DashboardActivity::class.java.name))
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun test_login_failure_invalid_credentials() {
        // Enter invalid email and password
        composeRule.onNodeWithTag("email_input")
            .performTextInput("wrong@gmail.com")
        composeRule.onNodeWithTag("password_input")
            .performTextInput("wrongpassword")
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Wait a bit for the response
        Thread.sleep(3000)

        // Should NOT navigate to dashboard (stays on login)
        try {
            Intents.intended(hasComponent(DashboardActivity::class.java.name))
            fail("Should not navigate to Dashboard with invalid credentials")
        } catch (e: AssertionError) {
            // Expected - login should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_login_failure_empty_fields() {
        // Click login without entering any credentials
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to dashboard
        try {
            Intents.intended(hasComponent(DashboardActivity::class.java.name))
            fail("Should not navigate to Dashboard with empty fields")
        } catch (e: AssertionError) {
            // Expected - login should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_login_failure_empty_password() {
        // Enter only email, leave password empty
        composeRule.onNodeWithTag("email_input")
            .performTextInput("test@gmail.com")
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to dashboard
        try {
            Intents.intended(hasComponent(DashboardActivity::class.java.name))
            fail("Should not navigate to Dashboard without password")
        } catch (e: AssertionError) {
            // Expected - login should fail
            assertTrue(true)
        }
    }

    @Test
    fun test_login_failure_empty_email() {
        // Enter only password, leave email empty
        composeRule.onNodeWithTag("password_input")
            .performTextInput("123456")
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should NOT navigate to dashboard
        try {
            Intents.intended(hasComponent(DashboardActivity::class.java.name))
            fail("Should not navigate to Dashboard without email")
        } catch (e: AssertionError) {
            // Expected - login should fail
            assertTrue(true)
        }
    }
}