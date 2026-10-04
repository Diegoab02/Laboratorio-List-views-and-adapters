package com.dieletech.mobile.data.model

// DTO de red (campos nulos tolerados; el catalogo no trae curriculum)
data class CourseNet(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val technology: String = "",
    val level: String = "",
    val price: Double = 0.0,
    val duration: Int = 0,
    val imageUrl: String? = null,
    val studentCount: Int = 0,
    val capacity: Int = 0,
    val seatsAvailable: Int = 0,
    val soldOut: Boolean = false,
    val longDescription: String? = null,
    val instructorName: String? = null,
    val previewVideoUrl: String? = null,
    val curriculum: List<String>? = null,
    val prerequisites: List<String>? = null,
    val learningObjectives: List<String>? = null,
    val targetAudience: List<String>? = null
)

fun CourseNet.toCourse(): Course = Course(
    id = id,
    title = title,
    description = description,
    technology = technology,
    level = level,
    price = price,
    duration = duration,
    imageUrl = imageUrl,
    studentCount = studentCount,
    capacity = capacity,
    seatsAvailable = seatsAvailable,
    soldOut = soldOut,
    longDescription = longDescription,
    instructorName = instructorName,
    previewVideoUrl = previewVideoUrl,
    curriculum = curriculum ?: emptyList(),
    prerequisites = prerequisites ?: emptyList(),
    learningObjectives = learningObjectives ?: emptyList(),
    targetAudience = targetAudience ?: emptyList()
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "STUDENT"
)

data class LoginRequest(val email: String, val password: String)

data class AuthResponseNet(
    val token: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = ""
)

data class VerifyCodeRequest(val email: String, val code: String)
data class ResendCodeRequest(val email: String)

data class PurchaseResponseNet(
    val success: Boolean = false,
    val orderId: String = "",
    val courseId: Long = 0,
    val courseName: String = "",
    val amount: Double = 0.0,
    val message: String = ""
)

data class PurchaseNet(
    val id: Long = 0,
    val courseId: Long = 0,
    val userEmail: String = "",
    val fullName: String = "",
    val amount: Double = 0.0,
    val orderId: String = "",
    val status: String = ""
)

// ── Recuperacion de contrasena (HU-03) ──
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, val password: String)
