package com.dieletech.backend.dto

import jakarta.validation.constraints.*

/**
 * Tarea tal como la ve el estudiante (HU-37).
 * El enunciado solo viaja cuando el curso esta comprado; si no, llega
 * vacio y locked = true, igual que se hizo con videoUrl en HU-23.
 */
data class AssignmentDTO(
    val id: Long,
    val courseId: Long,
    val lessonId: Long?,
    val lessonTitle: String?,
    val title: String,
    val statement: String?,
    val type: String,
    val maxScore: Double,
    val dueOffsetDays: Int,
    val orderIndex: Int,
    /** true cuando el usuario no compro el curso. */
    val locked: Boolean
)

/** Tarea con los contadores que necesita el panel del instructor. */
data class AssignmentAdminDTO(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val lessonId: Long?,
    val lessonTitle: String?,
    val title: String,
    val statement: String,
    val type: String,
    val maxScore: Double,
    val dueOffsetDays: Int,
    val orderIndex: Int,
    val active: Boolean
)

/** Alta y edicion de una tarea. Espejo exacto de las reglas del servicio. */
data class CreateAssignmentDTO(

    @field:NotBlank(message = "El titulo es obligatorio")
    @field:Size(min = 5, max = 160, message = "El titulo debe tener entre 5 y 160 caracteres")
    val title: String,

    @field:NotBlank(message = "El enunciado es obligatorio")
    @field:Size(min = 20, max = 4000, message = "El enunciado debe tener entre 20 y 4000 caracteres")
    val statement: String,

    @field:NotBlank(message = "Indica el tipo de entrega")
    @field:Pattern(
        regexp = "CODE|FILE|TEXT",
        message = "El tipo de entrega debe ser CODE, FILE o TEXT"
    )
    val type: String,

    /** Null = tarea del curso completo. */
    val lessonId: Long? = null,

    @field:NotNull(message = "Indica el puntaje maximo")
    @field:DecimalMin(value = "1.0", message = "El puntaje maximo debe ser al menos 1")
    @field:DecimalMax(value = "100.0", message = "El puntaje maximo no puede superar 100")
    val maxScore: Double = 100.0,

    @field:NotNull(message = "Indica el plazo en dias")
    @field:Min(value = 1, message = "El plazo debe ser de al menos 1 dia")
    @field:Max(value = 365, message = "El plazo no puede superar 365 dias")
    val dueOffsetDays: Int = 14,

    val orderIndex: Int = 0
)
