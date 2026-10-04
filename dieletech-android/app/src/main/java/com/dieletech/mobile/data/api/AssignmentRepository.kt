package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.*

/**
 * HU-37 / HU-38 — tareas del curso y entregas del estudiante.
 *
 * Sin mock: si el backend falla, se devuelve lista vacia o Result.failure.
 * Mostrar datos inventados romperia la trazabilidad que es el nucleo de la
 * epica de tareas.
 */
object AssignmentRepository {
    private val api = RetrofitClient.api

    suspend fun listForCourse(courseId: Long): List<AssignmentNet> = try {
        api.assignmentsByCourse(courseId)
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun myCourseView(courseId: Long): List<StudentAssignmentNet> = try {
        api.myCourseAssignments(courseId)
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun myAssignment(id: Long): StudentAssignmentNet? = try {
        api.myAssignment(id)
    } catch (_: Exception) {
        null
    }

    suspend fun submit(assignmentId: Long, content: String, fileUrl: String?, draft: Boolean): Result<SubmissionNet> = try {
        Result.success(api.submitAssignment(assignmentId, SubmitAssignmentRequest(content, fileUrl, draft)))
    } catch (e: retrofit2.HttpException) {
        val body = e.response()?.errorBody()?.string().orEmpty()
        val msg = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1)
            ?: body.ifBlank { "No se pudo guardar la entrega (HTTP ${e.code()})" }
        Result.failure(IllegalStateException(msg))
    } catch (_: Exception) {
        Result.failure(IllegalStateException("No hay conexion con el servidor"))
    }

    // ── Vista admin / instructor ──
    suspend fun listAdmin(courseId: Long): List<AssignmentAdminNet> = try {
        api.adminAssignments(courseId)
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun createAdmin(courseId: Long, req: CreateAssignmentRequest): Result<AssignmentAdminNet> = try {
        Result.success(api.adminCreateAssignment(courseId, req))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateAdmin(id: Long, req: CreateAssignmentRequest): Result<AssignmentAdminNet> = try {
        Result.success(api.adminUpdateAssignment(id, req))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAdmin(id: Long): Result<Unit> = try {
        api.adminDeleteAssignment(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
