package com.controller

import com.dieletech.backend.dto.ChangePasswordDTO
import com.dieletech.backend.dto.UpdateProfileDTO
import com.dieletech.backend.dto.UserProfileDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import service.UserProfileService

/**
 * HU-14: perfil de usuario. Todos los endpoints operan sobre el usuario
 * autenticado leido del JWT; nunca sobre un id enviado por el cliente.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(private val profileService: UserProfileService) {

    @GetMapping("/me")
    fun me(authentication: Authentication?): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesion no valida")
        return try {
            ResponseEntity.ok(profileService.getProfile(email))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
        }
    }

    @PutMapping("/me")
    fun updateMe(
        @Valid @RequestBody dto: UpdateProfileDTO,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesion no valida")
        return try {
            ResponseEntity.ok(profileService.updateProfile(email, dto))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
        }
    }

    @PutMapping("/me/password")
    fun changePassword(
        @Valid @RequestBody dto: ChangePasswordDTO,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesion no valida")
        return try {
            ResponseEntity.ok(mapOf("message" to profileService.changePassword(email, dto)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
        }
    }

    @PutMapping("/me/avatar")
    fun updateAvatar(
        @RequestBody body: Map<String, String>,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesion no valida")
        val dataUri = body["avatarUrl"]
            ?: return ResponseEntity.badRequest().body("Falta el campo avatarUrl")
        return try {
            ResponseEntity.ok(profileService.updateAvatar(email, dataUri))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
        }
    }
}
