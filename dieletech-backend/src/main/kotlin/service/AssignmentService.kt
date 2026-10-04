package service

import com.dieletech.backend.dto.AssignmentAdminDTO
import com.dieletech.backend.dto.AssignmentDTO
import com.dieletech.backend.dto.CreateAssignmentDTO
import com.dieletech.backend.error.ForbiddenException
import com.dieletech.backend.error.NotFoundException
import com.dieletech.backend.model.Assignment
import com.dieletech.backend.model.AssignmentType
import com.dieletech.backend.model.Course
import com.dieletech.backend.model.Role
import com.dieletech.backend.repository.AssignmentRepository
import com.dieletech.backend.repository.CourseRepository
import com.dieletech.backend.repository.LessonRepository
import com.dieletech.backend.repository.PurchaseRepository
import com.dieletech.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * HU-37: ejercicios practicos por modulo.
 *
 * Cuatro reglas gobiernan este servicio y todas se comprueban en el
 * servidor, nunca en la interfaz:
 *
 *  1. El enunciado solo se entrega si el usuario compro el curso.
 *  2. Un modulo tiene cero o una tarea activa.
 *  3. La leccion referida tiene que pertenecer al mismo curso.
 *  4. Solo el instructor dueño del curso, o un ADMIN, puede crear,
 *     editar o archivar tareas.
 *
 * La cuarta no se resuelve con roles en SecurityConfig: un INSTRUCTOR
 * autenticado sigue siendo un extraño en el curso de otro, y eso solo
 * lo sabe el servicio que compara con Course.instructorEmail.
 */
