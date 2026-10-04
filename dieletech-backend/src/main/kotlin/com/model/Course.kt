package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "courses")
class Course(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, length = 1000)
    var description: String,

    /** Descripcion extensa en markdown ligero para la ficha del curso. */
    @Column(name = "long_description", length = 5000)
    var longDescription: String? = null,

    /** Objetivos de aprendizaje separados por '|'. */
    @Column(name = "learning_objectives", length = 2000)
    var learningObjectives: String? = null,

    /** A quien va dirigido, separado por '|'. */
    @Column(name = "target_audience", length = 1000)
    var targetAudience: String? = null,

    @Column(nullable = false)
    var technology: String,

    @Column(nullable = false)
    var level: String,

    @Column(nullable = false)
    var price: Double,

    @Column(nullable = false)
    var duration: Int,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    /**
     * Contador denormalizado de matriculados. Se recalcula desde la tabla
     * purchases al arrancar y se incrementa en cada compra real.
     * Nunca se siembra con valores ficticios.
     */
    @Column(name = "student_count")
    var studentCount: Int = 0,

    /** Cupos totales del curso. La compra se bloquea al agotarse. */
    @Column(nullable = false)
    var capacity: Int = 50,

    @Column(length = 2000)
    var curriculum: String? = null,

    @Column(length = 1000)
    var prerequisites: String? = null,

    @Column(name = "preview_video_url")
    var previewVideoUrl: String? = null,

    /** Nombre del instructor responsable del curso. */
    @Column(name = "instructor_name")
    var instructorName: String? = null,

    /** Correo del instructor: permite filtrar "mis cursos" en el panel. */
    @Column(name = "instructor_email")
    var instructorEmail: String? = null,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /** Cupos libres; nunca negativo. */
    val seatsAvailable: Int
        get() = (capacity - studentCount).coerceAtLeast(0)

    val isSoldOut: Boolean
        get() = seatsAvailable == 0
}
