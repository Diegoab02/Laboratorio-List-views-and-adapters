package service

import com.dieletech.backend.dto.AssignmentDTO
import com.dieletech.backend.dto.StudentAssignmentDTO
import com.dieletech.backend.dto.SubmissionDTO
import com.dieletech.backend.dto.SubmitAssignmentDTO
import com.dieletech.backend.error.ConflictException
import com.dieletech.backend.error.ForbiddenException
import com.dieletech.backend.error.NotFoundException
import com.dieletech.backend.model.Assignment
import com.dieletech.backend.model.AssignmentType
import com.dieletech.backend.model.Submission
import com.dieletech.backend.model.SubmissionStatus
import com.dieletech.backend.repository.AssignmentRepository
import com.dieletech.backend.repository.LessonProgressRepository
import com.dieletech.backend.repository.LessonRepository
import com.dieletech.backend.repository.PurchaseRepository
import com.dieletech.backend.repository.SubmissionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * HU-38: entrega del estudiante.
 *
 * Cinco reglas, todas en el servidor:
 *
 *  1. Hay que haber comprado el curso.
 *  2. El modulo tiene que estar completo. Si la tarea es del curso
 *     entero, lo tienen que estar todas las lecciones.
 *  3. Una sola entrega por estudiante y tarea.
 *  4. Se puede reenviar mientras no este calificada; despues queda
 *     cerrada, porque la nota ya se escribio contra ese contenido.
 *  5. Contenido no vacio y dentro del limite; una tarea de tipo FILE
 *     exige ademas un enlace http o https valido.
 *
 * El plazo se calcula desde la fecha de compra, no desde la creacion de
 * la tarea: cada estudiante entra al curso cuando quiere. Entregar
 * tarde no se bloquea, se registra. Bloquearlo castigaria al estudiante
 * por un dato que el instructor puede cambiar en cualquier momento.
 */
