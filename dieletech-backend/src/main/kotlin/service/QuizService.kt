package service

import com.dieletech.backend.dto.*
import com.dieletech.backend.model.Question
import com.dieletech.backend.model.QuizAttempt
import com.dieletech.backend.repository.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * HU-10: evaluacion final. Tres reglas gobiernan el acceso:
 *  1. El estudiante compro el curso.
 *  2. Completó todas las lecciones.
 *  3. El curso tiene preguntas publicadas.
 * El indice correcto nunca sale del backend antes de enviar el intento.
 */
@Service
class QuizService(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: QuizAttemptRepository,
    private val courseRepository: CourseRepository,
    private val purchaseRepository: PurchaseRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: LessonProgressRepository,
    private val certificateService: CertificateService,
    @Value("\${app.quiz.passing-score:70}") private val passingScore: Int
) {

    fun getQuiz(courseId: Long, email: String): QuizInfoDTO {
        val normalized = email.trim().lowercase()
        val course = courseRepository.findById(courseId).orElseThrow {
            RuntimeException("El curso no existe")
        }

        val owns = purchaseRepository.existsPurchaseNormalized(courseId, normalized)
        val totalLessons = lessonRepository.countByCourseIdAndActiveTrue(courseId)
        val completed = progressRepository.countCompletedByEmailAndCourse(normalized, courseId)
        val questions = questionRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)

        val attempts = attemptRepository.findAttempts(normalized, courseId)
        val passed = attempts.any { it.passed }
        val best = attempts.maxOfOrNull { it.score } ?: 0
        val certificate = certificateService.findForCourse(normalized, courseId)

        val blocked = when {
            !owns -> "Debes comprar el curso para presentar la evaluacion."
            questions.isEmpty() -> "Este curso todavia no tiene evaluacion publicada."
            totalLessons == 0 -> "Este curso todavia no tiene lecciones publicadas."
            completed < totalLessons ->
                "Completa las $totalLessons lecciones del curso para habilitar la evaluacion. " +
                "Llevas $completed."
            else -> null
        }

        return QuizInfoDTO(
            courseId = courseId,
            courseTitle = course.title,
            totalQuestions = questions.size,
            passingScore = passingScore,
            available = blocked == null,
            blockedReason = blocked,
            lessonsCompleted = completed,
            totalLessons = totalLessons,
            attempts = attempts.size,
            bestScore = best,
            passed = passed,
            certificateCode = certificate?.code,
            // Las preguntas solo viajan cuando la evaluacion esta habilitada.
            questions = if (blocked == null) questions.map { it.toDTO() } else emptyList()
        )
    }

    @Transactional
    fun submit(courseId: Long, email: String, dto: SubmitQuizDTO): QuizResultDTO {
        val normalized = email.trim().lowercase()
        val course = courseRepository.findById(courseId).orElseThrow {
            RuntimeException("El curso no existe")
        }

        if (!purchaseRepository.existsPurchaseNormalized(courseId, normalized)) {
            throw RuntimeException("Debes comprar el curso para presentar la evaluacion")
        }

        val totalLessons = lessonRepository.countByCourseIdAndActiveTrue(courseId)
        val completed = progressRepository.countCompletedByEmailAndCourse(normalized, courseId)
        if (totalLessons == 0 || completed < totalLessons) {
            throw RuntimeException("Debes completar todas las lecciones antes de presentar la evaluacion")
        }

        val questions = questionRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)
        if (questions.isEmpty()) {
            throw RuntimeException("Este curso no tiene evaluacion publicada")
        }

        val byId = questions.associateBy { it.id }
        val answers = dto.answers.associate { it.questionId to it.selectedIndex }

        // No se acepta un intento parcial: el puntaje seria enganoso.
        val missing = questions.filter { it.id !in answers }
        if (missing.isNotEmpty()) {
            throw RuntimeException("Debes responder las ${questions.size} preguntas. Faltan ${missing.size}.")
        }

        val results = questions.map { q ->
            val selected = answers[q.id] ?: -1
            val opts = q.optionList
            val valid = selected in opts.indices
            QuestionResultDTO(
                questionId = q.id,
                text = q.text,
                options = opts,
                selectedIndex = if (valid) selected else -1,
                correctIndex = q.correctIndex,
                correct = valid && selected == q.correctIndex,
                explanation = q.explanation
            )
        }

        val correct = results.count { it.correct }
        val score = (correct * 100) / questions.size
        val passed = score >= passingScore

        attemptRepository.save(
            QuizAttempt(
                userEmail = normalized,
                courseId = courseId,
                score = score,
                correctAnswers = correct,
                totalQuestions = questions.size,
                passed = passed,
                answers = dto.answers.joinToString("|") { "${it.questionId}:${it.selectedIndex}" }
            )
        )

        // El certificado se emite una sola vez, con el primer aprobado.
        var code: String? = certificateService.findForCourse(normalized, courseId)?.code
        if (passed && code == null) {
            code = certificateService.issue(normalized, course, score).code
        }

        val message = when {
            passed && code != null -> "Aprobaste con $score%. Tu certificado ya esta disponible."
            passed -> "Aprobaste con $score%."
            else -> "Obtuviste $score%. Necesitas $passingScore% para aprobar. Puedes intentarlo de nuevo."
        }

        return QuizResultDTO(
            score = score,
            correctAnswers = correct,
            totalQuestions = questions.size,
            passingScore = passingScore,
            passed = passed,
            message = message,
            certificateCode = if (passed) code else null,
            results = results
        )
    }

    // ── CRUD del instructor ──

    fun listForAdmin(courseId: Long): List<Map<String, Any?>> =
        questionRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId).map {
            mapOf(
                "id" to it.id,
                "text" to it.text,
                "options" to it.optionList,
                "correctIndex" to it.correctIndex,
                "explanation" to it.explanation,
                "orderIndex" to it.orderIndex
            )
        }

    @Transactional
    fun create(courseId: Long, dto: CreateQuestionDTO): Map<String, Any?> {
        validate(dto)
        val q = questionRepository.save(
            Question(
                courseId = courseId,
                text = dto.text.trim(),
                options = dto.options.joinToString("|") { it.trim() },
                correctIndex = dto.correctIndex,
                explanation = dto.explanation?.trim()?.ifBlank { null },
                orderIndex = if (dto.orderIndex > 0) dto.orderIndex
                             else questionRepository.countByCourseIdAndActiveTrue(courseId) + 1
            )
        )
        return mapOf("id" to q.id, "message" to "Pregunta creada")
    }

    @Transactional
    fun update(questionId: Long, dto: CreateQuestionDTO): Map<String, Any?> {
        validate(dto)
        val q = questionRepository.findById(questionId).orElseThrow {
            RuntimeException("La pregunta no existe")
        }
        q.text = dto.text.trim()
        q.options = dto.options.joinToString("|") { it.trim() }
        q.correctIndex = dto.correctIndex
        q.explanation = dto.explanation?.trim()?.ifBlank { null }
        if (dto.orderIndex > 0) q.orderIndex = dto.orderIndex
        questionRepository.save(q)
        return mapOf("id" to q.id, "message" to "Pregunta actualizada")
    }

    @Transactional
    fun delete(questionId: Long) {
        val q = questionRepository.findById(questionId).orElseThrow {
            RuntimeException("La pregunta no existe")
        }
        q.active = false
        questionRepository.save(q)
    }

    private fun validate(dto: CreateQuestionDTO) {
        val opts = dto.options.map { it.trim() }.filter { it.isNotEmpty() }
        if (opts.size < 2) throw RuntimeException("Registra al menos 2 alternativas con texto")
        if (opts.size > 5) throw RuntimeException("Maximo 5 alternativas")
        if (dto.correctIndex !in opts.indices) {
            throw RuntimeException("La alternativa correcta debe ser una de las registradas")
        }
        if (opts.distinct().size != opts.size) {
            throw RuntimeException("Las alternativas no pueden repetirse")
        }
    }

    private fun Question.toDTO() = QuestionDTO(
        id = id,
        text = text,
        options = optionList,
        orderIndex = orderIndex
    )
}
