package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Un intento de evaluacion final. Se conserva el historial completo:
 * el certificado se emite con el primer intento aprobado.
 */
@Entity
@Table(name = "quiz_attempts")
class QuizAttempt(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_email", nullable = false)
    var userEmail: String,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    /** Porcentaje de acierto, 0 a 100. */
    @Column(nullable = false)
    var score: Int,

    @Column(name = "correct_answers", nullable = false)
    var correctAnswers: Int,

    @Column(name = "total_questions", nullable = false)
    var totalQuestions: Int,

    @Column(nullable = false)
    var passed: Boolean,

    /** Respuestas enviadas, como "idPregunta:indice|idPregunta:indice". */
    @Column(length = 2000)
    var answers: String? = null,

    @Column(name = "attempted_at")
    val attemptedAt: LocalDateTime = LocalDateTime.now()
)
