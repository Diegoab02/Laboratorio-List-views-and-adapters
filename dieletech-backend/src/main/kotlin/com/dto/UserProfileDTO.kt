package com.dieletech.backend.dto

import jakarta.validation.constraints.*

/** Perfil completo que consume la pagina de usuario. */
data class UserProfileDTO(
    val id: Long,
    val name: String,
    val displayName: String?,
    val email: String,
    val role: String,
    val verified: Boolean,
    val avatarUrl: String?,
    val bio: String?,
    val jobTitle: String?,
    val phone: String?,
    val country: String?,
    val city: String?,
    val interests: List<String>,
    val linkedinUrl: String?,
    val githubUrl: String?,
    val websiteUrl: String?,
    val themePreference: String,
    val accentColor: String,
    val languagePreference: String,
    val notifyEmail: Boolean,
    val notifyNewCourses: Boolean,
    val notifyProgress: Boolean,
    val publicProfile: Boolean,
    val memberSince: String,
    // Estadisticas reales del estudiante
    val coursesEnrolled: Int,
    val coursesCompleted: Int,
    val lessonsCompleted: Int,
    val totalInvested: Double,
    val averageProgress: Int
)

/** Todos los campos son opcionales: se actualiza solo lo que llega. */
data class UpdateProfileDTO(
    @field:Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    val name: String? = null,

    @field:Size(max = 50, message = "El nombre para mostrar no puede exceder 50 caracteres")
    val displayName: String? = null,

    @field:Size(max = 500, message = "La biografia no puede exceder 500 caracteres")
    val bio: String? = null,

    @field:Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    val jobTitle: String? = null,

    @field:Pattern(
        regexp = "^$|^[0-9+ ()-]{7,20}$",
        message = "El telefono no tiene un formato valido"
    )
    val phone: String? = null,

    val country: String? = null,
    val city: String? = null,

    /** Lista de intereses; se almacena separada por '|'. */
    val interests: List<String>? = null,

    @field:Pattern(
        regexp = "^$|^https?://.*",
        message = "El enlace de LinkedIn debe iniciar con http:// o https://"
    )
    val linkedinUrl: String? = null,

    @field:Pattern(
        regexp = "^$|^https?://.*",
        message = "El enlace de GitHub debe iniciar con http:// o https://"
    )
    val githubUrl: String? = null,

    @field:Pattern(
        regexp = "^$|^https?://.*",
        message = "El sitio web debe iniciar con http:// o https://"
    )
    val websiteUrl: String? = null,

    val avatarUrl: String? = null,

    @field:Pattern(
        regexp = "^$|^(light|dark|system)$",
        message = "El tema debe ser light, dark o system"
    )
    val themePreference: String? = null,

    @field:Pattern(
        regexp = "^$|^#[0-9a-fA-F]{6}$",
        message = "El color debe estar en formato hexadecimal, por ejemplo #2563eb"
    )
    val accentColor: String? = null,

    @field:Pattern(
        regexp = "^$|^(es|en)$",
        message = "El idioma debe ser es o en"
    )
    val languagePreference: String? = null,

    val notifyEmail: Boolean? = null,
    val notifyNewCourses: Boolean? = null,
    val notifyProgress: Boolean? = null,
    val publicProfile: Boolean? = null
)

data class ChangePasswordDTO(
    @field:NotBlank(message = "La contrasena actual es obligatoria")
    val currentPassword: String,

    @field:NotBlank(message = "La nueva contrasena es obligatoria")
    @field:Size(min = 6, message = "La nueva contrasena debe tener minimo 6 caracteres")
    val newPassword: String,

    @field:NotBlank(message = "Debes confirmar la nueva contrasena")
    val confirmPassword: String
)
