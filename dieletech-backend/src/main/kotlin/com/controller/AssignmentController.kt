package com.controller

import com.dieletech.backend.dto.AssignmentDTO
import com.dieletech.backend.dto.StudentAssignmentDTO
import com.dieletech.backend.dto.SubmissionDTO
import com.dieletech.backend.dto.SubmitAssignmentDTO
import com.dieletech.backend.error.ForbiddenException
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import service.AssignmentService
import service.SubmissionService

/**
 * HU-37: lectura de tareas por parte del estudiante.
 *
 * No hay try/catch aqui a proposito: NotFoundException y ForbiddenException
 * ya se traducen a 404 y 403 en GlobalExceptionHandler. Capturarlas en el
 * controlador las degradaria otra vez a 400, que es justo el problema que
 * se corrigio en este sprint.
 */
@RestController
@CrossOrigin(origins = ["*"])
class AssignmentController(
    private val assignmentService: AssignmentService,
    private val submissionService: SubmissionService
) {

    /** El correo sale del JWT, nunca de un parametro que el cliente controle. */
    private fun requireEmail(auth: Authentication?): String =
        auth?.name ?: throw ForbiddenException("Inicia sesion para continuar")

    /** Tareas del curso. Sin compra llegan bloqueadas y sin enunciado. */
    @GetMapping("/api/courses/{courseId}/assignments")
    fun listByCourse(
        @PathVariable courseId: Long,
        authentication: Authentication?
    ): ResponseEntity<List<AssignmentDTO>> =
        ResponseEntity.ok(assignmentService.listForStudent(courseId, authentication?.name))

    /** Una tarea concreta, con el mismo bloqueo por compra. */
    @GetMapping("/api/assignments/{assignmentId}")
    fun getOne(
        @PathVariable assignmentId: Long,
        authentication: Authentication?
    ): ResponseEntity<AssignmentDTO> =
        ResponseEntity.ok(assignmentService.getForStudent(assignmentId, authentication?.name))

    // ═══════════════════════════════════════════
    //  HU-38 · Entregas del estudiante
    // ═══════════════════════════════════════════

    /** Tareas del curso con mi entrega y si puedo entregar ahora. */
    @GetMapping("/api/courses/{courseId}/assignments/mine")
    fun myCourseAssignments(
        @PathVariable courseId: Long,
        authentication: Authentication?
    ): ResponseEntity<List<StudentAssignmentDTO>> =
        ResponseEntity.ok(submissionService.listStudentView(courseId, requireEmail(authentication)))

    /** Una tarea con mi entrega. */
    @GetMapping("/api/assignments/{assignmentId}/mine")
    fun myAssignment(
        @PathVariable assignmentId: Long,
        authentication: Authentication?
    ): ResponseEntity<StudentAssignmentDTO> =
        ResponseEntity.ok(submissionService.getStudentView(assignmentId, requireEmail(authentication)))

    /**
     * Guarda borrador (draft = true) o entrega definitiva (draft = false).
     * Una sola ruta porque son la misma fila y las mismas cinco reglas.
     */
    @PostMapping("/api/assignments/{assignmentId}/submission")
    fun submit(
        @PathVariable assignmentId: Long,
        @Valid @RequestBody dto: SubmitAssignmentDTO,
        authentication: Authentication?
    ): ResponseEntity<SubmissionDTO> =
        ResponseEntity.ok(submissionService.save(assignmentId, requireEmail(authentication), dto))
}
