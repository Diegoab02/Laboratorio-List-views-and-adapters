package service

import com.dieletech.backend.dto.*
import com.dieletech.backend.model.Lesson
import com.dieletech.backend.model.LessonProgress
import com.dieletech.backend.repository.LessonProgressRepository
import com.dieletech.backend.repository.LessonRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LessonService(
    private val lessonRepository: LessonRepository,
    private val progressRepository: LessonProgressRepository,
    private val purchaseRepository: com.dieletech.backend.repository.PurchaseRepository
) {

    // ── HU-07: Obtener lecciones de un curso ──

    /**
     * Devuelve las lecciones del curso. Si el usuario no compro el curso,
     * el videoUrl de las lecciones que no son preview gratuito viaja en null
     * y quedan marcadas como locked: el contenido de pago nunca sale del
     * backend sin compra, aunque el cliente manipule la interfaz.
     */
    fun getLessonsByCourse(courseId: Long, email: String? = null): List<LessonDTO> {
        val normalized = email?.trim()?.lowercase()
        val hasPurchased = !normalized.isNullOrBlank() &&
            purchaseRepository.existsPurchaseNormalized(courseId, normalized)

        return lessonRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)
            .map { lesson ->
                val unlocked = hasPurchased || lesson.freePreview
                toLessonDTO(lesson).copy(
                    videoUrl = if (unlocked) lesson.videoUrl else null,
                    materialUrl = if (unlocked) lesson.materialUrl else null,
                    locked = !unlocked
                )
            }
    }

    // ── HU-07: Actualizar progreso de una lección ──

    fun updateProgress(lessonId: Long, dto: LessonProgressUpdateDTO): LessonProgressDTO {
        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { RuntimeException("Lección no encontrada: $lessonId") }

        val email = dto.email.trim().lowercase()

        // Solo se registra progreso de cursos efectivamente comprados.
        if (!purchaseRepository.existsPurchaseNormalized(lesson.courseId, email)) {
            throw RuntimeException("Debes comprar el curso para registrar tu progreso")
        }

        var progress = progressRepository.findByUserEmailAndLessonId(email, lessonId)

        if (progress == null) {
            progress = LessonProgress(
                userEmail = email,
                lessonId = lessonId,
                courseId = lesson.courseId
            )
        }

        dto.completed?.let {
            progress.completed = it
            if (it) progress.completedAt = LocalDateTime.now()
        }
        dto.lastPositionSeconds?.let { progress.lastPositionSeconds = it }
        progress.updatedAt = LocalDateTime.now()

        progressRepository.save(progress)

        return LessonProgressDTO(
            lessonId = progress.lessonId,
            completed = progress.completed,
            lastPositionSeconds = progress.lastPositionSeconds,
            completedAt = progress.completedAt
        )
    }

    // ── HU-08: Progreso general del curso ──

    fun getCourseProgress(courseId: Long, email: String): CourseProgressDTO {
        val normalizedEmail = email.trim().lowercase()
        val totalLessons = lessonRepository.countByCourseIdAndActiveTrue(courseId)
        val completedLessons = progressRepository.countCompletedByEmailAndCourse(normalizedEmail, courseId)
        val lessonProgress = progressRepository.findByUserEmailAndCourseId(normalizedEmail, courseId)
            .map { LessonProgressDTO(it.lessonId, it.completed, it.lastPositionSeconds, it.completedAt) }

        val pct = if (totalLessons > 0) (completedLessons * 100) / totalLessons else 0

        return CourseProgressDTO(
            courseId = courseId,
            totalLessons = totalLessons,
            completedLessons = completedLessons,
            progressPercent = pct,
            lessonProgress = lessonProgress
        )
    }

    // ── HU-09: CRUD de lecciones (Instructor) ──

    fun createLesson(courseId: Long, dto: CreateLessonDTO): LessonDTO {
        val lesson = Lesson(
            courseId = courseId,
            title = dto.title,
            description = dto.description,
            videoUrl = dto.videoUrl,
            orderIndex = dto.orderIndex,
            durationMinutes = dto.durationMinutes,
            contentType = dto.contentType,
            materialUrl = dto.materialUrl,
            freePreview = dto.freePreview
        )
        return toLessonDTO(lessonRepository.save(lesson))
    }

    fun updateLesson(lessonId: Long, dto: CreateLessonDTO): LessonDTO {
        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { RuntimeException("Lección no encontrada: $lessonId") }

        lesson.title = dto.title
        lesson.description = dto.description
        lesson.videoUrl = dto.videoUrl
        lesson.orderIndex = dto.orderIndex
        lesson.durationMinutes = dto.durationMinutes
        lesson.contentType = dto.contentType
        lesson.materialUrl = dto.materialUrl
        lesson.freePreview = dto.freePreview

        return toLessonDTO(lessonRepository.save(lesson))
    }

    fun deleteLesson(lessonId: Long) {
        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { RuntimeException("Lección no encontrada: $lessonId") }
        lesson.active = false
        lessonRepository.save(lesson)
    }

    private fun toLessonDTO(lesson: Lesson) = LessonDTO(
        id = lesson.id,
        courseId = lesson.courseId,
        title = lesson.title,
        description = lesson.description,
        videoUrl = lesson.videoUrl,
        orderIndex = lesson.orderIndex,
        durationMinutes = lesson.durationMinutes,
        contentType = lesson.contentType,
        materialUrl = lesson.materialUrl,
        freePreview = lesson.freePreview,
        locked = false
    )
}
