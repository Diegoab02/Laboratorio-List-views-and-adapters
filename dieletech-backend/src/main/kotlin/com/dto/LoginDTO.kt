package com.dieletech.backend.dto

import jakarta.validation.constraints.*

data class LoginDTO(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String
)