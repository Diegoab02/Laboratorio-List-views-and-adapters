package com.controller

import com.dieletech.backend.dto.*
import com.dieletech.backend.model.Course
import com.dieletech.backend.model.Role
import com.dieletech.backend.repository.CourseRepository
import com.dieletech.backend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import service.AdminMetricsService
import service.AssignmentService
import service.CourseService
import service.LessonService
import service.PurchaseService
import service.QuizService
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = ["*"])
class AdminController(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val lessonService: LessonService,
    private val courseService: CourseService,
    private val purchaseService: PurchaseService,
    private val metricsService: AdminMetricsService,
    private val quizService: QuizService,
    private val assignmentService: AssignmentService
) {

    // ═══════════════════════════════════════════
    //  Dashboard con metricas reales (HU-09, HU-13)
    // ═══════════════════════════════════════════

    /**
     * ADMIN ve toda la plataforma; INSTRUCTOR solo sus cursos.
     * El rol se lee del JWT, no de un parametro manipulable por el cliente.
     */
    @GetMapping("/dashboard")
    fun dashboard(authentication: Authentication?): ResponseEntity<AdminDashboardDTO> {
        val email = authentication?.name
        val user = email?.let { userRepository.findByEmail(it.lowercase()).orElse(null) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return when (user.role) {
            Role.ADMIN -> ResponseEntity.ok(metricsService.buildDashboard(null))
            Role.INSTRUCTOR -> ResponseEntity.ok(metricsService.buildDashboard(user.email))
            else -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    /** Gestion de usuarios: exclusivo ADMIN. */
    @GetMapping("/users")
    fun listUsers(authentication: Authentication?): ResponseEntity<List<UserAdminDTO>> {
        val user = authentication?.name?.let { userRepository.findByEmail(it.lowercase()).orElse(null) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        if (user.role != Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        return ResponseEntity.ok(metricsService.listUsers())
    }

    /** Cambio de rol de un usuario: exclusivo ADMIN. */
    @PutMapping("/users/{id}/role")
    fun changeRole(
        @PathVariable id: Long,
        @RequestBody body: Map<String, String>,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        val actor = authentication?.name?.let { userRepository.findByEmail(it.lowercase()).orElse(null) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        if (actor.role != Role.ADMIN) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        val target = userRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (target.id == actor.id) {
            return ResponseEntity.badRequest().body("No puedes cambiar tu propio rol")
        }

        val newRole = runCatching { Role.valueOf(body["role"]?.uppercase() ?: "") }.getOrNull()
            ?: return ResponseEntity.badRequest().body("Rol invalido. Usa STUDENT, INSTRUCTOR o ADMIN")

        target.role = newRole
        userRepository.save(target)
        return ResponseEntity.ok(mapOf("message" to "Rol actualizado a ${newRole.name}"))
    }

    /** Matriculados de un curso con su progreso real. */
    @GetMapping("/courses/{courseId}/students")
    fun courseStudents(@PathVariable courseId: Long): ResponseEntity<List<EnrolledStudentDTO>> =
        ResponseEntity.ok(purchaseService.getEnrolledStudents(courseId))

    /** Recalcula los contadores de matriculados desde la tabla de compras. */
    @PostMapping("/maintenance/recalculate-counts")
    fun recalculate(): ResponseEntity<Map<String, Any>> {
        val fixed = purchaseService.recalculateAllStudentCounts()
        return ResponseEntity.ok(mapOf("corrected" to fixed, "message" to "Contadores sincronizados"))
    }

    // ═══════════════════════════════════════════
    //  CRUD de Cursos (HU-09)
    // ═══════════════════════════════════════════

    @PostMapping("/courses")
    fun createCourse(
        @RequestBody dto: CreateCourseDTO,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        validate(dto)?.let { return ResponseEntity.badRequest().body(it) }

        val owner = authentication?.name?.let { userRepository.findByEmail(it.lowercase()).orElse(null) }

        val course = Course(
            title = dto.title.trim(),
            description = dto.description.trim(),
            longDescription = dto.longDescription,
            learningObjectives = dto.learningObjectives,
            targetAudience = dto.targetAudience,
            technology = dto.technology.trim(),
            level = dto.level.trim(),
            price = dto.price,
            duration = dto.duration,
            capacity = dto.capacity,
            imageUrl = dto.imageUrl,
            curriculum = dto.curriculum,
            prerequisites = dto.prerequisites,
            previewVideoUrl = dto.previewVideoUrl,
            instructorName = dto.instructorName ?: owner?.name,
            instructorEmail = owner?.email
        )
        val saved = courseRepository.save(course)
        val body = courseService.getCourseById(saved.id) ?: return ResponseEntity.internalServerError().body("No se pudo leer el curso creado")
        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }

    @PutMapping("/courses/{id}")
    fun updateCourse(@PathVariable id: Long, @RequestBody dto: CreateCourseDTO): ResponseEntity<Any> {
        validate(dto)?.let { return ResponseEntity.badRequest().body(it) }

        val course = courseRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        // No se puede reducir el cupo por debajo de los ya matriculados.
        if (dto.capacity < course.studentCount) {
            return ResponseEntity.badRequest()
                .body("El cupo no puede ser menor a los ${course.studentCount} estudiantes ya matriculados")
        }

        course.title = dto.title.trim()
        course.description = dto.description.trim()
        course.longDescription = dto.longDescription
        course.learningObjectives = dto.learningObjectives
        course.targetAudience = dto.targetAudience
        course.technology = dto.technology.trim()
        course.level = dto.level.trim()
        course.price = dto.price
        course.duration = dto.duration
        course.capacity = dto.capacity
        course.imageUrl = dto.imageUrl
        course.curriculum = dto.curriculum
        course.prerequisites = dto.prerequisites
        course.previewVideoUrl = dto.previewVideoUrl
        dto.instructorName?.let { course.instructorName = it }
        course.updatedAt = LocalDateTime.now()

        courseRepository.save(course)
        val body = courseService.getCourseById(id) ?: return ResponseEntity.internalServerError().body("No se pudo leer el curso actualizado")
        return ResponseEntity.ok(body)
    }

    /** Baja logica: conserva el historial de compras. */
    @DeleteMapping("/courses/{id}")
    fun deleteCourse(@PathVariable id: Long): ResponseEntity<Any> {
        val course = courseRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (course.studentCount > 0) {
            course.active = false
            courseRepository.save(course)
            return ResponseEntity.ok(
                mapOf("message" to "Curso archivado: tiene ${course.studentCount} estudiantes matriculados")
            )
        }
        course.active = false
        courseRepository.save(course)
        return ResponseEntity.noContent().build()
    }

    /** Reglas de negocio del formulario de curso. */
    private fun validate(dto: CreateCourseDTO): String? = when {
        dto.title.isBlank() -> "El titulo es obligatorio"
        dto.title.trim().length < 5 -> "El titulo debe tener al menos 5 caracteres"
        dto.description.isBlank() -> "La descripcion es obligatoria"
        dto.description.trim().length < 20 -> "La descripcion debe tener al menos 20 caracteres"
        dto.technology.isBlank() -> "La tecnologia es obligatoria"
        dto.level !in listOf("Principiante", "Intermedio", "Avanzado") ->
            "El nivel debe ser Principiante, Intermedio o Avanzado"
        dto.price < 0 -> "El precio no puede ser negativo"
        dto.duration <= 0 -> "La duracion debe ser mayor a cero"
        dto.capacity <= 0 -> "El cupo debe ser mayor a cero"
        else -> null
    }

    // ═══════════════════════════════════════════
    //  Evaluacion final (HU-10)
    // ═══════════════════════════════════════════

    @GetMapping("/courses/{courseId}/questions")
    fun listQuestions(@PathVariable courseId: Long): ResponseEntity<Any> =
        ResponseEntity.ok(quizService.listForAdmin(courseId))

    @PostMapping("/courses/{courseId}/questions")
    fun createQuestion(
        @PathVariable courseId: Long,
        @RequestBody dto: CreateQuestionDTO
    ): ResponseEntity<Any> = try {
        ResponseEntity.status(HttpStatus.CREATED).body(quizService.create(courseId, dto))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
    }

    @PutMapping("/questions/{questionId}")
    fun updateQuestion(
        @PathVariable questionId: Long,
        @RequestBody dto: CreateQuestionDTO
    ): ResponseEntity<Any> = try {
        ResponseEntity.ok(quizService.update(questionId, dto))
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
    }

    @DeleteMapping("/questions/{questionId}")
    fun deleteQuestion(@PathVariable questionId: Long): ResponseEntity<Any> = try {
        quizService.delete(questionId)
        ResponseEntity.noContent().build()
    } catch (e: Exception) {
        ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
    }

    // ═══════════════════════════════════════════
    //  CRUD de Lecciones (HU-09)
    // ═══════════════════════════════════════════

    @PostMapping("/courses/{courseId}/lessons")
    fun createLesson(
        @PathVariable courseId: Long,
        @RequestBody dto: CreateLessonDTO
    ): ResponseEntity<Any> {
        if (dto.title.isBlank()) return ResponseEntity.badRequest().body("El titulo de la leccion es obligatorio")
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(courseId, dto))
    }

    @PutMapping("/lessons/{lessonId}")
    fun updateLesson(
        @PathVariable lessonId: Long,
        @RequestBody dto: CreateLessonDTO
    ): ResponseEntity<Any> = try {
        if (dto.title.isBlank()) ResponseEntity.badRequest().body("El titulo de la leccion es obligatorio")
        else ResponseEntity.ok(lessonService.updateLesson(lessonId, dto))
    } catch (e: Exception) {
        ResponseEntity.notFound().build()
    }

    @DeleteMapping("/lessons/{lessonId}")
    fun deleteLesson(@PathVariable lessonId: Long): ResponseEntity<Void> = try {
        lessonService.deleteLesson(lessonId)
        ResponseEntity.noContent().build()
    } catch (e: Exception) {
        ResponseEntity.notFound().build()
    }

    // ═══════════════════════════════════════════
    //  CRUD de Tareas practicas (HU-37)
    // ═══════════════════════════════════════════

    /*
     * La pertenencia del curso no la resuelve SecurityConfig. Un
     * INSTRUCTOR autenticado pasa el filtro de rol, pero sigue siendo un
     * extraño en el curso de otro: eso lo comprueba AssignmentService
     * contra Course.instructorEmail y responde 403.
     */

    @GetMapping("/courses/{courseId}/assignments")
    fun listAssignments(
        @PathVariable courseId: Long,
        authentication: Authentication
    ): ResponseEntity<List<AssignmentAdminDTO>> =
        ResponseEntity.ok(assignmentService.listForInstructor(courseId, authentication.name))

    @PostMapping("/courses/{courseId}/assignments")
    fun createAssignment(
        @PathVariable courseId: Long,
        @Valid @RequestBody dto: CreateAssignmentDTO,
        authentication: Authentication
    ): ResponseEntity<AssignmentAdminDTO> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(assignmentService.create(courseId, dto, authentication.name))

    @PutMapping("/assignments/{assignmentId}")
    fun updateAssignment(
        @PathVariable assignmentId: Long,
        @Valid @RequestBody dto: CreateAssignmentDTO,
        authentication: Authentication
    ): ResponseEntity<AssignmentAdminDTO> =
        ResponseEntity.ok(assignmentService.update(assignmentId, dto, authentication.name))

    /** Archivado logico: las entregas ya hechas conservan su trazabilidad. */
    @DeleteMapping("/assignments/{assignmentId}")
    fun archiveAssignment(
        @PathVariable assignmentId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        assignmentService.archive(assignmentId, authentication.name)
        return ResponseEntity.noContent().build()
    }
}
