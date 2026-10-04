package com.dieletech.mobile.data.model

data class Course(
    val id: Long,
    val title: String,
    val description: String,
    val technology: String,
    val level: String,
    val price: Double,
    val duration: Int,
    val imageUrl: String? = null,
    val studentCount: Int = 0,
    val capacity: Int = 0,
    val seatsAvailable: Int = 0,
    val soldOut: Boolean = false,
    val longDescription: String? = null,
    val instructorName: String? = null,
    val previewVideoUrl: String? = null,
    val curriculum: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val learningObjectives: List<String> = emptyList(),
    val targetAudience: List<String> = emptyList()
)

/**
 * Debe coincidir con PurchaseRequestDTO del backend: documento, telefono
 * y aceptacion de terminos son obligatorios o la compra se rechaza con 400.
 */
data class PurchaseRequest(
    val courseId: Long,
    val fullName: String,
    val email: String,
    val documentId: String,
    val phone: String,
    val paymentMethod: String,
    val amount: Double,
    val acceptTerms: Boolean
)

data class PurchaseResponse(
    val success: Boolean,
    val orderId: String,
    val message: String
)
