package com.dieletech.backend.service

import com.dieletech.backend.dto.AuthResponse
import com.dieletech.backend.dto.LoginDTO
import com.dieletech.backend.dto.RegisterDTO
import com.dieletech.backend.model.Role
import com.dieletech.backend.model.User
import com.dieletech.backend.repository.UserRepository
import com.dieletech.backend.security.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    private val jwtUtil: JwtUtil,
    @Value("\${app.auto-verify:false}") private val autoVerify: Boolean
) {

    fun register(dto: RegisterDTO): String {
        if (userRepository.findByEmail(dto.email.lowercase().trim()).isPresent) {
            throw RuntimeException("El correo ya está registrado")
        }

        // Parse role from request (default STUDENT)
        val role = try {
            Role.valueOf(dto.role?.uppercase()?.trim() ?: "STUDENT")
        } catch (e: Exception) {
            Role.STUDENT
        }

        // Generate 6-digit verification code
        val code = String.format("%06d", (100000..999999).random())

        val user = User(
            name = dto.name.trim(),
            email = dto.email.lowercase().trim(),
            password = passwordEncoder.encode(dto.password),
            verified = autoVerify,
            verificationToken = code,
            role = role,
            createdAt = LocalDateTime.now()
        )

        userRepository.save(user)

        // Send verification code email
        try {
            emailService.sendVerificationCodeEmail(dto.email.trim(), dto.name.trim(), code)
        } catch (e: Exception) {
            println("AVISO: no se pudo enviar correo de verificacion: ${e.message}")
        }

        return if (autoVerify)
            "Registro exitoso. Tu cuenta ya está activa, puedes iniciar sesión."
        else
            "Registro exitoso. Hemos enviado un código de verificación a tu correo."
    }

    fun verifyEmail(token: String): String {
        val user = userRepository.findByVerificationToken(token)
            .orElseThrow { RuntimeException("Token inválido o expirado") }

        user.verified = true
        user.verificationToken = null
        userRepository.save(user)

        return "Cuenta verificada exitosamente. Ya puedes iniciar sesión."
    }

    fun verifyCode(email: String, code: String): String {
        val normalizedEmail = email.lowercase().trim()
        val user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { RuntimeException("No se encontró una cuenta con ese correo") }

        if (user.verified) {
            return "La cuenta ya está verificada. Puedes iniciar sesión."
        }

        if (user.verificationToken == null || user.verificationToken != code.trim()) {
            throw RuntimeException("Código incorrecto. Verifica e intenta de nuevo.")
        }

        user.verified = true
        user.verificationToken = null
        userRepository.save(user)

        return "Cuenta verificada exitosamente. Ya puedes iniciar sesión."
    }

    fun resendVerificationCode(email: String): String {
        val normalizedEmail = email.lowercase().trim()
        val user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { RuntimeException("No se encontró una cuenta con ese correo") }

        if (user.verified) {
            return "La cuenta ya está verificada."
        }

        val newCode = String.format("%06d", (100000..999999).random())
        user.verificationToken = newCode
        userRepository.save(user)

        try {
            emailService.sendVerificationCodeEmail(user.email, user.name, newCode)
        } catch (e: Exception) {
            println("AVISO: no se pudo reenviar correo: ${e.message}")
        }

        return "Nuevo código enviado a tu correo."
    }

    fun login(dto: LoginDTO): AuthResponse {
        val user = userRepository.findByEmail(dto.email.lowercase().trim())
            .orElseThrow { RuntimeException("Credenciales incorrectas") }

        if (!user.verified) {
            throw RuntimeException("Debes verificar tu correo antes de iniciar sesión. Revisa tu bandeja de entrada.")
        }

        if (!passwordEncoder.matches(dto.password, user.password)) {
            throw RuntimeException("Credenciales incorrectas")
        }

        val token = jwtUtil.generateToken(user.email)

        return AuthResponse(
            token = token,
            name = user.name,
            email = user.email,
            role = user.role.name
        )
    }

    fun forgotPassword(email: String): String {
        val user = userRepository.findByEmail(email.lowercase().trim()).orElse(null)
        if (user != null) {
            val code = String.format("%06d", (100000..999999).random())
            user.resetToken = code
            user.resetTokenExpiry = LocalDateTime.now().plusMinutes(30)
            userRepository.save(user)
            try {
                emailService.sendPasswordResetEmail(user.email, user.name, code)
            } catch (e: Exception) {
                println("AVISO: no se pudo enviar correo de reset: ${e.message}")
            }
        }
        return "Si el correo está registrado, enviamos instrucciones para restablecer la contraseña."
    }

    fun resetPassword(token: String, newPassword: String): String {
        if (newPassword.length < 6) throw RuntimeException("La contraseña debe tener mínimo 6 caracteres")
        val user = userRepository.findByResetToken(token)
            .orElseThrow { RuntimeException("Código inválido o expirado") }
        val expiry = user.resetTokenExpiry
        if (expiry == null || expiry.isBefore(LocalDateTime.now())) {
            throw RuntimeException("El código expiró. Solicita uno nuevo.")
        }
        user.password = passwordEncoder.encode(newPassword)
        user.resetToken = null
        user.resetTokenExpiry = null
        userRepository.save(user)
        return "Contraseña actualizada. Ya puedes iniciar sesión."
    }
}
