package com.dieletech.backend.dto

import java.time.LocalDateTime

data class LessonDTO(
    val id: Long,
    val courseId: Long,
    val title: String,
    val description: String?,
    val videoUrl: String?,
    val orderIndex: Int,
    val durationMinutes: Int,
    val contentType: String,
    val materialUrl: String?,
    val freePreview: Boolean = false,
    /** El backend oculta el video si el usuario no compro y no es preview. */
    val locked: Boolean = false
)

data class LessonProgressDTO(
    val lessonId: Long,
    val completed: Boolean,
    val lastPositionSeconds: Int,
    val completedAt: LocalDateTime?
)

data class LessonProgressUpdateDTO(
    val email: String,
    val completed: Boolean? = null,
    val lastPositionSeconds: Int? = null
)

data class CourseProgressDTO(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val progressPercent: Int,
    val lessonProgress: List<LessonProgressDTO>
)

// DTOs para Admin (HU-09)
data class CreateCourseDTO(
    val title: String,
    val description: String,
    val longDescription: String? = null,
    val technology: String,
    val level: String,
    val price: Double,
    val duration: Int,
    val capacity: Int = 50,
    val imageUrl: String? = null,
    val curriculum: String? = null,
    val prerequisites: String? = null,
    val learningObjectives: String? = null,
    val targetAudience: String? = null,
    val previewVideoUrl: String? = null,
    val instructorName: String? = null
)

data class CreateLessonDTO(
    val title: String,
    val description: String? = null,
    val videoUrl: String? = null,
    val orderIndex: Int,
    val durationMinutes: Int = 0,
    val contentType: String = "VIDEO",
    val materialUrl: String? = null,
    val freePreview: Boolean = false
)
