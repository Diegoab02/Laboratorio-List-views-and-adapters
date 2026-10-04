package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Pregunta de la evaluacion final de un curso (HU-10).
 * Las opciones se guardan separadas por '|' para no abrir una tabla extra
 * en un modelo que siempre tiene entre 3 y 5 alternativas.
 */
@Entity
@Table(name = "questions")
class Question(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(nullable = false, length = 1000)
    var text: String,

    /** Alternativas separadas por '|'. Minimo 2, maximo 5. */
    @Column(nullable = false, length = 2000)
    var options: String,

    /** Indice de la alternativa correcta, base 0. Nunca viaja al cliente. */
    @Column(name = "correct_index", nullable = false)
    var correctIndex: Int,

    /** Explicacion que se muestra al revisar el resultado. */
    @Column(length = 1000)
    var explanation: String? = null,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val optionList: List<String>
        get() = options.split("|").map { it.trim() }.filter { it.isNotEmpty() }
}
