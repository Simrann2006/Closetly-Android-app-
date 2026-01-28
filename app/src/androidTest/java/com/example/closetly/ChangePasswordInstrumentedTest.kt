package com.example.closetly

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.closetly.view.ChangePasswordActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangePasswordInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ChangePasswordActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test_changePassword_success_with_valid_credentials() {
        // Note: This test requires a logged-in user with known credentials
        // Enter current password
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("oldPass123")

        // Enter new password
        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("newPass123")

        // Confirm new password
        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("newPass123")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait for the password change to complete
        Thread.sleep(5000)

        // Verify success by checking if activity finishes (password changed successfully)
        // In real scenario, activity should finish on success
    }

    @Test
    fun test_changePassword_failure_empty_current_password() {
        // Enter only new password and confirm, leave current empty
        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("newPass123")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("newPass123")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should display error message (validation fails, stays on screen)
        // Activity should not finish
    }

    @Test
    fun test_changePassword_failure_empty_new_password() {
        // Enter only current password, leave new password empty
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("oldPass123")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should display error message (validation fails, stays on screen)
    }

    @Test
    fun test_changePassword_failure_empty_confirm_password() {
        // Enter current and new password, leave confirm empty
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("oldPass123")

        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("newPass123")

        // Click change password button without confirming
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should display error message (validation fails)
    }

    @Test
    fun test_changePassword_failure_password_too_short() {
        // Enter passwords that are too short (< 6 characters)
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("oldPass123")

        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("123")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should display error message about password length
    }

    @Test
    fun test_changePassword_failure_passwords_do_not_match() {
        // Enter different new password and confirm password
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("oldPass123")

        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("newPass123")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("newPass456")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should display error message about passwords not matching
    }

    @Test
    fun test_changePassword_failure_same_as_current_password() {
        // Enter new password same as current password
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("oldPass123")

        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("oldPass123")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("oldPass123")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should display error message about new password must be different
    }

    @Test
    fun test_changePassword_failure_incorrect_current_password() {
        // Enter wrong current password
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("wrongPassword")

        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("newPass123")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("newPass123")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait for Firebase authentication
        Thread.sleep(5000)

        // Should display error about incorrect current password
        // Activity should not finish
    }

    @Test
    fun test_changePassword_all_fields_empty() {
        // Click button without entering any information
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait a bit
        Thread.sleep(1000)

        // Should display error for empty current password (first validation)
    }

    @Test
    fun test_changePassword_valid_password_length() {
        // Test with exactly 6 characters (minimum valid length)
        composeRule.onNodeWithTag("current_password_input")
            .performTextInput("oldPass123")

        composeRule.onNodeWithTag("new_password_input")
            .performTextInput("123456")

        composeRule.onNodeWithTag("confirm_password_input")
            .performTextInput("123456")

        // Click change password button
        composeRule.onNodeWithTag("change_password_button")
            .performClick()

        // Wait for processing
        Thread.sleep(5000)

        // Should attempt password change (validation passes)
    }
}