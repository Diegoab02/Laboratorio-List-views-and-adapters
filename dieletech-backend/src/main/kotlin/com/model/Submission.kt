package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * HU-38: entrega de un estudiante para una tarea practica.
 *
 * Hay una sola fila por estudiante y tarea: la restriccion unica lo
 * garantiza en la base de datos y no solo en el servicio, porque dos
 * peticiones simultaneas pueden pasar la misma comprobacion de
 * aplicacion y escribir dos veces.
 *
 * El historial de calificacion vive en la misma fila (score, feedback,
 * gradedAt, gradedBy): esa es la trazabilidad que se pidio. Nadie
 * califica de forma anonima.
 */
@Entity
@Table(
    name = "submissions",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_submission_assignment_user", columnNames = ["assignment_id", "user_email"])
    ]
)
class Submission(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "assignment_id", nullable = false)
    var assignmentId: Long,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(name = "user_email", nullable = false, length = 160)
    var userEmail: String,

    @Column(nullable = false, length = 20000)
    var content: String = "",

    /** Enlace al archivo o repositorio, solo para tareas de tipo FILE. */
    @Column(name = "file_url", length = 1000)
    var fileUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SubmissionStatus = SubmissionStatus.DRAFT,

    /** Null mientras sea borrador. */
    @Column(name = "submitted_at")
    var submittedAt: LocalDateTime? = null,

    /** Fecha limite calculada desde la compra, congelada en la entrega. */
    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    /** Se entrego despues del plazo. No bloquea, queda registrado. */
    @Column(name = "late_submission", nullable = false)
    var lateSubmission: Boolean = false,

    @Column
    var score: Double? = null,

    @Column(length = 4000)
    var feedback: String? = null,

    @Column(name = "graded_at")
    var gradedAt: LocalDateTime? = null,

    /** Correo de quien califico. Sin esto no hay trazabilidad. */
    @Column(name = "graded_by", length = 160)
    var gradedBy: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
