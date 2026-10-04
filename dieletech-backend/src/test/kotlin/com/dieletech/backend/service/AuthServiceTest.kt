package com.dieletech.backend.service

import com.dieletech.backend.dto.LoginDTO
import com.dieletech.backend.dto.RegisterDTO
import com.dieletech.backend.model.Role
import com.dieletech.backend.model.User
import com.dieletech.backend.repository.UserRepository
import com.dieletech.backend.security.JwtUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("AuthService - Tests Unitarios")
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Captor
    private lateinit var userCaptor: ArgumentCaptor<User>

    private lateinit var authService: AuthService

    // Helper para Mockito any() en Kotlin — evita NPE en params non-nullable
    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObj(): T = any<T>() as T

    @BeforeEach
    fun setUp() {
        // autoVerify = false (modo produccion)
        authService = AuthService(userRepository, passwordEncoder, emailService, jwtUtil, false)
    }

    // =====================================================
    // REGISTER
    // =====================================================
    @Nested
    @DisplayName("register()")
    inner class RegisterTests {

        @Test
        @DisplayName("Registro exitoso con rol STUDENT por defecto")
        fun registerSuccess_defaultRole() {
            val dto = RegisterDTO(name = "Diego", email = "diego@test.com", password = "123456")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.empty())
            `when`(passwordEncoder.encode("123456")).thenReturn("encoded_password")
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            val result = authService.register(dto)

            assertTrue(result.contains("código de verificación"))
            verify(userRepository).save(userCaptor.capture())
            val savedUser = userCaptor.value
            assertEquals("Diego", savedUser.name)
            assertEquals("diego@test.com", savedUser.email)
            assertEquals("encoded_password", savedUser.password)
            assertEquals(Role.STUDENT, savedUser.role)
            assertFalse(savedUser.verified)
            assertNotNull(savedUser.verificationToken)
            assertEquals(6, savedUser.verificationToken!!.length)
        }

        @Test
        @DisplayName("Registro exitoso con rol INSTRUCTOR")
        fun registerSuccess_instructorRole() {
            val dto = RegisterDTO(name = "Prof Ana", email = "Ana@Test.COM", password = "abc123", role = "INSTRUCTOR")
            `when`(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.empty())
            `when`(passwordEncoder.encode("abc123")).thenReturn("enc_abc")
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            authService.register(dto)

            verify(userRepository).save(userCaptor.capture())
            assertEquals(Role.INSTRUCTOR, userCaptor.value.role)
            assertEquals("ana@test.com", userCaptor.value.email)
        }

        @Test
        @DisplayName("Registro exitoso con rol ADMIN")
        fun registerSuccess_adminRole() {
            val dto = RegisterDTO(name = "Admin", email = "admin@dieletech.com", password = "secure1", role = "ADMIN")
            `when`(userRepository.findByEmail("admin@dieletech.com")).thenReturn(Optional.empty())
            `when`(passwordEncoder.encode("secure1")).thenReturn("enc_secure")
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            authService.register(dto)

            verify(userRepository).save(userCaptor.capture())
            assertEquals(Role.ADMIN, userCaptor.value.role)
        }

        @Test
        @DisplayName("Registro con rol invalido cae a STUDENT")
        fun registerSuccess_invalidRoleFallsBackToStudent() {
            val dto = RegisterDTO(name = "Test", email = "test@x.com", password = "123456", role = "SUPERUSER")
            `when`(userRepository.findByEmail("test@x.com")).thenReturn(Optional.empty())
            `when`(passwordEncoder.encode("123456")).thenReturn("enc")
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            authService.register(dto)

            verify(userRepository).save(userCaptor.capture())
            assertEquals(Role.STUDENT, userCaptor.value.role)
        }

        @Test
        @DisplayName("Registro falla si el correo ya existe")
        fun registerFails_duplicateEmail() {
            val existingUser = createUser(email = "diego@test.com")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(existingUser))

            val dto = RegisterDTO(name = "Diego", email = "Diego@Test.com", password = "123456")

            val exception = assertThrows(RuntimeException::class.java) {
                authService.register(dto)
            }
            assertTrue(exception.message!!.contains("ya está registrado"))
            verify(userRepository, never()).save(anyObj<User>())
        }

        @Test
        @DisplayName("Registro con autoVerify=true activa la cuenta automaticamente")
        fun registerWithAutoVerify() {
            val autoVerifyService = AuthService(userRepository, passwordEncoder, emailService, jwtUtil, true)
            val dto = RegisterDTO(name = "Auto", email = "auto@test.com", password = "123456")
            `when`(userRepository.findByEmail("auto@test.com")).thenReturn(Optional.empty())
            `when`(passwordEncoder.encode("123456")).thenReturn("enc")
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            val result = autoVerifyService.register(dto)

            assertTrue(result.contains("ya está activa"))
            verify(userRepository).save(userCaptor.capture())
            assertTrue(userCaptor.value.verified)
        }

        @Test
        @DisplayName("Registro continua si email falla al enviar")
        fun registerContinuesIfEmailFails() {
            val dto = RegisterDTO(name = "Test", email = "fail@test.com", password = "123456")
            `when`(userRepository.findByEmail("fail@test.com")).thenReturn(Optional.empty())
            `when`(passwordEncoder.encode("123456")).thenReturn("enc")
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }
            doThrow(RuntimeException("SMTP error")).`when`(emailService)
                .sendVerificationCodeEmail(anyString(), anyString(), anyString())

            val result = authService.register(dto)

            assertTrue(result.contains("código de verificación"))
            verify(userRepository).save(anyObj<User>())
        }
    }

    // =====================================================
    // VERIFY CODE
    // =====================================================
    @Nested
    @DisplayName("verifyCode()")
    inner class VerifyCodeTests {

        @Test
        @DisplayName("Verificacion exitosa con codigo correcto")
        fun verifyCodeSuccess() {
            val user = createUser(email = "diego@test.com", verified = false, verificationToken = "123456")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            val result = authService.verifyCode("Diego@Test.com", "123456")

            assertTrue(result.contains("verificada exitosamente"))
            verify(userRepository).save(userCaptor.capture())
            assertTrue(userCaptor.value.verified)
            assertNull(userCaptor.value.verificationToken)
        }

        @Test
        @DisplayName("Cuenta ya verificada retorna mensaje informativo")
        fun verifyCode_alreadyVerified() {
            val user = createUser(email = "diego@test.com", verified = true)
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))

            val result = authService.verifyCode("diego@test.com", "123456")

            assertTrue(result.contains("ya está verificada"))
            verify(userRepository, never()).save(anyObj<User>())
        }

        @Test
        @DisplayName("Codigo incorrecto lanza excepcion")
        fun verifyCode_wrongCode() {
            val user = createUser(email = "diego@test.com", verified = false, verificationToken = "999999")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))

            val exception = assertThrows(RuntimeException::class.java) {
                authService.verifyCode("diego@test.com", "111111")
            }
            assertTrue(exception.message!!.contains("incorrecto"))
        }

        @Test
        @DisplayName("Email no encontrado lanza excepcion")
        fun verifyCode_emailNotFound() {
            `when`(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty())

            val exception = assertThrows(RuntimeException::class.java) {
                authService.verifyCode("ghost@test.com", "123456")
            }
            assertTrue(exception.message!!.contains("No se encontró"))
        }

        @Test
        @DisplayName("Normaliza email (lowercase + trim) antes de buscar")
        fun verifyCode_normalizesEmail() {
            val user = createUser(email = "diego@test.com", verified = false, verificationToken = "123456")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            authService.verifyCode("  DIEGO@Test.COM  ", "123456")

            verify(userRepository).findByEmail("diego@test.com")
        }
    }

    // =====================================================
    // RESEND VERIFICATION CODE
    // =====================================================
    @Nested
    @DisplayName("resendVerificationCode()")
    inner class ResendCodeTests {

        @Test
        @DisplayName("Reenvia codigo exitosamente")
        fun resendSuccess() {
            val user = createUser(email = "diego@test.com", verified = false, verificationToken = "old_code")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            val result = authService.resendVerificationCode("diego@test.com")

            assertTrue(result.contains("Nuevo código enviado"))
            verify(userRepository).save(userCaptor.capture())
            assertNotEquals("old_code", userCaptor.value.verificationToken)
            assertEquals(6, userCaptor.value.verificationToken!!.length)
        }

        @Test
        @DisplayName("Cuenta ya verificada retorna mensaje informativo")
        fun resend_alreadyVerified() {
            val user = createUser(email = "diego@test.com", verified = true)
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))

            val result = authService.resendVerificationCode("diego@test.com")

            assertTrue(result.contains("ya está verificada"))
            verify(emailService, never()).sendVerificationCodeEmail(anyString(), anyString(), anyString())
        }

        @Test
        @DisplayName("Email no encontrado lanza excepcion")
        fun resend_emailNotFound() {
            `when`(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty())

            assertThrows(RuntimeException::class.java) {
                authService.resendVerificationCode("ghost@test.com")
            }
        }
    }

    // =====================================================
    // LOGIN
    // =====================================================
    @Nested
    @DisplayName("login()")
    inner class LoginTests {

        @Test
        @DisplayName("Login exitoso retorna token y datos del usuario")
        fun loginSuccess() {
            val user = createUser(
                email = "diego@test.com", name = "Diego",
                password = "encoded_pw", verified = true, role = Role.INSTRUCTOR
            )
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))
            `when`(passwordEncoder.matches("mypassword", "encoded_pw")).thenReturn(true)
            `when`(jwtUtil.generateToken("diego@test.com")).thenReturn("jwt_token_123")

            val dto = LoginDTO(email = "Diego@Test.com", password = "mypassword")
            val response = authService.login(dto)

            assertEquals("jwt_token_123", response.token)
            assertEquals("Diego", response.name)
            assertEquals("diego@test.com", response.email)
            assertEquals("INSTRUCTOR", response.role)
        }

        @Test
        @DisplayName("Login con usuario no verificado lanza excepcion")
        fun loginFails_notVerified() {
            val user = createUser(email = "diego@test.com", verified = false, password = "enc")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))

            val dto = LoginDTO(email = "diego@test.com", password = "123456")

            val exception = assertThrows(RuntimeException::class.java) {
                authService.login(dto)
            }
            assertTrue(exception.message!!.contains("verificar"))
        }

        @Test
        @DisplayName("Login con password incorrecto lanza excepcion")
        fun loginFails_wrongPassword() {
            val user = createUser(email = "diego@test.com", verified = true, password = "enc")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))
            `when`(passwordEncoder.matches("wrong_pw", "enc")).thenReturn(false)

            val dto = LoginDTO(email = "diego@test.com", password = "wrong_pw")

            val exception = assertThrows(RuntimeException::class.java) {
                authService.login(dto)
            }
            assertTrue(exception.message!!.contains("incorrectas"))
        }

        @Test
        @DisplayName("Login con email no registrado lanza excepcion")
        fun loginFails_emailNotFound() {
            `when`(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty())

            val dto = LoginDTO(email = "nobody@test.com", password = "123456")

            val exception = assertThrows(RuntimeException::class.java) {
                authService.login(dto)
            }
            assertTrue(exception.message!!.contains("incorrectas"))
        }

        @Test
        @DisplayName("Login normaliza email (lowercase + trim)")
        fun login_normalizesEmail() {
            val user = createUser(email = "diego@test.com", verified = true, password = "enc")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))
            `when`(passwordEncoder.matches("pass", "enc")).thenReturn(true)
            `when`(jwtUtil.generateToken("diego@test.com")).thenReturn("token")

            val dto = LoginDTO(email = "  DIEGO@Test.COM  ", password = "pass")
            authService.login(dto)

            verify(userRepository).findByEmail("diego@test.com")
        }
    }

    // =====================================================
    // FORGOT PASSWORD
    // =====================================================
    @Nested
    @DisplayName("forgotPassword()")
    inner class ForgotPasswordTests {

        @Test
        @DisplayName("Envia codigo de reset si el email existe")
        fun forgotPassword_emailExists() {
            val user = createUser(email = "diego@test.com")
            `when`(userRepository.findByEmail("diego@test.com")).thenReturn(Optional.of(user))
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            val result = authService.forgotPassword("diego@test.com")

            assertTrue(result.contains("instrucciones"))
            verify(userRepository).save(userCaptor.capture())
            assertNotNull(userCaptor.value.resetToken)
            assertNotNull(userCaptor.value.resetTokenExpiry)
        }

        @Test
        @DisplayName("No revela si el email no existe (seguridad)")
        fun forgotPassword_emailNotExists() {
            `when`(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty())

            val result = authService.forgotPassword("ghost@test.com")

            assertTrue(result.contains("instrucciones"))
            verify(userRepository, never()).save(anyObj<User>())
        }
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================
    @Nested
    @DisplayName("resetPassword()")
    inner class ResetPasswordTests {

        @Test
        @DisplayName("Reset exitoso actualiza password")
        fun resetPasswordSuccess() {
            val user = createUser(
                email = "diego@test.com",
                resetToken = "ABC123",
                resetTokenExpiry = LocalDateTime.now().plusMinutes(15)
            )
            `when`(userRepository.findByResetToken("ABC123")).thenReturn(Optional.of(user))
            `when`(passwordEncoder.encode("newpass1")).thenReturn("enc_new")
            `when`(userRepository.save(anyObj<User>())).thenAnswer { it.arguments[0] }

            val result = authService.resetPassword("ABC123", "newpass1")

            assertTrue(result.contains("actualizada"))
            verify(userRepository).save(userCaptor.capture())
            assertEquals("enc_new", userCaptor.value.password)
            assertNull(userCaptor.value.resetToken)
            assertNull(userCaptor.value.resetTokenExpiry)
        }

        @Test
        @DisplayName("Password muy corto lanza excepcion")
        fun resetPassword_tooShort() {
            val exception = assertThrows(RuntimeException::class.java) {
                authService.resetPassword("token", "abc")
            }
            assertTrue(exception.message!!.contains("mínimo 6"))
        }

        @Test
        @DisplayName("Token expirado lanza excepcion")
        fun resetPassword_expiredToken() {
            val user = createUser(
                resetToken = "EXP123",
                resetTokenExpiry = LocalDateTime.now().minusMinutes(5)
            )
            `when`(userRepository.findByResetToken("EXP123")).thenReturn(Optional.of(user))

            val exception = assertThrows(RuntimeException::class.java) {
                authService.resetPassword("EXP123", "newpass1")
            }
            assertTrue(exception.message!!.contains("expiró"))
        }

        @Test
        @DisplayName("Token invalido lanza excepcion")
        fun resetPassword_invalidToken() {
            `when`(userRepository.findByResetToken("INVALID")).thenReturn(Optional.empty())

            assertThrows(RuntimeException::class.java) {
                authService.resetPassword("INVALID", "newpass1")
            }
        }
    }

    // =====================================================
    // HELPER
    // =====================================================
    private fun createUser(
        id: Long = 1L,
        name: String = "Test User",
        email: String = "test@test.com",
        password: String = "encoded",
        verified: Boolean = false,
        verificationToken: String? = null,
        role: Role = Role.STUDENT,
        resetToken: String? = null,
        resetTokenExpiry: LocalDateTime? = null
    ): User {
        val user = User(
            id = id,
            name = name,
            email = email,
            password = password,
            verified = verified,
            verificationToken = verificationToken,
            role = role,
            createdAt = LocalDateTime.now()
        )
        user.resetToken = resetToken
        user.resetTokenExpiry = resetTokenExpiry
        return user
    }
}
