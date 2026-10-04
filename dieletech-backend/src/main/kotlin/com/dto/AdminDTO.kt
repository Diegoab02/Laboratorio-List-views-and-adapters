package com.dieletech.backend.dto

/** KPIs globales del panel. Todos derivados de datos reales. */
data class AdminOverviewDTO(
    val totalCourses: Int,
    val activeCourses: Int,
    val totalEnrollments: Int,
    val uniqueStudents: Int,
    val totalRevenue: Double,
    val totalLessons: Int,
    val totalCapacity: Int,
    val seatsAvailable: Int,
    val occupancyPercent: Int,
    val averageProgressPercent: Int,
    val completionRatePercent: Int,
    val enrollmentsLast30Days: Int,
    val revenueLast30Days: Double
)

/** Metrica por curso para la tabla del panel. */
data class CourseMetricsDTO(
    val courseId: Long,
    val title: String,
    val technology: String,
    val level: String,
    val price: Double,
    val active: Boolean,
    val capacity: Int,
    val enrolled: Int,
    val seatsAvailable: Int,
    val occupancyPercent: Int,
    val revenue: Double,
    val lessonCount: Int,
    val averageProgressPercent: Int,
    val studentsCompleted: Int
)

/** Serie temporal de matriculas para la grafica de tendencia. */
data class EnrollmentPointDTO(
    val date: String,
    val enrollments: Int,
    val revenue: Double
)

/** Distribucion de ingresos y matriculas por tecnologia. */
data class TechnologyBreakdownDTO(
    val technology: String,
    val courses: Int,
    val enrollments: Int,
    val revenue: Double
)

/** Matricula reciente para el feed de actividad. */
data class RecentEnrollmentDTO(
    val studentName: String,
    val studentEmail: String,
    val courseTitle: String,
    val amount: Double,
    val purchasedAt: String,
    val orderId: String
)

/** Respuesta completa del dashboard. */
data class AdminDashboardDTO(
    val overview: AdminOverviewDTO,
    val courses: List<CourseMetricsDTO>,
    val enrollmentTrend: List<EnrollmentPointDTO>,
    val technologyBreakdown: List<TechnologyBreakdownDTO>,
    val recentEnrollments: List<RecentEnrollmentDTO>
)

/** Fila de la gestion de usuarios (solo ADMIN). */
data class UserAdminDTO(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val verified: Boolean,
    val createdAt: String,
    val coursesEnrolled: Int,
    val totalSpent: Double
)
