package com.dieletech.mobile.data.model

/**
 * Lección individual de un curso.
 * HU-07: cada lección tiene un video reproducible.
 * HU-08: se puede marcar como completada.
 */
data class Lesson(
    val id: Long,
    val courseId: Long,
    val title: String,
    val description: String = "",
    val videoUrl: String,
    val durationSec: Int = 0,
    val order: Int = 0,
    val thumbnailUrl: String? = null
)

data class LessonProgress(
    val lessonId: Long,
    val positionMs: Long = 0L,
    val completed: Boolean = false
)

data class CourseProgress(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int
) {
    val percent: Float get() = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons
}
