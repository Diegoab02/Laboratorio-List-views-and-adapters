package com.dieletech.backend.dto

import jakarta.validation.constraints.*

/** Pregunta tal como la ve el estudiante: sin el indice correcto. */
data class QuestionDTO(
    val id: Long,
    val text: String,
    val options: List<String>,
    val orderIndex: Int
)

/** Estado de la evaluacion antes de presentarla. */
data class QuizInfoDTO(
    val courseId: Long,
    val courseTitle: String,
    val totalQuestions: Int,
    val passingScore: Int,
    /** Si el estudiante puede presentarla ahora mismo. */
    val available: Boolean,
    /** Motivo cuando no esta disponible, en lenguaje del usuario. */
    val blockedReason: String?,
    val lessonsCompleted: Int,
    val totalLessons: Int,
    val attempts: Int,
    val bestScore: Int,
    val passed: Boolean,
    val certificateCode: String?,
    val questions: List<QuestionDTO>
)

data class SubmitAnswerDTO(
    @field:NotNull(message = "Falta el identificador de la pregunta")
    val questionId: Long,

    @field:NotNull(message = "Falta la respuesta")
    @field:Min(value = 0, message = "Respuesta invalida")
    val selectedIndex: Int
)

data class SubmitQuizDTO(
    @field:NotEmpty(message = "Debes responder todas las preguntas")
    val answers: List<SubmitAnswerDTO>
)

/** Revision de una pregunta despues de enviar el intento. */
data class QuestionResultDTO(
    val questionId: Long,
    val text: String,
    val options: List<String>,
    val selectedIndex: Int,
    val correctIndex: Int,
    val correct: Boolean,
    val explanation: String?
)

data class QuizResultDTO(
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val passingScore: Int,
    val passed: Boolean,
    val message: String,
    /** Codigo del certificado cuando el intento aprueba. */
    val certificateCode: String?,
    val results: List<QuestionResultDTO>
)

/** CRUD de preguntas para el instructor. */
data class CreateQuestionDTO(
    @field:NotBlank(message = "El enunciado es obligatorio")
    @field:Size(min = 10, message = "El enunciado debe tener al menos 10 caracteres")
    val text: String,

    @field:NotEmpty(message = "Debes registrar las alternativas")
    @field:Size(min = 2, max = 5, message = "Entre 2 y 5 alternativas")
    val options: List<String>,

    @field:NotNull(message = "Indica cual es la alternativa correcta")
    @field:Min(value = 0, message = "Alternativa correcta invalida")
    val correctIndex: Int,

    val explanation: String? = null,
    val orderIndex: Int = 0
)
