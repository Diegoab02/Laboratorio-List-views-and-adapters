package com.dieletech.backend.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

/** Entrega tal como la ve su autor (HU-38). */
data class SubmissionDTO(
    val id: Long,
    val assignmentId: Long,
    val courseId: Long,
    val content: String,
    val fileUrl: String?,
    val status: String,
    val submittedAt: LocalDateTime?,
    val dueDate: LocalDateTime?,
    val late: Boolean,
    val score: Double?,
    val feedback: String?,
    val gradedAt: LocalDateTime?,
    val gradedBy: String?
)

/**
 * Tarea con la entrega del estudiante y el estado de habilitacion.
 * Un solo viaje al servidor para pintar el modulo completo.
 */
data class StudentAssignmentDTO(
    val assignment: AssignmentDTO,
    val submission: SubmissionDTO?,
    /** Si puede entregar ahora mismo. */
    val canSubmit: Boolean,
    /** Motivo cuando no puede, en lenguaje del usuario. */
    val blockedReason: String?,
    val dueDate: LocalDateTime?,
    /** Dias que faltan; negativo si el plazo ya paso. */
    val daysLeft: Long?
)

/**
 * Guardar borrador o entregar.
 *
 * El contenido se exige siempre: un borrador vacio no es un borrador,
 * es ruido en la base de datos. Para las tareas de tipo FILE el
 * servicio ademas comprueba que fileUrl sea una URL http o https.
 */
data class SubmitAssignmentDTO(

    @field:NotBlank(message = "La entrega no puede estar vacia")
    @field:Size(max = 20000, message = "La entrega no puede superar 20000 caracteres")
    val content: String,

    @field:Size(max = 1000, message = "El enlace no puede superar 1000 caracteres")
    val fileUrl: String? = null,

    /** true guarda borrador; false entrega de forma definitiva. */
    @field:NotNull(message = "Indica si es borrador o entrega definitiva")
    val draft: Boolean = true
)
