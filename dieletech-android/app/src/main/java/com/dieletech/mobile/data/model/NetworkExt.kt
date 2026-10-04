package com.dieletech.mobile.data.model

/**
 * HU-37 / HU-38 · Tareas y entregas.
 * El enunciado llega vacío si no hay compra y locked=true, igual que en el
 * backend; el UI se encarga de mostrar el bloqueo.
 */
data class AssignmentNet(
    val id: Long = 0,
    val courseId: Long = 0,
    val lessonId: Long? = null,
    val lessonTitle: String? = null,
    val title: String = "",
    val statement: String? = null,
    val type: String = "TEXT",
    val maxScore: Double = 100.0,
    val dueOffsetDays: Int = 14,
    val orderIndex: Int = 0,
    val locked: Boolean = false
)

data class AssignmentAdminNet(
    val id: Long = 0,
    val courseId: Long = 0,
    val courseTitle: String = "",
    val lessonId: Long? = null,
    val lessonTitle: String? = null,
    val title: String = "",
    val statement: String = "",
    val type: String = "TEXT",
    val maxScore: Double = 100.0,
    val dueOffsetDays: Int = 14,
    val orderIndex: Int = 0,
    val active: Boolean = true
)

data class CreateAssignmentRequest(
    val title: String,
    val statement: String,
    val type: String = "TEXT",
    val lessonId: Long? = null,
    val maxScore: Double = 100.0,
    val dueOffsetDays: Int = 14,
    val orderIndex: Int = 0
)

data class SubmissionNet(
    val id: Long = 0,
    val assignmentId: Long = 0,
    val courseId: Long = 0,
    val content: String = "",
    val fileUrl: String? = null,
    val status: String = "DRAFT",
    val submittedAt: String? = null,
    val dueDate: String? = null,
    val late: Boolean = false,
    val score: Double? = null,
    val feedback: String? = null,
    val gradedAt: String? = null,
    val gradedBy: String? = null
)

data class StudentAssignmentNet(
    val assignment: AssignmentNet = AssignmentNet(),
    val submission: SubmissionNet? = null,
    val canSubmit: Boolean = false,
    val blockedReason: String? = null,
    val dueDate: String? = null,
    val daysLeft: Long? = null
)

data class SubmitAssignmentRequest(
    val content: String,
    val fileUrl: String? = null,
    val draft: Boolean = true
)

data class GradeSubmissionRequest(
    val score: Double,
    val feedback: String? = null
)

// ─── HU-10 · Evaluación / Quiz ────────────────────────────────────────────

data class QuestionNet(
    val id: Long = 0,
    val text: String = "",
    val options: List<String> = emptyList(),
    val orderIndex: Int = 0
)

data class QuizInfoNet(
    val courseId: Long = 0,
    val courseTitle: String = "",
    val totalQuestions: Int = 0,
    val passingScore: Int = 70,
    val available: Boolean = false,
    val blockedReason: String? = null,
    val lessonsCompleted: Int = 0,
    val totalLessons: Int = 0,
    val attempts: Int = 0,
    val bestScore: Int = 0,
    val passed: Boolean = false,
    val certificateCode: String? = null,
    val questions: List<QuestionNet> = emptyList()
)

data class SubmitAnswerRequest(val questionId: Long, val selectedIndex: Int)
data class SubmitQuizRequest(val answers: List<SubmitAnswerRequest>)

data class QuestionResultNet(
    val questionId: Long = 0,
    val text: String = "",
    val options: List<String> = emptyList(),
    val selectedIndex: Int = -1,
    val correctIndex: Int = -1,
    val correct: Boolean = false,
    val explanation: String? = null
)

data class QuizResultNet(
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val passingScore: Int = 70,
    val passed: Boolean = false,
    val message: String = "",
    val certificateCode: String? = null,
    val results: List<QuestionResultNet> = emptyList()
)

// ─── HU-12 · Certificados ─────────────────────────────────────────────────

data class CertificateNet(
    val code: String = "",
    val courseId: Long = 0,
    val courseTitle: String = "",
    val courseHours: Int = 0,
    val studentName: String = "",
    val instructorName: String? = null,
    val score: Int = 0,
    val issuedAt: String = "",
    val verifyUrl: String = ""
)

data class CertificateVerificationNet(
    val valid: Boolean = false,
    val message: String = "",
    val code: String = "",
    val studentName: String? = null,
    val courseTitle: String? = null,
    val courseHours: Int? = null,
    val instructorName: String? = null,
    val score: Int? = null,
    val issuedAt: String? = null
)

// ─── HU-14 · Perfil ───────────────────────────────────────────────────────

data class UserProfileNet(
    val id: Long = 0,
    val name: String = "",
    val displayName: String? = null,
    val email: String = "",
    val role: String = "STUDENT",
    val verified: Boolean = false,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val jobTitle: String? = null,
    val phone: String? = null,
    val country: String? = null,
    val city: String? = null,
    val interests: List<String> = emptyList(),
    val linkedinUrl: String? = null,
    val githubUrl: String? = null,
    val websiteUrl: String? = null,
    val themePreference: String = "system",
    val accentColor: String = "#2563eb",
    val languagePreference: String = "es",
    val notifyEmail: Boolean = true,
    val notifyNewCourses: Boolean = true,
    val notifyProgress: Boolean = true,
    val publicProfile: Boolean = false,
    val memberSince: String = "",
    val coursesEnrolled: Int = 0,
    val coursesCompleted: Int = 0,
    val lessonsCompleted: Int = 0,
    val totalInvested: Double = 0.0,
    val averageProgress: Int = 0
)

