package service

import com.dieletech.backend.dto.EnrolledStudentDTO
import com.dieletech.backend.dto.PurchaseRequestDTO
import com.dieletech.backend.dto.PurchaseResponseDTO
import com.dieletech.backend.model.Purchase
import com.dieletech.backend.repository.CourseRepository
import com.dieletech.backend.repository.LessonProgressRepository
import com.dieletech.backend.repository.LessonRepository
import com.dieletech.backend.repository.PurchaseRepository
import com.dieletech.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.abs

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository,
    private val lessonProgressRepository: LessonProgressRepository
) {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * Reglas de negocio de la compra. Todas se validan antes de persistir:
     *  1. El curso existe y esta activo.
     *  2. El comprador tiene una cuenta registrada (no se vende a desconocidos).
     *  3. No hay una compra previa del mismo curso por el mismo correo.
     *  4. Quedan cupos disponibles.
     *  5. El monto enviado coincide con el precio real del curso.
     * Es transaccional: si algo falla no queda una compra a medias ni un
     * contador de matriculados inflado.
     */
    @Transactional
    fun processPurchase(request: PurchaseRequestDTO): PurchaseResponseDTO {
        val course = courseRepository.findById(request.courseId).orElseThrow {
            RuntimeException("El curso solicitado no existe")
        }

        if (!course.active) {
            throw RuntimeException("Este curso no esta disponible actualmente")
        }

        val normalizedEmail = request.email.trim().lowercase()
        val normalizedName = request.fullName.trim()

        userRepository.findByEmail(normalizedEmail).orElseThrow {
            RuntimeException("Debes tener una cuenta registrada para comprar. Inicia sesion e intenta de nuevo.")
        }

        if (purchaseRepository.existsPurchaseNormalized(course.id, normalizedEmail)) {
            throw RuntimeException("Ya tienes este curso. Encuentralo en 'Mis Cursos'.")
        }

        if (course.seatsAvailable <= 0) {
            throw RuntimeException("No quedan cupos disponibles para este curso.")
        }

        // Tolerancia por redondeo de punto flotante al viajar como JSON.
        if (abs(request.amount - course.price) > 0.01) {
            throw RuntimeException("El monto no coincide con el precio del curso. Recarga la pagina.")
        }

        val orderId = "ORD-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"

        val purchase = Purchase(
            courseId = course.id,
            userEmail = normalizedEmail,
            fullName = normalizedName,
            documentId = request.documentId.trim(),
            phone = request.phone.trim(),
            amount = course.price,
            paymentMethod = request.paymentMethod,
            orderId = orderId,
            status = "COMPLETED"
        )
        purchaseRepository.save(purchase)

        // Contador real, derivado de compras efectivas.
        course.studentCount = purchaseRepository.countByCourseId(course.id).toInt()
        courseRepository.save(course)

        return PurchaseResponseDTO(
            success = true,
            orderId = orderId,
            courseId = course.id,
            courseName = course.title,
            amount = course.price,
            message = "Compra realizada exitosamente"
        )
    }

    fun getPurchasesByEmail(email: String): List<Purchase> =
        purchaseRepository.findByUserEmailNormalized(email)

    fun hasUserPurchasedCourse(courseId: Long, email: String): Boolean =
        purchaseRepository.existsPurchaseNormalized(courseId, email)

    /** Lista de matriculados de un curso con su progreso real (panel instructor). */
    fun getEnrolledStudents(courseId: Long): List<EnrolledStudentDTO> {
        val totalLessons = lessonRepository.countByCourseIdAndActiveTrue(courseId)
        return purchaseRepository.findByCourseId(courseId).map { p ->
            val completed = lessonProgressRepository
                .countCompletedByEmailAndCourse(p.userEmail, courseId)
            EnrolledStudentDTO(
                fullName = p.fullName,
                email = p.userEmail,
                orderId = p.orderId,
                purchasedAt = p.purchasedAt.format(dateFmt),
                amount = p.amount,
                progressPercent = if (totalLessons > 0) (completed * 100) / totalLessons else 0,
                completedLessons = completed,
                totalLessons = totalLessons
            )
        }.sortedByDescending { it.purchasedAt }
    }

    /**
     * Recalcula studentCount de todos los cursos desde la tabla de compras.
     * Elimina cualquier cifra sembrada o desincronizada.
     */
    @Transactional
    fun recalculateAllStudentCounts(): Int {
        val courses = courseRepository.findAll()
        var fixed = 0
        for (c in courses) {
            val real = purchaseRepository.countByCourseId(c.id).toInt()
            if (c.studentCount != real) {
                c.studentCount = real
                fixed++
            }
        }
        if (fixed > 0) courseRepository.saveAll(courses)
        return fixed
    }
}
