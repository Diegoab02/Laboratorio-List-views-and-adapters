package com.dieletech.backend.controller

import com.dieletech.backend.dto.AuthResponse
import com.dieletech.backend.service.AuthService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
@DisplayName("AuthController - Tests de Integracion")
class AuthControllerTest {

    @Mock
    private lateinit var authService: AuthService

    private lateinit var mockMvc: MockMvc

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObj(): T = any<T>() as T

    @BeforeEach
    fun setup() {
        val controller = AuthController(authService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    // =====================================================
    // POST /api/auth/register
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/register")
    inner class RegisterEndpoint {

        @Test
        @DisplayName("Registro exitoso retorna 200 con mensaje")
        fun registerSuccess() {
            `when`(authService.register(anyObj())).thenReturn("Registro exitoso. Codigo de verificacion enviado.")

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Diego","email":"diego@test.com","password":"123456","role":"STUDENT"}""")
            )
                .andExpect(status().isOk)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("verificacion")))
        }

        @Test
        @DisplayName("Registro con email duplicado retorna 400")
        fun registerDuplicateEmail() {
            `when`(authService.register(anyObj())).thenThrow(RuntimeException("El correo ya esta registrado"))

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Diego","email":"diego@test.com","password":"123456"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("registrado")))
        }

        @Test
        @DisplayName("Registro con rol INSTRUCTOR")
        fun registerWithInstructorRole() {
            `when`(authService.register(anyObj())).thenReturn("Registro exitoso.")

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Prof Ana","email":"ana@test.com","password":"abc123","role":"INSTRUCTOR"}""")
            )
                .andExpect(status().isOk)
        }
    }

    // =====================================================
    // POST /api/auth/verify-code
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/verify-code")
    inner class VerifyCodeEndpoint {

        @Test
        @DisplayName("Verificacion exitosa retorna 200")
        fun verifyCodeSuccess() {
            `when`(authService.verifyCode(anyString(), anyString()))
                .thenReturn("Cuenta verificada exitosamente.")

            mockMvc.perform(
                post("/api/auth/verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com","code":"123456"}""")
            )
                .andExpect(status().isOk)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("verificada")))
        }

        @Test
        @DisplayName("Codigo incorrecto retorna 400")
        fun verifyCodeWrong() {
            `when`(authService.verifyCode(anyString(), anyString()))
                .thenThrow(RuntimeException("Codigo incorrecto"))

            mockMvc.perform(
                post("/api/auth/verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com","code":"000000"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("incorrecto")))
        }

        @Test
        @DisplayName("Sin email en body retorna 400")
        fun verifyCodeMissingEmail() {
            mockMvc.perform(
                post("/api/auth/verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"code":"123456"}""")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Sin codigo en body retorna 400")
        fun verifyCodeMissingCode() {
            mockMvc.perform(
                post("/api/auth/verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com"}""")
            )
                .andExpect(status().isBadRequest)
        }
    }

    // =====================================================
    // POST /api/auth/resend-code
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/resend-code")
    inner class ResendCodeEndpoint {

        @Test
        @DisplayName("Reenvio exitoso retorna 200")
        fun resendSuccess() {
            `when`(authService.resendVerificationCode(anyString()))
                .thenReturn("Nuevo codigo enviado a tu correo.")

            mockMvc.perform(
                post("/api/auth/resend-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com"}""")
            )
                .andExpect(status().isOk)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("codigo")))
        }

        @Test
        @DisplayName("Email no encontrado retorna 400")
        fun resendEmailNotFound() {
            `when`(authService.resendVerificationCode(anyString()))
                .thenThrow(RuntimeException("No se encontro una cuenta con ese correo"))

            mockMvc.perform(
                post("/api/auth/resend-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"ghost@test.com"}""")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Sin email en body retorna 400")
        fun resendMissingEmail() {
            mockMvc.perform(
                post("/api/auth/resend-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{}""")
            )
                .andExpect(status().isBadRequest)
        }
    }

    // =====================================================
    // POST /api/auth/login
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/login")
    inner class LoginEndpoint {

        @Test
        @DisplayName("Login exitoso retorna 200 con token y datos")
        fun loginSuccess() {
            val response = AuthResponse(
                token = "jwt_token_abc",
                name = "Diego",
                email = "diego@test.com",
                role = "STUDENT"
            )
            `when`(authService.login(anyObj())).thenReturn(response)

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com","password":"123456"}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").value("jwt_token_abc"))
                .andExpect(jsonPath("$.name").value("Diego"))
                .andExpect(jsonPath("$.email").value("diego@test.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
        }

        @Test
        @DisplayName("Login con credenciales incorrectas retorna 400")
        fun loginWrongCredentials() {
            `when`(authService.login(anyObj())).thenThrow(RuntimeException("Credenciales incorrectas"))

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com","password":"wrong"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("incorrectas")))
        }

        @Test
        @DisplayName("Login con usuario no verificado retorna 400")
        fun loginNotVerified() {
            `when`(authService.login(anyObj()))
                .thenThrow(RuntimeException("Debes verificar tu correo antes de iniciar sesion"))

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com","password":"123456"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("verificar")))
        }
    }

    // =====================================================
    // POST /api/auth/forgot-password
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/forgot-password")
    inner class ForgotPasswordEndpoint {

        @Test
        @DisplayName("Forgot password retorna 200 siempre (seguridad)")
        fun forgotPasswordSuccess() {
            `when`(authService.forgotPassword(anyString()))
                .thenReturn("Si el correo esta registrado, enviamos instrucciones.")

            mockMvc.perform(
                post("/api/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"diego@test.com"}""")
            )
                .andExpect(status().isOk)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("instrucciones")))
        }
    }

    // =====================================================
    // POST /api/auth/reset-password
    // =====================================================
    @Nested
    @DisplayName("POST /api/auth/reset-password")
    inner class ResetPasswordEndpoint {

        @Test
        @DisplayName("Reset exitoso retorna 200")
        fun resetSuccess() {
            `when`(authService.resetPassword(anyString(), anyString()))
                .thenReturn("Contrasena actualizada.")

            mockMvc.perform(
                post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"token":"ABC123","password":"newpass1"}""")
            )
                .andExpect(status().isOk)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("actualizada")))
        }

        @Test
        @DisplayName("Token invalido retorna 400")
        fun resetInvalidToken() {
            `when`(authService.resetPassword(anyString(), anyString()))
                .thenThrow(RuntimeException("Codigo invalido o expirado"))

            mockMvc.perform(
                post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"token":"INVALID","password":"newpass1"}""")
            )
                .andExpect(status().isBadRequest)
        }
    }
}