data class UpdateProfileRequest(
    val name: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val jobTitle: String? = null,
    val phone: String? = null,
    val country: String? = null,
    val city: String? = null,
    val interests: List<String>? = null,
    val linkedinUrl: String? = null,
    val githubUrl: String? = null,
    val websiteUrl: String? = null,
    val avatarUrl: String? = null,
    val themePreference: String? = null,
    val accentColor: String? = null,
    val languagePreference: String? = null,
    val notifyEmail: Boolean? = null,
    val notifyNewCourses: Boolean? = null,
    val notifyProgress: Boolean? = null,
    val publicProfile: Boolean? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

// ─── HU-09 · Admin: cursos y lecciones ────────────────────────────────────

data class CreateCourseRequest(
    val title: String,
    val description: String,
    val longDescription: String? = null,
    val technology: String,
    val level: String,
    val price: Double,
    val duration: Int,
    val capacity: Int = 50,
    val imageUrl: String? = null,
    val curriculum: String? = null,
    val prerequisites: String? = null,
    val learningObjectives: String? = null,
    val targetAudience: String? = null,
    val previewVideoUrl: String? = null,
    val instructorName: String? = null
)

data class CreateLessonRequest(
    val title: String,
    val description: String? = null,
    val videoUrl: String? = null,
    val orderIndex: Int,
    val durationMinutes: Int = 0,
    val contentType: String = "VIDEO",
    val materialUrl: String? = null,
    val freePreview: Boolean = false
)

// ─── HU-13 · Admin dashboard ──────────────────────────────────────────────

data class AdminOverviewNet(
    val totalCourses: Int = 0,
    val activeCourses: Int = 0,
    val totalEnrollments: Int = 0,
    val uniqueStudents: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalLessons: Int = 0,
    val totalCapacity: Int = 0,
    val seatsAvailable: Int = 0,
    val occupancyPercent: Int = 0,
    val averageProgressPercent: Int = 0,
    val completionRatePercent: Int = 0,
    val enrollmentsLast30Days: Int = 0,
    val revenueLast30Days: Double = 0.0
)

data class CourseMetricsNet(
    val courseId: Long = 0,
    val title: String = "",
    val technology: String = "",
    val level: String = "",
    val price: Double = 0.0,
    val active: Boolean = true,
    val capacity: Int = 0,
    val enrolled: Int = 0,
    val seatsAvailable: Int = 0,
    val occupancyPercent: Int = 0,
    val revenue: Double = 0.0,
    val lessonCount: Int = 0,
    val averageProgressPercent: Int = 0,
    val studentsCompleted: Int = 0
)

data class EnrollmentPointNet(val date: String = "", val enrollments: Int = 0, val revenue: Double = 0.0)
data class TechnologyBreakdownNet(val technology: String = "", val courses: Int = 0, val enrollments: Int = 0, val revenue: Double = 0.0)
data class RecentEnrollmentNet(
    val studentName: String = "",
    val studentEmail: String = "",
    val courseTitle: String = "",
    val amount: Double = 0.0,
    val purchasedAt: String = "",
    val orderId: String = ""
)

data class AdminDashboardNet(
    val overview: AdminOverviewNet = AdminOverviewNet(),
    val courses: List<CourseMetricsNet> = emptyList(),
    val enrollmentTrend: List<EnrollmentPointNet> = emptyList(),
    val technologyBreakdown: List<TechnologyBreakdownNet> = emptyList(),
    val recentEnrollments: List<RecentEnrollmentNet> = emptyList()
)

data class UserAdminNet(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val role: String = "STUDENT",
    val verified: Boolean = false,
    val createdAt: String = "",
    val coursesEnrolled: Int = 0,
    val totalSpent: Double = 0.0
)

data class ChangeRoleRequest(val role: String)

data class EnrolledStudentNet(
    val email: String = "",
    val name: String = "",
    val progressPercent: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val purchasedAt: String = ""
)

// ─── HU-07 / HU-08 · Lecciones (backend real) ─────────────────────────────

data class LessonNet(
    val id: Long = 0,
    val courseId: Long = 0,
    val title: String = "",
    val description: String? = null,
    val videoUrl: String? = null,
    val orderIndex: Int = 0,
    val durationMinutes: Int = 0,
    val contentType: String = "VIDEO",
    val materialUrl: String? = null,
    val freePreview: Boolean = false,
    val locked: Boolean = false
)

fun LessonNet.toLesson(): Lesson = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    description = description.orEmpty(),
    videoUrl = videoUrl.orEmpty(),
    durationSec = durationMinutes * 60,
    order = orderIndex
)

data class LessonProgressNet(
    val lessonId: Long = 0,
    val completed: Boolean = false,
    val lastPositionSeconds: Int = 0,
    val completedAt: String? = null
)

data class CourseProgressNet(
    val courseId: Long = 0,
    val totalLessons: Int = 0,
    val completedLessons: Int = 0,
    val progressPercent: Int = 0,
    val lessonProgress: List<LessonProgressNet> = emptyList()
)

data class LessonProgressUpdateRequest(
    val email: String,
    val completed: Boolean? = null,
    val lastPositionSeconds: Int? = null
)
