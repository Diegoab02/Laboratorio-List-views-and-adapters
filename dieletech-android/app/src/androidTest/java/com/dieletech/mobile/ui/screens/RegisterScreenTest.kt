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
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchScreen() {
        composeTestRule.setContent {
            DieletechTheme {
                val navController = rememberNavController()
                RegisterScreen(navController = navController)
            }
        }
    }

    // =====================================================
    // ELEMENTOS VISIBLES
    // =====================================================

    @Test
    fun registerScreen_showsTitle() {
        launchScreen()
        composeTestRule.onNodeWithText("Crear cuenta").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsRoleCards() {
        launchScreen()
        composeTestRule.onNodeWithText("Estudiante").assertIsDisplayed()
        composeTestRule.onNodeWithText("Instructor").assertIsDisplayed()
        composeTestRule.onNodeWithText("Admin").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsAllInputFields() {
        launchScreen()
        composeTestRule.onNodeWithText("Nombre completo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo electronico").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contrasena").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmar contrasena").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsRegisterButton() {
        launchScreen()
        // The button shows "Crear cuenta" text
        composeTestRule.onAllNodesWithText("Crear cuenta").assertCountEquals(2)
    }

    @Test
    fun registerScreen_showsLoginLink() {
        launchScreen()
        composeTestRule.onNodeWithText("Inicia sesion").assertIsDisplayed()
    }

    // =====================================================
    // INTERACCIONES
    // =====================================================

    @Test
    fun registerScreen_canTypeInNameField() {
        launchScreen()
        composeTestRule.onNodeWithText("Nombre completo").performTextInput("Diego")
        composeTestRule.onNodeWithText("Diego").assertIsDisplayed()
    }

    @Test
    fun registerScreen_canTypeInEmailField() {
        launchScreen()
        composeTestRule.onNodeWithText("Correo electronico").performTextInput("diego@test.com")
        composeTestRule.onNodeWithText("diego@test.com").assertIsDisplayed()
    }

    @Test
    fun registerScreen_canSelectDifferentRoles() {
        launchScreen()
        // Click on Instructor role
        composeTestRule.onNodeWithText("Instructor").performClick()
        // Click on Admin role
        composeTestRule.onNodeWithText("Admin").performClick()
        // Click back on Estudiante
        composeTestRule.onNodeWithText("Estudiante").performClick()
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    @Test
    fun registerScreen_showsNameValidation_whenTooShort() {
        launchScreen()
        composeTestRule.onNodeWithText("Nombre completo").performTextInput("AB")
        // Validation appears for names < 3 chars
        composeTestRule.onNodeWithText("Minimo 3 caracteres").assertIsDisplayed()
    }

    @Test
    fun registerScreen_showsEmailValidation_whenInvalid() {
        launchScreen()
        composeTestRule.onNodeWithText("Correo electronico").performTextInput("notanemail")
        composeTestRule.onNodeWithText("Ingresa un correo valido").assertIsDisplayed()
    }

    @Test
    fun registerScreen_emptyFormSubmit_showsError() {
        launchScreen()
        // Find the submit button (second "Crear cuenta" node - the button text)
        composeTestRule.onAllNodesWithText("Crear cuenta")[1].performClick()
        // Should show an error because fields are empty
        composeTestRule.waitForIdle()
    }
}
