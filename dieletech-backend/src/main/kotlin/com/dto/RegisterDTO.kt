package com.dieletech.backend.dto

import jakarta.validation.constraints.*

data class RegisterDTO(
    @field:NotBlank(message = "El nombre es obligatorio")
    val name: String,

    @field:Email(message = "El correo no tiene un formato válido")
    @field:NotBlank(message = "El correo es obligatorio")
    val email: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    val password: String,

    val role: String? = "STUDENT"
)
