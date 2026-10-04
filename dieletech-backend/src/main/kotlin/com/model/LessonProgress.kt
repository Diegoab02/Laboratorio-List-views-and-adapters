package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "lesson_progress",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_email", "lesson_id"])]
)
class LessonProgress(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_email", nullable = false)
    var userEmail: String,

    @Column(name = "lesson_id", nullable = false)
    var lessonId: Long,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(nullable = false)
    var completed: Boolean = false,

    @Column(name = "last_position_seconds")
    var lastPositionSeconds: Int = 0,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