@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val assignmentRepository: AssignmentRepository,
    private val purchaseRepository: PurchaseRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: LessonProgressRepository
) {

    // ═══════════════════════════════════════════
    //  Lectura
    // ═══════════════════════════════════════════

    /** La tarea con la entrega del estudiante y su estado de habilitacion. */
    fun getStudentView(assignmentId: Long, email: String): StudentAssignmentDTO {
        val normalized = email.trim().lowercase()
        val assignment = assignmentRepository.findByIdAndActiveTrue(assignmentId)
            ?: throw NotFoundException("La tarea no existe")

        val owns = purchaseRepository.existsPurchaseNormalized(assignment.courseId, normalized)
        val submission = submissionRepository.findMine(assignmentId, normalized)
        val due = dueDateFor(assignment, normalized)
        val blocked = blockedReason(assignment, normalized, owns, submission)

        return StudentAssignmentDTO(
            assignment = toAssignmentDTO(assignment, unlocked = owns),
            submission = submission?.toDTO(),
            canSubmit = blocked == null,
            blockedReason = blocked,
            dueDate = submission?.dueDate ?: due,
            daysLeft = due?.let { ChronoUnit.DAYS.between(LocalDateTime.now(), it) }
        )
    }

    /** Todas las tareas de un curso con el estado de entrega del estudiante. */
    fun listStudentView(courseId: Long, email: String): List<StudentAssignmentDTO> {
        val normalized = email.trim().lowercase()
        val owns = purchaseRepository.existsPurchaseNormalized(courseId, normalized)
        val mine = submissionRepository.findMineByCourse(courseId, normalized)
            .associateBy { it.assignmentId }
        val due0 = purchaseDate(courseId, normalized)

        return assignmentRepository
            .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)
            .map { a ->
                val submission = mine[a.id]
                val due = due0?.plusDays(a.dueOffsetDays.toLong())
                val blocked = blockedReason(a, normalized, owns, submission)
                StudentAssignmentDTO(
                    assignment = toAssignmentDTO(a, unlocked = owns),
                    submission = submission?.toDTO(),
                    canSubmit = blocked == null,
                    blockedReason = blocked,
                    dueDate = submission?.dueDate ?: due,
                    daysLeft = due?.let { ChronoUnit.DAYS.between(LocalDateTime.now(), it) }
                )
            }
    }

    // ═══════════════════════════════════════════
    //  Escritura
    // ═══════════════════════════════════════════

    /**
     * Guarda un borrador o entrega de forma definitiva.
     *
     * Es la misma operacion a proposito: el estudiante escribe, guarda y
     * vuelve cuantas veces quiera sobre la misma fila, y solo cuando
     * pulsa entregar cambia el estado. Dos endpoints distintos habrian
     * duplicado las cinco comprobaciones.
     */
    @Transactional
    fun save(assignmentId: Long, email: String, dto: SubmitAssignmentDTO): SubmissionDTO {
        val normalized = email.trim().lowercase()
        val assignment = assignmentRepository.findByIdAndActiveTrue(assignmentId)
            ?: throw NotFoundException("La tarea no existe")

        // Regla 1
        if (!purchaseRepository.existsPurchaseNormalized(assignment.courseId, normalized)) {
            throw ForbiddenException("Debes comprar el curso para entregar esta tarea")
        }

        // Regla 2
        requireModuleCompleted(assignment, normalized)

        // Regla 5
        val content = dto.content.trim()
        if (content.isEmpty()) {
            throw IllegalArgumentException("La entrega no puede estar vacia")
        }
        val fileUrl = dto.fileUrl?.trim()?.ifBlank { null }
        if (assignment.type == AssignmentType.FILE && !dto.draft) {
            if (fileUrl == null) {
                throw IllegalArgumentException("Esta tarea se entrega con un enlace a tu archivo o repositorio")
            }
            if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
                throw IllegalArgumentException("El enlace debe empezar por http:// o https://")
            }
        }

        // Reglas 3 y 4: una sola fila, cerrada al calificar
        val existing = submissionRepository.findMine(assignmentId, normalized)
        if (existing != null && existing.status == SubmissionStatus.GRADED) {
            throw ConflictException(
                "Esta entrega ya fue calificada con ${fmt(existing.score)} puntos y no se puede modificar"
            )
        }

        val due = dueDateFor(assignment, normalized)
        val now = LocalDateTime.now()
        val submission = existing ?: Submission(
            assignmentId = assignmentId,
            courseId = assignment.courseId,
            userEmail = normalized
        )

        submission.content = content
        submission.fileUrl = fileUrl
        submission.updatedAt = now

        if (dto.draft) {
            submission.status = SubmissionStatus.DRAFT
            submission.submittedAt = null
        } else {
            submission.status = SubmissionStatus.SUBMITTED
            submission.submittedAt = now
            submission.dueDate = due
            submission.lateSubmission = due != null && now.isAfter(due)
        }

        return submissionRepository.save(submission).toDTO()
    }

    // ═══════════════════════════════════════════
    //  Reglas compartidas
    // ═══════════════════════════════════════════

    private fun requireModuleCompleted(assignment: Assignment, email: String) {
        val lessonId = assignment.lessonId
        if (lessonId != null) {
            if (!progressRepository.isLessonCompleted(email, lessonId)) {
                val title = lessonRepository.findById(lessonId).orElse(null)?.title
                throw ForbiddenException(
                    if (title != null) "Completa la leccion \"$title\" antes de entregar esta tarea"
                    else "Completa el modulo antes de entregar esta tarea"
                )
            }
            return
        }

        val total = lessonRepository.countByCourseIdAndActiveTrue(assignment.courseId)
        val done = progressRepository.countCompletedByEmailAndCourse(email, assignment.courseId)
        if (total == 0 || done < total) {
            throw ForbiddenException(
                "Esta tarea cierra el curso: completa las $total lecciones antes de entregarla. Llevas $done."
            )
        }
    }

    /** Motivo legible por el que no se puede entregar, o null si se puede. */
    private fun blockedReason(
        assignment: Assignment,
        email: String,
        owns: Boolean,
        submission: Submission?
    ): String? = when {
        !owns -> "Debes comprar el curso para entregar esta tarea."
        submission?.status == SubmissionStatus.GRADED ->
            "Ya fue calificada con ${fmt(submission.score)} puntos. No admite cambios."
        else -> runCatching { requireModuleCompleted(assignment, email) }
            .exceptionOrNull()?.message
    }

    private fun purchaseDate(courseId: Long, email: String): LocalDateTime? =
        purchaseRepository.findByUserEmailNormalized(email)
            .filter { it.courseId == courseId }
            .minByOrNull { it.purchasedAt }
            ?.purchasedAt

    private fun dueDateFor(assignment: Assignment, email: String): LocalDateTime? =
        purchaseDate(assignment.courseId, email)?.plusDays(assignment.dueOffsetDays.toLong())

    private fun fmt(score: Double?): String =
        if (score == null) "0" else if (score % 1.0 == 0.0) score.toInt().toString() else score.toString()

    private fun toAssignmentDTO(a: Assignment, unlocked: Boolean) = AssignmentDTO(
        id = a.id,
        courseId = a.courseId,
        lessonId = a.lessonId,
        lessonTitle = a.lessonId?.let { lessonRepository.findById(it).orElse(null)?.title },
        title = a.title,
        statement = if (unlocked) a.statement else null,
        type = a.type.name,
        maxScore = a.maxScore,
        dueOffsetDays = a.dueOffsetDays,
        orderIndex = a.orderIndex,
        locked = !unlocked
    )

    private fun Submission.toDTO() = SubmissionDTO(
        id = id,
        assignmentId = assignmentId,
        courseId = courseId,
        content = content,
        fileUrl = fileUrl,
        status = status.name,
        submittedAt = submittedAt,
        dueDate = dueDate,
        late = lateSubmission,
        score = score,
        feedback = feedback,
        gradedAt = gradedAt,
        gradedBy = gradedBy
    )
}
