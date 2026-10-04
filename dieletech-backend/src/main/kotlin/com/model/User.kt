package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var verified: Boolean = false,

    @Column(name = "verification_token")
    var verificationToken: String? = null,

    @Column(name = "reset_token")
    var resetToken: String? = null,

    @Column(name = "reset_token_expiry")
    var resetTokenExpiry: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.STUDENT,

    // ── Perfil personalizable (HU-14) ──

    /** Avatar: URL o data URI de imagen subida por el usuario. */
    @Column(name = "avatar_url", length = 2000)
    var avatarUrl: String? = null,

    /** Iniciales de respaldo cuando no hay avatar. */
    @Column(name = "display_name")
    var displayName: String? = null,

    @Column(length = 500)
    var bio: String? = null,

    @Column(name = "job_title")
    var jobTitle: String? = null,

    var phone: String? = null,

    var country: String? = null,

    var city: String? = null,

    /** Intereses de aprendizaje separados por '|'. */
    @Column(length = 500)
    var interests: String? = null,

    @Column(name = "linkedin_url")
    var linkedinUrl: String? = null,

    @Column(name = "github_url")
    var githubUrl: String? = null,

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    /** Preferencia de tema de la interfaz: light, dark o system. */
    @Column(name = "theme_preference")
    var themePreference: String = "system",

    /** Color de acento elegido por el usuario. */
    @Column(name = "accent_color")
    var accentColor: String = "#2563eb",

    @Column(name = "language_preference")
    var languagePreference: String = "es",

    @Column(name = "notify_email")
    var notifyEmail: Boolean = true,

    @Column(name = "notify_new_courses")
    var notifyNewCourses: Boolean = true,

    @Column(name = "notify_progress")
    var notifyProgress: Boolean = true,

    /** Perfil publico visible para otros usuarios. */
    @Column(name = "public_profile")
    var publicProfile: Boolean = false,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)