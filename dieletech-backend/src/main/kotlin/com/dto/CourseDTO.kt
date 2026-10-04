package com.dieletech.backend.dto

data class CourseDTO(
    val id: Long,
    val title: String,
    val description: String,
    val longDescription: String?,
    val technology: String,
    val level: String,
    val price: Double,
    val duration: Int,
    val imageUrl: String?,
    val studentCount: Int,
    val capacity: Int,
    val seatsAvailable: Int,
    val soldOut: Boolean,
    val curriculum: List<String>,
    val prerequisites: List<String>,
    val learningObjectives: List<String>,
    val targetAudience: List<String>,
    val previewVideoUrl: String?,
    val instructorName: String?
)

/**
 * Resumen para el catalogo. Incluye cupos y matriculados reales para que
 * la tarjeta del curso no tenga que inventar cifras.
 */
data class CourseSummaryDTO(
    val id: Long,
    val title: String,
    val description: String,
    val technology: String,
    val level: String,
    val price: Double,
    val duration: Int,
    val imageUrl: String?,
    val studentCount: Int,
    val capacity: Int,
    val seatsAvailable: Int,
    val soldOut: Boolean,
    val previewVideoUrl: String?,
    val instructorName: String?
)
