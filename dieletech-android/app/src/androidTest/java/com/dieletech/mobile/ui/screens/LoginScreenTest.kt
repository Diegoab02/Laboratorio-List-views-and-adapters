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
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchScreen() {
        composeTestRule.setContent {
            DieletechTheme {
                val navController = rememberNavController()
                LoginScreen(navController = navController)
            }
        }
    }

    // =====================================================
    // ELEMENTOS VISIBLES
    // =====================================================

    @Test
    fun loginScreen_showsTitle() {
        launchScreen()
        composeTestRule.onNodeWithText("Bienvenido de vuelta").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsEmailField() {
        launchScreen()
        composeTestRule.onNodeWithText("Correo electronico").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsPasswordField() {
        launchScreen()
        composeTestRule.onNodeWithText("Contrasena").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsLoginButton() {
        launchScreen()
        composeTestRule.onNodeWithText("Iniciar sesion").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsRegisterLink() {
        launchScreen()
        composeTestRule.onNodeWithText("Registrate gratis").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsExploreCatalogButton() {
        launchScreen()
        composeTestRule.onNodeWithText("Explorar catalogo sin cuenta").assertIsDisplayed()
    }

    // =====================================================
    // INTERACCIONES
    // =====================================================

    @Test
    fun loginScreen_canTypeEmail() {
        launchScreen()
        composeTestRule.onNodeWithText("Correo electronico").performTextInput("diego@test.com")
        composeTestRule.onNodeWithText("diego@test.com").assertIsDisplayed()
    }

    @Test
    fun loginScreen_canTypePassword() {
        launchScreen()
        composeTestRule.onNodeWithText("Contrasena").performTextInput("mypass")
        // Password field doesn't show text (it's masked), but we verify no crash
        composeTestRule.waitForIdle()
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    @Test
    fun loginScreen_showsEmailValidation_whenInvalid() {
        launchScreen()
        composeTestRule.onNodeWithText("Correo electronico").performTextInput("notanemail")
        composeTestRule.onNodeWithText("Ingresa un correo valido").assertIsDisplayed()
    }

    @Test
    fun loginScreen_submitWithEmptyEmail_showsError() {
        launchScreen()
        composeTestRule.onNodeWithText("Iniciar sesion").performClick()
        composeTestRule.waitForIdle()
        // Should show validation error
        composeTestRule.onNodeWithText("Ingresa un correo electronico valido").assertIsDisplayed()
    }

    @Test
    fun loginScreen_submitWithEmptyPassword_showsError() {
        launchScreen()
        composeTestRule.onNodeWithText("Correo electronico").performTextInput("diego@test.com")
        composeTestRule.onNodeWithText("Iniciar sesion").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ingresa tu contrasena").assertIsDisplayed()
    }
}
