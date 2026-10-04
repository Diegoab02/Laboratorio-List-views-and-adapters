package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "lessons")
class Lesson(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(nullable = false)
    var title: String,

    @Column(length = 1000)
    var description: String? = null,

    @Column(name = "video_url")
    var videoUrl: String? = null,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @Column(name = "duration_minutes")
    var durationMinutes: Int = 0,

    @Column(name = "content_type", nullable = false)
    var contentType: String = "VIDEO",

    /** Leccion de muestra: visible sin haber comprado el curso. */
    @Column(name = "free_preview", nullable = false)
    var freePreview: Boolean = false,

    @Column(name = "material_url")
    var materialUrl: String? = null,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