@Service
class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val purchaseRepository: PurchaseRepository,
    private val userRepository: UserRepository
) {

    // ═══════════════════════════════════════════
    //  Lectura del estudiante
    // ═══════════════════════════════════════════

    /**
     * Tareas de un curso. Sin compra el enunciado no viaja: se devuelve el
     * titulo para que la ficha del curso pueda mostrar que existen, pero el
     * contenido queda bloqueado. Es la misma decision que HU-23 tomo con
     * videoUrl: ocultarlo solo en pantalla no protege nada.
     */
    fun listForStudent(courseId: Long, email: String?): List<AssignmentDTO> {
        courseRepository.findById(courseId).orElseThrow {
            NotFoundException("El curso no existe")
        }

        val normalized = email?.trim()?.lowercase()
        val owns = normalized != null &&
            purchaseRepository.existsPurchaseNormalized(courseId, normalized)

        val lessonTitles = lessonRepository
            .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)
            .associate { it.id to it.title }

        return assignmentRepository
            .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)
            .map { it.toStudentDTO(lessonTitles[it.lessonId], owns) }
    }

    /** Una tarea concreta. Sin compra no se entrega el enunciado. */
    fun getForStudent(assignmentId: Long, email: String?): AssignmentDTO {
        val assignment = assignmentRepository.findByIdAndActiveTrue(assignmentId)
            ?: throw NotFoundException("La tarea no existe")

        val normalized = email?.trim()?.lowercase()
        val owns = normalized != null &&
            purchaseRepository.existsPurchaseNormalized(assignment.courseId, normalized)

        val lessonTitle = assignment.lessonId
            ?.let { lessonRepository.findById(it).orElse(null)?.title }

        return assignment.toStudentDTO(lessonTitle, owns)
    }

    // ═══════════════════════════════════════════
    //  Gestion del instructor
    // ═══════════════════════════════════════════

    fun listForInstructor(courseId: Long, email: String): List<AssignmentAdminDTO> {
        val course = requireOwnedCourse(courseId, email)
        val lessonTitles = lessonRepository
            .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)
            .associate { it.id to it.title }

        return assignmentRepository
            .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId)
            .map { it.toAdminDTO(course.title, lessonTitles[it.lessonId]) }
    }

    @Transactional
    fun create(courseId: Long, dto: CreateAssignmentDTO, email: String): AssignmentAdminDTO {
        val course = requireOwnedCourse(courseId, email)
        validateLesson(courseId, dto.lessonId)
        requireFreeModule(courseId, dto.lessonId, excludingId = null)

        val saved = assignmentRepository.save(
            Assignment(
                courseId = courseId,
                lessonId = dto.lessonId,
                title = dto.title.trim(),
                statement = dto.statement.trim(),
                type = AssignmentType.valueOf(dto.type.uppercase()),
                maxScore = dto.maxScore,
                dueOffsetDays = dto.dueOffsetDays,
                orderIndex = if (dto.orderIndex > 0) dto.orderIndex
                             else assignmentRepository.countByCourseIdAndActiveTrue(courseId) + 1
            )
        )
        return saved.toAdminDTO(course.title, lessonTitle(saved.lessonId))
    }

    @Transactional
    fun update(assignmentId: Long, dto: CreateAssignmentDTO, email: String): AssignmentAdminDTO {
        val assignment = assignmentRepository.findByIdAndActiveTrue(assignmentId)
            ?: throw NotFoundException("La tarea no existe")
        val course = requireOwnedCourse(assignment.courseId, email)

        validateLesson(assignment.courseId, dto.lessonId)
        requireFreeModule(assignment.courseId, dto.lessonId, excludingId = assignment.id)

        assignment.lessonId = dto.lessonId
        assignment.title = dto.title.trim()
        assignment.statement = dto.statement.trim()
        assignment.type = AssignmentType.valueOf(dto.type.uppercase())
        assignment.maxScore = dto.maxScore
        assignment.dueOffsetDays = dto.dueOffsetDays
        if (dto.orderIndex > 0) assignment.orderIndex = dto.orderIndex
        assignment.updatedAt = LocalDateTime.now()

        assignmentRepository.save(assignment)
        return assignment.toAdminDTO(course.title, lessonTitle(assignment.lessonId))
    }

    /**
     * Archivado logico, igual que los cursos y las lecciones: las entregas
     * ya calificadas siguen existiendo y la trazabilidad del estudiante no
     * se pierde por retirar el enunciado.
     */
    @Transactional
    fun archive(assignmentId: Long, email: String) {
        val assignment = assignmentRepository.findByIdAndActiveTrue(assignmentId)
            ?: throw NotFoundException("La tarea no existe")
        requireOwnedCourse(assignment.courseId, email)
        assignment.active = false
        assignment.updatedAt = LocalDateTime.now()
        assignmentRepository.save(assignment)
    }

    // ═══════════════════════════════════════════
    //  Reglas compartidas
    // ═══════════════════════════════════════════

    /** Regla 4: dueño del curso o ADMIN. Cualquier otro recibe 403. */
    fun requireOwnedCourse(courseId: Long, email: String): Course {
        val course = courseRepository.findById(courseId).orElseThrow {
            NotFoundException("El curso no existe")
        }
        val user = userRepository.findByEmail(email.trim().lowercase()).orElse(null)
            ?: throw ForbiddenException("Tu cuenta no existe")

        if (user.role == Role.ADMIN) return course

        val owner = course.instructorEmail?.trim()?.lowercase()
        if (owner == null || owner != user.email.trim().lowercase()) {
            throw ForbiddenException("Este curso no esta a tu cargo")
        }
        return course
    }

    /** Regla 3: la leccion tiene que ser de este mismo curso. */
    private fun validateLesson(courseId: Long, lessonId: Long?) {
        if (lessonId == null) return
        val lesson = lessonRepository.findById(lessonId).orElseThrow {
            NotFoundException("La leccion no existe")
        }
        if (lesson.courseId != courseId) {
            throw IllegalArgumentException("La leccion pertenece a otro curso")
        }
    }

    /** Regla 2: cero o una tarea activa por modulo. */
    private fun requireFreeModule(courseId: Long, lessonId: Long?, excludingId: Long?) {
        if (lessonId == null) return
        val existing = assignmentRepository
            .findByCourseIdAndLessonIdAndActiveTrue(courseId, lessonId) ?: return
        if (existing.id != excludingId) {
            throw IllegalArgumentException("Este modulo ya tiene una tarea asignada")
        }
    }

    private fun lessonTitle(lessonId: Long?): String? =
        lessonId?.let { lessonRepository.findById(it).orElse(null)?.title }

    private fun Assignment.toStudentDTO(lessonTitle: String?, unlocked: Boolean) = AssignmentDTO(
        id = id,
        courseId = courseId,
        lessonId = lessonId,
        lessonTitle = lessonTitle,
        title = title,
        statement = if (unlocked) statement else null,
        type = type.name,
        maxScore = maxScore,
        dueOffsetDays = dueOffsetDays,
        orderIndex = orderIndex,
        locked = !unlocked
    )

    private fun Assignment.toAdminDTO(courseTitle: String, lessonTitle: String?) = AssignmentAdminDTO(
        id = id,
        courseId = courseId,
        courseTitle = courseTitle,
        lessonId = lessonId,
        lessonTitle = lessonTitle,
        title = title,
        statement = statement,
        type = type.name,
        maxScore = maxScore,
        dueOffsetDays = dueOffsetDays,
        orderIndex = orderIndex,
        active = active
    )
}
