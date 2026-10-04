package service

import com.dieletech.backend.dto.*
import com.dieletech.backend.repository.*
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Calcula todas las metricas del panel a partir de las tablas reales
 * (purchases, lesson_progress, courses, users). No hay cifras sembradas
 * ni estimaciones: si no hay compras, los KPIs son cero.
 */
@Service
class AdminMetricsService(
    private val courseRepository: CourseRepository,
    private val purchaseRepository: PurchaseRepository,
    private val lessonRepository: LessonRepository,
    private val lessonProgressRepository: LessonProgressRepository,
    private val userRepository: UserRepository
) {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val dayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * @param instructorEmail si viene, limita el dashboard a los cursos de
     *        ese instructor (vista INSTRUCTOR). Si es null, es la vista ADMIN.
     */
    fun buildDashboard(instructorEmail: String? = null): AdminDashboardDTO {
        val allCourses = courseRepository.findAll()
        val courses = if (instructorEmail == null) allCourses
        else allCourses.filter { it.instructorEmail.equals(instructorEmail, ignoreCase = true) }

        val courseIds = courses.map { it.id }.toSet()
        val purchases = purchaseRepository.findAll().filter { it.courseId in courseIds }

        val lessonCountByCourse = courses.associate { it.id to lessonRepository.countByCourseIdAndActiveTrue(it.id) }

        // ── Metricas por curso ──
        val courseMetrics = courses.map { c ->
            val coursePurchases = purchases.filter { it.courseId == c.id }
            val enrolled = coursePurchases.size
            val lessons = lessonCountByCourse[c.id] ?: 0

            val progresses = coursePurchases.map { p ->
                val done = lessonProgressRepository.countCompletedByEmailAndCourse(p.userEmail, c.id)
                if (lessons > 0) (done * 100) / lessons else 0
            }

            CourseMetricsDTO(
                courseId = c.id,
                title = c.title,
                technology = c.technology,
                level = c.level,
                price = c.price,
                active = c.active,
                capacity = c.capacity,
                enrolled = enrolled,
                seatsAvailable = (c.capacity - enrolled).coerceAtLeast(0),
                occupancyPercent = if (c.capacity > 0) (enrolled * 100) / c.capacity else 0,
                revenue = coursePurchases.sumOf { it.amount },
                lessonCount = lessons,
                averageProgressPercent = if (progresses.isNotEmpty()) progresses.average().toInt() else 0,
                studentsCompleted = progresses.count { it >= 100 }
            )
        }.sortedByDescending { it.enrolled }

        // ── KPIs globales ──
        val cutoff30 = LocalDateTime.now().minusDays(30)
        val recent = purchases.filter { it.purchasedAt.isAfter(cutoff30) }
        val totalCapacity = courses.sumOf { it.capacity }
        val totalEnrollments = purchases.size
        val allProgress = courseMetrics.filter { it.enrolled > 0 }

        val overview = AdminOverviewDTO(
            totalCourses = courses.size,
            activeCourses = courses.count { it.active },
            totalEnrollments = totalEnrollments,
            uniqueStudents = purchases.map { it.userEmail.lowercase() }.distinct().size,
            totalRevenue = purchases.sumOf { it.amount },
            totalLessons = lessonCountByCourse.values.sum(),
            totalCapacity = totalCapacity,
            seatsAvailable = (totalCapacity - totalEnrollments).coerceAtLeast(0),
            occupancyPercent = if (totalCapacity > 0) (totalEnrollments * 100) / totalCapacity else 0,
            averageProgressPercent = if (allProgress.isNotEmpty())
                allProgress.map { it.averageProgressPercent }.average().toInt() else 0,
            completionRatePercent = if (totalEnrollments > 0)
                (courseMetrics.sumOf { it.studentsCompleted } * 100) / totalEnrollments else 0,
            enrollmentsLast30Days = recent.size,
            revenueLast30Days = recent.sumOf { it.amount }
        )

        // ── Tendencia de los ultimos 30 dias (incluye dias en cero) ──
        val byDay = purchases.groupBy { it.purchasedAt.toLocalDate() }
        val trend = (0L..29L).map { back ->
            val day = LocalDate.now().minusDays(29 - back)
            val dayPurchases = byDay[day] ?: emptyList()
            EnrollmentPointDTO(
                date = day.format(dayFmt),
                enrollments = dayPurchases.size,
                revenue = dayPurchases.sumOf { it.amount }
            )
        }

        // ── Distribucion por tecnologia ──
        val breakdown = courses.groupBy { it.technology }.map { (tech, list) ->
            val ids = list.map { it.id }.toSet()
            val techPurchases = purchases.filter { it.courseId in ids }
            TechnologyBreakdownDTO(
                technology = tech,
                courses = list.size,
                enrollments = techPurchases.size,
                revenue = techPurchases.sumOf { it.amount }
            )
        }.sortedByDescending { it.revenue }

        // ── Actividad reciente ──
        val titleById = courses.associate { it.id to it.title }
        val recentEnrollments = purchases
            .sortedByDescending { it.purchasedAt }
            .take(15)
            .map {
                RecentEnrollmentDTO(
                    studentName = it.fullName,
                    studentEmail = it.userEmail,
                    courseTitle = titleById[it.courseId] ?: "Curso eliminado",
                    amount = it.amount,
                    purchasedAt = it.purchasedAt.format(dateFmt),
                    orderId = it.orderId
                )
            }

        return AdminDashboardDTO(
            overview = overview,
            courses = courseMetrics,
            enrollmentTrend = trend,
            technologyBreakdown = breakdown,
            recentEnrollments = recentEnrollments
        )
    }

    /** Gestion de usuarios: solo para ADMIN. */
    fun listUsers(): List<UserAdminDTO> {
        val purchases = purchaseRepository.findAll()
        return userRepository.findAll().map { u ->
            val mine = purchases.filter { it.userEmail.equals(u.email, ignoreCase = true) }
            UserAdminDTO(
                id = u.id,
                name = u.name,
                email = u.email,
                role = u.role.name,
                verified = u.verified,
                createdAt = u.createdAt.format(dateFmt),
                coursesEnrolled = mine.size,
                totalSpent = mine.sumOf { it.amount }
            )
        }.sortedByDescending { it.createdAt }
    }
}
