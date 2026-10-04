package com.dieletech.backend.dto

data class AuthResponse(
    val token: String,
    val name: String,
    val email: String,
    val role: String
)