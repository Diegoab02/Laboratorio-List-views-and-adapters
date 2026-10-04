package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.*

/** HU-09 + HU-13 — gestión de cursos, lecciones, usuarios y métricas. */
object AdminRepository {
    private val api = RetrofitClient.api

    suspend fun dashboard(): AdminDashboardNet? = try {
        api.adminDashboard()
    } catch (_: Exception) {
        null
    }

    suspend fun users(): List<UserAdminNet> = try {
        api.adminUsers()
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun changeRole(userId: Long, newRole: String): Result<Unit> = try {
        val r = api.adminChangeRole(userId, ChangeRoleRequest(newRole))
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(IllegalStateException(r.errorBody()?.string().orEmpty().ifBlank { "HTTP ${r.code()}" }))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun enrolledStudents(courseId: Long): List<EnrolledStudentNet> = try {
        api.adminEnrolledStudents(courseId)
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun createCourse(req: CreateCourseRequest): Result<CourseNet> = try {
        Result.success(api.adminCreateCourse(req))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCourse(id: Long, req: CreateCourseRequest): Result<CourseNet> = try {
        Result.success(api.adminUpdateCourse(id, req))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteCourse(id: Long): Result<Unit> = try {
        api.adminDeleteCourse(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createLesson(courseId: Long, req: CreateLessonRequest): Result<LessonNet> = try {
        Result.success(api.adminCreateLesson(courseId, req))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateLesson(lessonId: Long, req: CreateLessonRequest): Result<LessonNet> = try {
        Result.success(api.adminUpdateLesson(lessonId, req))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteLesson(lessonId: Long): Result<Unit> = try {
        api.adminDeleteLesson(lessonId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
