package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * ApiService completo — paridad 1:1 con el backend Spring Boot.
 * HU-01 a HU-14, HU-37 a HU-43.
 */
interface ApiService {

    // ── Catalogo (HU-04) ──
    @GET("api/courses")
    suspend fun getCourses(): List<CourseNet>

    @GET("api/courses/{id}")
    suspend fun getCourse(@Path("id") id: Long): CourseNet

    @GET("api/courses/search")
    suspend fun searchCourses(@Query("q") query: String): List<CourseNet>

    @GET("api/courses/technology/{technology}")
    suspend fun coursesByTechnology(@Path("technology") technology: String): List<CourseNet>

    @GET("api/courses/level/{level}")
    suspend fun coursesByLevel(@Path("level") level: String): List<CourseNet>

    // ── Autenticacion (HU-01, HU-03) ──
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ResponseBody>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponseNet>

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body body: VerifyCodeRequest): Response<ResponseBody>

    @POST("api/auth/resend-code")
    suspend fun resendCode(@Body body: ResendCodeRequest): Response<ResponseBody>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ResponseBody>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ResponseBody>

    // ── Compras (HU-06) ──
    @POST("api/purchases")
    suspend fun purchase(@Body body: PurchaseRequest): PurchaseResponseNet

    @GET("api/purchases/user/{email}")
    suspend fun myPurchases(@Path("email") email: String): List<PurchaseNet>

    // ── Lecciones (HU-07, HU-08) ──
    @GET("api/courses/{courseId}/lessons")
    suspend fun lessonsByCourse(@Path("courseId") courseId: Long): List<LessonNet>

    @POST("api/lessons/{lessonId}/progress")
    suspend fun saveLessonProgress(
        @Path("lessonId") lessonId: Long,
        @Body body: LessonProgressUpdateRequest
    ): LessonProgressNet

    @GET("api/courses/{courseId}/progress")
    suspend fun courseProgress(
        @Path("courseId") courseId: Long,
        @Query("email") email: String
    ): CourseProgressNet

    // ── Quiz (HU-10) ──
    @GET("api/courses/{courseId}/quiz")
    suspend fun getQuiz(@Path("courseId") courseId: Long): QuizInfoNet

    @POST("api/courses/{courseId}/quiz/attempts")
    suspend fun submitQuiz(
        @Path("courseId") courseId: Long,
        @Body body: SubmitQuizRequest
    ): QuizResultNet

    // ── Certificados (HU-12) ──
    @GET("api/certificates/mine")
    suspend fun myCertificates(): List<CertificateNet>

    @GET("api/certificates/verify/{code}")
    suspend fun verifyCertificate(@Path("code") code: String): CertificateVerificationNet

    // ── Perfil (HU-14) ──
    @GET("api/users/me")
    suspend fun getProfile(): UserProfileNet

    @PUT("api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserProfileNet

    @PUT("api/users/me/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Map<String, String>

    @PUT("api/users/me/avatar")
    suspend fun updateAvatar(@Body body: Map<String, String>): UserProfileNet

    // ── Tareas · estudiante (HU-37, HU-38) ──
    @GET("api/courses/{courseId}/assignments")
    suspend fun assignmentsByCourse(@Path("courseId") courseId: Long): List<AssignmentNet>

    @GET("api/assignments/{id}")
    suspend fun assignment(@Path("id") id: Long): AssignmentNet

    @GET("api/courses/{courseId}/assignments/mine")
    suspend fun myCourseAssignments(@Path("courseId") courseId: Long): List<StudentAssignmentNet>

    @GET("api/assignments/{id}/mine")
    suspend fun myAssignment(@Path("id") id: Long): StudentAssignmentNet

    @POST("api/assignments/{id}/submission")
    suspend fun submitAssignment(
        @Path("id") id: Long,
        @Body body: SubmitAssignmentRequest
    ): SubmissionNet

    // ── Admin / Instructor (HU-09, HU-13, HU-39, HU-41) ──
    @GET("api/admin/dashboard")
    suspend fun adminDashboard(): AdminDashboardNet

    @GET("api/admin/users")
    suspend fun adminUsers(): List<UserAdminNet>

    @PUT("api/admin/users/{id}/role")
    suspend fun adminChangeRole(@Path("id") id: Long, @Body body: ChangeRoleRequest): Response<ResponseBody>

    @GET("api/admin/courses/{courseId}/students")
    suspend fun adminEnrolledStudents(@Path("courseId") courseId: Long): List<EnrolledStudentNet>

    @POST("api/admin/courses")
    suspend fun adminCreateCourse(@Body body: CreateCourseRequest): CourseNet

    @PUT("api/admin/courses/{id}")
    suspend fun adminUpdateCourse(@Path("id") id: Long, @Body body: CreateCourseRequest): CourseNet

    @DELETE("api/admin/courses/{id}")
    suspend fun adminDeleteCourse(@Path("id") id: Long): Response<ResponseBody>

    @POST("api/admin/courses/{courseId}/lessons")
    suspend fun adminCreateLesson(
        @Path("courseId") courseId: Long,
        @Body body: CreateLessonRequest
    ): LessonNet

    @PUT("api/admin/lessons/{lessonId}")
    suspend fun adminUpdateLesson(
        @Path("lessonId") lessonId: Long,
        @Body body: CreateLessonRequest
    ): LessonNet

    @DELETE("api/admin/lessons/{lessonId}")
    suspend fun adminDeleteLesson(@Path("lessonId") lessonId: Long): Response<ResponseBody>

    @GET("api/admin/courses/{courseId}/assignments")
    suspend fun adminAssignments(@Path("courseId") courseId: Long): List<AssignmentAdminNet>

    @POST("api/admin/courses/{courseId}/assignments")
    suspend fun adminCreateAssignment(
        @Path("courseId") courseId: Long,
        @Body body: CreateAssignmentRequest
    ): AssignmentAdminNet

    @PUT("api/admin/assignments/{id}")
    suspend fun adminUpdateAssignment(
        @Path("id") id: Long,
        @Body body: CreateAssignmentRequest
    ): AssignmentAdminNet

    @DELETE("api/admin/assignments/{id}")
    suspend fun adminDeleteAssignment(@Path("id") id: Long): Response<ResponseBody>
}
