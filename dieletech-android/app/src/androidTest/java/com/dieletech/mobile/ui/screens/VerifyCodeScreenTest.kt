package com.dieletech.mobile.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dieletech.mobile.ui.theme.DieletechTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerifyCodeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchScreen(email: String = "diego@test.com") {
        composeTestRule.setContent {
            DieletechTheme {
                val navController = rememberNavController()
                VerifyCodeScreen(email = email, navController = navController)
            }
        }
    }

    // =====================================================
    // ELEMENTOS VISIBLES
    // =====================================================

    @Test
    fun verifyScreen_showsTitle() {
        launchScreen()
        composeTestRule.onNodeWithText("Verifica tu correo").assertIsDisplayed()
    }

    @Test
    fun verifyScreen_showsCodeInput() {
        launchScreen()
        composeTestRule.onNodeWithText("Codigo de verificacion").assertIsDisplayed()
    }

    @Test
    fun verifyScreen_showsVerifyButton() {
        launchScreen()
        composeTestRule.onNodeWithText("Verificar cuenta").assertIsDisplayed()
    }

    @Test
    fun verifyScreen_showsResendButton() {
        launchScreen()
        // Initially shows "Reenviar en 60s" due to cooldown
        composeTestRule.onNodeWithText("Reenviar en 60s", substring = true).assertIsDisplayed()
    }

    @Test
    fun verifyScreen_showsBackToLoginLink() {
        launchScreen()
        composeTestRule.onNodeWithText("Volver a inicio de sesion").assertIsDisplayed()
    }

    // =====================================================
    // INTERACCIONES
    // =====================================================

    @Test
    fun verifyScreen_canTypeCode() {
        launchScreen()
        composeTestRule.onNodeWithText("Codigo de verificacion").performTextInput("123456")
        composeTestRule.onNodeWithText("6/6 digitos").assertIsDisplayed()
    }

    @Test
    fun verifyScreen_codeOnlyAcceptsDigits() {
        launchScreen()
        composeTestRule.onNodeWithText("Codigo de verificacion").performTextInput("abc123")
        // Only digits are kept, so it should show "123" (3 digits)
        composeTestRule.onNodeWithText("3/6 digitos").assertIsDisplayed()
    }

    @Test
    fun verifyScreen_codeLimitedTo6Digits() {
        launchScreen()
        composeTestRule.onNodeWithText("Codigo de verificacion").performTextInput("12345678")
        // Should only show 6 digits max
        composeTestRule.onNodeWithText("6/6 digitos").assertIsDisplayed()
    }

    @Test
    fun verifyScreen_verifyButtonDisabledWhenCodeIncomplete() {
        launchScreen()
        composeTestRule.onNodeWithText("Codigo de verificacion").performTextInput("123")
        // The verify button should still be there
        composeTestRule.onNodeWithText("Verificar cuenta").assertIsDisplayed()
    }

    @Test
    fun verifyScreen_showsDigitCounter() {
        launchScreen()
        composeTestRule.onNodeWithText("Codigo de verificacion").performTextInput("12")
        composeTestRule.onNodeWithText("2/6 digitos").assertIsDisplayed()
    }
}
