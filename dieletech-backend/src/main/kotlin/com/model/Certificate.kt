package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Certificado emitido al aprobar la evaluacion final (HU-11).
 * El codigo es publico y verificable: identifica el certificado sin
 * exponer el correo del estudiante.
 */
@Entity
@Table(name = "certificates")
class Certificate(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** Codigo unico de verificacion, formato DTC-XXXX-XXXX-XXXX. */
    @Column(nullable = false, unique = true, length = 32)
    var code: String,

    @Column(name = "user_email", nullable = false)
    var userEmail: String,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    /** Se congelan en el momento de la emision: el certificado no cambia
     *  aunque despues cambie el nombre del estudiante o el del curso. */
    @Column(name = "student_name", nullable = false)
    var studentName: String,

    @Column(name = "course_title", nullable = false)
    var courseTitle: String,

    @Column(name = "course_hours", nullable = false)
    var courseHours: Int,

    @Column(name = "instructor_name")
    var instructorName: String? = null,

    @Column(nullable = false)
    var score: Int,

    @Column(nullable = false)
    var revoked: Boolean = false,

    @Column(name = "issued_at")
    val issuedAt: LocalDateTime = LocalDateTime.now()
)
