package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * HU-37: ejercicio practico que la plataforma plantea para un modulo.
 *
 * El plazo se guarda como desplazamiento en dias desde la compra
 * (dueOffsetDays) y no como fecha absoluta: cada estudiante entra al curso
 * cuando quiere, asi que una fecha fija no significaria lo mismo para todos.
 *
 * La tabla nace vacia, por eso todas las columnas obligatorias declaran
 * nullable = false desde el primer commit. Es la leccion del Sprint 8: una
 * columna anulable anadida a una tabla con filas ya escritas deja NULL en
 * propiedades primitivas y revienta Hibernate al arrancar.
 */
@Entity
@Table(
    name = "assignments",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_assignment_course_lesson", columnNames = ["course_id", "lesson_id"])
    ]
)
class Assignment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    /** Modulo al que pertenece. Null = tarea del curso completo. */
    @Column(name = "lesson_id")
    var lessonId: Long? = null,

    @Column(nullable = false, length = 160)
    var title: String,

    @Column(nullable = false, length = 4000)
    var statement: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: AssignmentType = AssignmentType.TEXT,

    @Column(name = "max_score", nullable = false)
    var maxScore: Double = 100.0,

    /** Dias de plazo contados desde la compra del curso. */
    @Column(name = "due_offset_days", nullable = false)
    var dueOffsetDays: Int = 14,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
