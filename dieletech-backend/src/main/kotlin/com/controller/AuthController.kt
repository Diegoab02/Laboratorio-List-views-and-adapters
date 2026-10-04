package com.dieletech.backend.controller

import com.dieletech.backend.dto.AuthResponse
import com.dieletech.backend.dto.LoginDTO
import com.dieletech.backend.dto.RegisterDTO
import com.dieletech.backend.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody dto: RegisterDTO): ResponseEntity<String> =
        ResponseEntity.ok(authService.register(dto))

    @GetMapping("/verify")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<String> =
        ResponseEntity.ok(authService.verifyEmail(token))

    @PostMapping("/verify-code")
    fun verifyCode(@RequestBody body: Map<String, String>): ResponseEntity<String> {
        val email = body["email"] ?: throw RuntimeException("Email requerido")
        val code = body["code"] ?: throw RuntimeException("Código requerido")
        return ResponseEntity.ok(authService.verifyCode(email, code))
    }

    @PostMapping("/resend-code")
    fun resendCode(@RequestBody body: Map<String, String>): ResponseEntity<String> {
        val email = body["email"] ?: throw RuntimeException("Email requerido")
        return ResponseEntity.ok(authService.resendVerificationCode(email))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody dto: LoginDTO): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(dto))

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody body: Map<String, String>): ResponseEntity<String> =
        ResponseEntity.ok(authService.forgotPassword(body["email"] ?: ""))

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody body: Map<String, String>): ResponseEntity<String> =
        ResponseEntity.ok(authService.resetPassword(body["token"] ?: "", body["password"] ?: ""))
}
