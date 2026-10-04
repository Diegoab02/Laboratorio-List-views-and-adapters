package com.dieletech.backend.service

import com.dieletech.backend.dto.SubmitAssignmentDTO
import com.dieletech.backend.error.ConflictException
import com.dieletech.backend.error.ForbiddenException
import com.dieletech.backend.error.NotFoundException
import com.dieletech.backend.model.Assignment
import com.dieletech.backend.model.AssignmentType
import com.dieletech.backend.model.Lesson
import com.dieletech.backend.model.Purchase
import com.dieletech.backend.model.Submission
import com.dieletech.backend.model.SubmissionStatus
import com.dieletech.backend.repository.AssignmentRepository
import com.dieletech.backend.repository.LessonProgressRepository
import com.dieletech.backend.repository.LessonRepository
import com.dieletech.backend.repository.PurchaseRepository
import com.dieletech.backend.repository.SubmissionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import service.SubmissionService
import java.time.LocalDateTime
import java.util.*

/**
 * HU-38. Una prueba por cada una de las cinco reglas, mas los casos que
 * en el Sprint 8 se dieron por buenos sin comprobar: el borrador que
 * duplicaba fila y la entrega calificada que se podia reescribir.
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SubmissionService - HU-38")
class SubmissionServiceTest {

    @Mock private lateinit var submissionRepository: SubmissionRepository
    @Mock private lateinit var assignmentRepository: AssignmentRepository
    @Mock private lateinit var purchaseRepository: PurchaseRepository
    @Mock private lateinit var lessonRepository: LessonRepository
    @Mock private lateinit var progressRepository: LessonProgressRepository

    private lateinit var service: SubmissionService

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObj(): T = any<T>() as T

    private val email = "diego@test.com"
    private val boughtAt: LocalDateTime = LocalDateTime.now().minusDays(2)

    @BeforeEach
    fun setUp() {
        service = SubmissionService(
            submissionRepository, assignmentRepository,
            purchaseRepository, lessonRepository, progressRepository
        )
    }

    // ── Datos de apoyo ──

    private fun assignment(
        id: Long = 100L,
        lessonId: Long? = 10L,
        type: AssignmentType = AssignmentType.CODE,
        days: Int = 5
    ) = Assignment(
        id = id,
        courseId = 1L,
        lessonId = lessonId,
        title = "Conversor de unidades",
        statement = "Convierte Celsius a Fahrenheit y Kelvin con validacion de entrada.",
        type = type,
        dueOffsetDays = days
    )

    private fun purchase(at: LocalDateTime = boughtAt) = Purchase(
        id = 1L,
        courseId = 1L,
        userEmail = email,
        fullName = "Diego Betancur",
        amount = 79900.0,
        paymentMethod = "card",
        orderId = "ORD-1",
        purchasedAt = at
    )

    /** Escenario base: compro el curso y completo la leccion del modulo. */
    private fun happyPath(a: Assignment = assignment(), at: LocalDateTime = boughtAt) {
        `when`(assignmentRepository.findByIdAndActiveTrue(a.id)).thenReturn(a)
        `when`(purchaseRepository.existsPurchaseNormalized(1L, email)).thenReturn(true)
        `when`(purchaseRepository.findByUserEmailNormalized(email)).thenReturn(listOf(purchase(at)))
        a.lessonId?.let { `when`(progressRepository.isLessonCompleted(email, it)).thenReturn(true) }
        `when`(submissionRepository.save(anyObj<Submission>())).thenAnswer { it.arguments[0] }
    }

    private fun dto(content: String = "print('hola')", file: String? = null, draft: Boolean = false) =
        SubmitAssignmentDTO(content = content, fileUrl = file, draft = draft)

    // =====================================================
    // Regla 1: hay que haber comprado
    // =====================================================
    @Nested
    @DisplayName("Regla 1 - entregar exige compra")
    inner class PurchaseGate {

        @Test
        @DisplayName("sin compra la entrega se rechaza con 403")
        fun sinCompra() {
            `when`(assignmentRepository.findByIdAndActiveTrue(100L)).thenReturn(assignment())
            `when`(purchaseRepository.existsPurchaseNormalized(1L, email)).thenReturn(false)

            val e = assertThrows(ForbiddenException::class.java) {
                service.save(100L, email, dto())
            }
            assertTrue(e.message!!.contains("comprar"))
            verify(submissionRepository, never()).save(anyObj<Submission>())
        }

        @Test
        @DisplayName("una tarea inexistente responde 404")
        fun tareaInexistente() {
            `when`(assignmentRepository.findByIdAndActiveTrue(404L)).thenReturn(null)
            assertThrows(NotFoundException::class.java) { service.save(404L, email, dto()) }
        }
    }

    // =====================================================
    // Regla 2: el modulo tiene que estar completo
    // =====================================================
    @Nested
    @DisplayName("Regla 2 - el modulo tiene que estar completo")
    inner class ModuleGate {

        @Test
        @DisplayName("con la leccion sin terminar no se puede entregar")
        fun leccionIncompleta() {
            `when`(assignmentRepository.findByIdAndActiveTrue(100L)).thenReturn(assignment())
            `when`(purchaseRepository.existsPurchaseNormalized(1L, email)).thenReturn(true)
            `when`(progressRepository.isLessonCompleted(email, 10L)).thenReturn(false)
            `when`(lessonRepository.findById(10L))
                .thenReturn(Optional.of(Lesson(id = 10L, courseId = 1L, title = "Variables y tipos de datos")))

            val e = assertThrows(ForbiddenException::class.java) {
                service.save(100L, email, dto())
            }
            assertTrue(e.message!!.contains("Variables y tipos de datos"))
        }

        @Test
        @DisplayName("la tarea del curso completo exige todas las lecciones")
        fun cursoCompletoIncompleto() {
            val a = assignment(lessonId = null)
            `when`(assignmentRepository.findByIdAndActiveTrue(100L)).thenReturn(a)
            `when`(purchaseRepository.existsPurchaseNormalized(1L, email)).thenReturn(true)
            `when`(lessonRepository.countByCourseIdAndActiveTrue(1L)).thenReturn(8)
            `when`(progressRepository.countCompletedByEmailAndCourse(email, 1L)).thenReturn(5)

            val e = assertThrows(ForbiddenException::class.java) {
                service.save(100L, email, dto())
            }
            assertTrue(e.message!!.contains("Llevas 5"))
        }
    }

    // =====================================================
    // Reglas 3 y 4: una fila, cerrada al calificar
    // =====================================================
    @Nested
    @DisplayName("Reglas 3 y 4 - una fila por tarea, cerrada al calificar")
    inner class OneRow {

        @Test
        @DisplayName("el borrador se sobrescribe, no crea una segunda fila")
        fun borradorSobrescribe() {
            val a = assignment()
            happyPath(a)
            val previo = Submission(
                id = 7L, assignmentId = 100L, courseId = 1L,
                userEmail = email, content = "intento viejo", status = SubmissionStatus.DRAFT
            )
            `when`(submissionRepository.findMine(100L, email)).thenReturn(previo)

            val result = service.save(100L, email, dto(content = "intento nuevo", draft = true))

            assertEquals(7L, result.id, "Debe reutilizar la misma fila")
            assertEquals("intento nuevo", result.content)
            assertEquals("DRAFT", result.status)
            assertNull(result.submittedAt, "Un borrador no tiene fecha de entrega")
        }

        @Test
        @DisplayName("una entrega ya calificada no admite cambios")
        fun calificadaCerrada() {
            val a = assignment()
            `when`(assignmentRepository.findByIdAndActiveTrue(100L)).thenReturn(a)
            `when`(purchaseRepository.existsPurchaseNormalized(1L, email)).thenReturn(true)
            `when`(progressRepository.isLessonCompleted(email, 10L)).thenReturn(true)
            `when`(submissionRepository.findMine(100L, email)).thenReturn(
                Submission(
                    id = 7L, assignmentId = 100L, courseId = 1L, userEmail = email,
                    content = "entregado", status = SubmissionStatus.GRADED, score = 85.0
                )
            )

            val e = assertThrows(ConflictException::class.java) {
                service.save(100L, email, dto(content = "quiero cambiarla"))
            }
            assertTrue(e.message!!.contains("85"))
            verify(submissionRepository, never()).save(anyObj<Submission>())
        }

        @Test
        @DisplayName("se puede reenviar mientras siga sin calificar")
        fun reenvioPermitido() {
            val a = assignment()
            happyPath(a)
            `when`(submissionRepository.findMine(100L, email)).thenReturn(
                Submission(
                    id = 7L, assignmentId = 100L, courseId = 1L, userEmail = email,
                    content = "primera version", status = SubmissionStatus.SUBMITTED,
                    submittedAt = LocalDateTime.now().minusDays(1)
                )
            )

            val result = service.save(100L, email, dto(content = "version corregida"))

            assertEquals("SUBMITTED", result.status)
            assertEquals("version corregida", result.content)
        }
    }

    // =====================================================
    // Regla 5: contenido valido
    // =====================================================
    @Nested
    @DisplayName("Regla 5 - contenido valido")
    inner class ContentRules {

        @Test
        @DisplayName("una entrega en blanco se rechaza")
        fun contenidoVacio() {
            happyPath()
            assertThrows(IllegalArgumentException::class.java) {
                service.save(100L, email, dto(content = "    "))
            }
        }

        @Test
        @DisplayName("una tarea de tipo FILE exige enlace al entregar")
        fun fileSinEnlace() {
            val a = assignment(type = AssignmentType.FILE)
            happyPath(a)
            val e = assertThrows(IllegalArgumentException::class.java) {
                service.save(100L, email, dto(content = "mi repo"))
            }
            assertTrue(e.message!!.contains("enlace"))
        }

        @Test
        @DisplayName("el enlace tiene que ser http o https")
        fun enlaceInvalido() {
            val a = assignment(type = AssignmentType.FILE)
            happyPath(a)
            val e = assertThrows(IllegalArgumentException::class.java) {
                service.save(100L, email, dto(content = "mi repo", file = "C:/Users/diego/proyecto"))
            }
            assertTrue(e.message!!.contains("http"))
        }

        @Test
        @DisplayName("el borrador de una tarea FILE no exige enlace todavia")
        fun borradorFileSinEnlace() {
            val a = assignment(type = AssignmentType.FILE)
            happyPath(a)
            `when`(submissionRepository.findMine(100L, email)).thenReturn(null)

            val result = service.save(100L, email, dto(content = "voy a medias", draft = true))

            assertEquals("DRAFT", result.status)
        }
    }

    // =====================================================
    // Plazo relativo a la compra
    // =====================================================
    @Nested
    @DisplayName("Plazo contado desde la compra")
    inner class DueDate {

        @Test
        @DisplayName("entregar dentro del plazo no marca retraso")
        fun aTiempo() {
            val a = assignment(days = 5)
            happyPath(a, at = LocalDateTime.now().minusDays(2))
            `when`(submissionRepository.findMine(100L, email)).thenReturn(null)

            val result = service.save(100L, email, dto())

            assertFalse(result.late)
            assertNotNull(result.submittedAt)
            assertNotNull(result.dueDate)
        }

        @Test
        @DisplayName("entregar fuera de plazo se registra pero no se bloquea")
        fun tardeNoBloquea() {
            val a = assignment(days = 5)
            happyPath(a, at = LocalDateTime.now().minusDays(20))
            `when`(submissionRepository.findMine(100L, email)).thenReturn(null)

            val result = service.save(100L, email, dto())

            assertEquals("SUBMITTED", result.status, "Entregar tarde no puede impedir entregar")
            assertTrue(result.late, "Pero queda registrado")
        }
    }

    // =====================================================
    // Vista del estudiante
    // =====================================================
    @Nested
    @DisplayName("Vista del estudiante")
    inner class StudentView {

        @Test
        @DisplayName("sin compra devuelve el motivo y el enunciado oculto")
        fun sinCompraMuestraMotivo() {
            `when`(assignmentRepository.findByIdAndActiveTrue(100L)).thenReturn(assignment())
            `when`(purchaseRepository.existsPurchaseNormalized(1L, email)).thenReturn(false)
            `when`(submissionRepository.findMine(100L, email)).thenReturn(null)
            `when`(purchaseRepository.findByUserEmailNormalized(email)).thenReturn(emptyList())

            val view = service.getStudentView(100L, email)

            assertFalse(view.canSubmit)
            assertTrue(view.blockedReason!!.contains("comprar"))
            assertTrue(view.assignment.locked)
            assertNull(view.assignment.statement)
        }

        @Test
        @DisplayName("con todo en regla queda habilitada y con dias restantes")
        fun habilitada() {
            happyPath(assignment(days = 5), at = LocalDateTime.now().minusDays(1))
            `when`(submissionRepository.findMine(100L, email)).thenReturn(null)
            `when`(lessonRepository.findById(10L))
                .thenReturn(Optional.of(Lesson(id = 10L, courseId = 1L, title = "Variables y tipos de datos")))

            val view = service.getStudentView(100L, email)

            assertTrue(view.canSubmit)
            assertNull(view.blockedReason)
            assertFalse(view.assignment.locked)
            assertNotNull(view.assignment.statement)
            assertNotNull(view.daysLeft)
        }
    }
}
