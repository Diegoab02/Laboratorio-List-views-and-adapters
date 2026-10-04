package service

import com.dieletech.backend.dto.CertificateDTO
import com.dieletech.backend.dto.CertificateVerificationDTO
import com.dieletech.backend.model.Certificate
import com.dieletech.backend.model.Course
import com.dieletech.backend.repository.CertificateRepository
import com.dieletech.backend.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * HU-11: emision y verificacion de certificados.
 * El PDF lo dibuja CertificatePdfGenerator; aqui vive la logica de
 * negocio: codigo unico, emision idempotente y verificacion publica.
 */
@Service
class CertificateService(
    private val certificateRepository: CertificateRepository,
    private val userRepository: UserRepository,
    private val pdfGenerator: CertificatePdfGenerator,
    @Value("\${app.url}") private val appUrl: String
) {

    private val random = SecureRandom()
    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // sin I, O, 0, 1
    private val dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "CO"))
    private val isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun findForCourse(email: String, courseId: Long): Certificate? =
        certificateRepository.findForCourse(email.trim().lowercase(), courseId).orElse(null)

    /** Emite el certificado. Si ya existe uno para ese curso, lo devuelve. */
    @Transactional
    fun issue(email: String, course: Course, score: Int): Certificate {
        val normalized = email.trim().lowercase()
        findForCourse(normalized, course.id)?.let { return it }

        val user = userRepository.findByEmail(normalized).orElseThrow {
            RuntimeException("Usuario no encontrado")
        }

        return certificateRepository.save(
            Certificate(
                code = generateCode(),
                userEmail = normalized,
                courseId = course.id,
                studentName = (user.displayName?.takeIf { it.isNotBlank() } ?: user.name).trim(),
                courseTitle = course.title,
                courseHours = course.duration,
                instructorName = course.instructorName,
                score = score
            )
        )
    }

    fun listMine(email: String): List<CertificateDTO> =
        certificateRepository.findMine(email.trim().lowercase()).map { it.toDTO() }

    fun getOwned(code: String, email: String): Certificate {
        val cert = certificateRepository.findByCode(code.trim().uppercase()).orElseThrow {
            RuntimeException("Certificado no encontrado")
        }
        if (!cert.userEmail.equals(email.trim().lowercase(), ignoreCase = true)) {
            throw RuntimeException("Este certificado no te pertenece")
        }
        return cert
    }

    /** Verificacion publica: confirma el certificado sin exponer el correo. */
    fun verify(code: String): CertificateVerificationDTO {
        val clean = code.trim().uppercase()
        val cert = certificateRepository.findByCode(clean).orElse(null)
            ?: return CertificateVerificationDTO(
                valid = false,
                message = "No existe ningun certificado con este codigo.",
                code = clean
            )

        if (cert.revoked) {
            return CertificateVerificationDTO(
                valid = false,
                message = "Este certificado fue revocado y ya no es valido.",
                code = clean
            )
        }

        return CertificateVerificationDTO(
            valid = true,
            message = "Certificado valido emitido por Dieletech.",
            code = cert.code,
            studentName = cert.studentName,
            courseTitle = cert.courseTitle,
            courseHours = cert.courseHours,
            instructorName = cert.instructorName,
            score = cert.score,
            issuedAt = cert.issuedAt.format(dateFmt)
        )
    }

    fun renderPdf(cert: Certificate): ByteArray =
        pdfGenerator.render(cert, verifyUrl(cert.code), cert.issuedAt.format(dateFmt))

    fun verifyUrl(code: String): String = "${appUrl.trimEnd('/')}/verificar/$code"

    /** DTC-XXXX-XXXX-XXXX con alfabeto sin caracteres ambiguos. */
    private fun generateCode(): String {
        repeat(12) {
            val body = (1..3).joinToString("-") { _ ->
                (1..4).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")
            }
            val code = "DTC-$body"
            if (!certificateRepository.existsByCode(code)) return code
        }
        throw RuntimeException("No se pudo generar un codigo unico. Intenta de nuevo.")
    }

    private fun Certificate.toDTO() = CertificateDTO(
        code = code,
        courseId = courseId,
        courseTitle = courseTitle,
        courseHours = courseHours,
        studentName = studentName,
        instructorName = instructorName,
        score = score,
        issuedAt = issuedAt.format(isoFmt),
        verifyUrl = verifyUrl(code)
    )
}
