package com.dieletech.backend.service

import com.dieletech.backend.dto.CreateAssignmentDTO
import com.dieletech.backend.error.ForbiddenException
import com.dieletech.backend.error.NotFoundException
import com.dieletech.backend.model.Assignment
import com.dieletech.backend.model.AssignmentType
import com.dieletech.backend.model.Course
import com.dieletech.backend.model.Lesson
import com.dieletech.backend.model.Role
import com.dieletech.backend.model.User
import com.dieletech.backend.repository.AssignmentRepository
import com.dieletech.backend.repository.CourseRepository
import com.dieletech.backend.repository.LessonRepository
import com.dieletech.backend.repository.PurchaseRepository
import com.dieletech.backend.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import service.AssignmentService
import java.util.*

/**
 * HU-37. Una prueba por cada regla del servicio, mas los caminos felices.
 * Las reglas se comprueban en el servidor, asi que es aqui donde tienen
 * que fallar: si una de estas pruebas se pone en verde por accidente, el
 * enunciado de un curso pago queda abierto.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("AssignmentService - HU-37")
class AssignmentServiceTest {

    @Mock private lateinit var assignmentRepository: AssignmentRepository
    @Mock private lateinit var courseRepository: CourseRepository
    @Mock private lateinit var lessonRepository: LessonRepository
    @Mock private lateinit var purchaseRepository: PurchaseRepository
    @Mock private lateinit var userRepository: UserRepository

    private lateinit var service: AssignmentService

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObj(): T = any<T>() as T

    private val instructorEmail = "profe@dieletech.com"
    private val studentEmail = "diego@test.com"

    @BeforeEach
    fun setUp() {
        service = AssignmentService(
            assignmentRepository, courseRepository,
            lessonRepository, purchaseRepository, userRepository
        )
    }

    // ── Datos de apoyo ──

    private fun course(id: Long = 1L, owner: String? = instructorEmail) = Course(
        id = id,
        title = "Fundamentos de Python",
        description = "Curso de prueba",
        technology = "Python",
        level = "Principiante",
        price = 79900.0,
        duration = 40,
        instructorEmail = owner
    )

    private fun lesson(id: Long = 10L, courseId: Long = 1L) =
        Lesson(id = id, courseId = courseId, title = "Variables y tipos de datos")

    private fun user(email: String, role: Role) =
        User(id = 1L, name = "Usuario", email = email, password = "x", role = role)

    private fun assignment(id: Long = 100L, courseId: Long = 1L, lessonId: Long? = 10L) =
        Assignment(
            id = id,
            courseId = courseId,
            lessonId = lessonId,
            title = "Conversor de unidades",
            statement = "Escribe un programa que convierta grados Celsius a Fahrenheit.",
            type = AssignmentType.CODE
        )

    private fun validDto(lessonId: Long? = 10L) = CreateAssignmentDTO(
        title = "Conversor de unidades",
        statement = "Escribe un programa que convierta grados Celsius a Fahrenheit y Kelvin.",
        type = "CODE",
        lessonId = lessonId,
        maxScore = 100.0,
        dueOffsetDays = 5
    )

    // =====================================================
    // Regla 1: el enunciado exige compra
    // =====================================================
    @Nested
    @DisplayName("Regla 1 - el enunciado exige compra")
    inner class PurchaseGate {

        @Test
        @DisplayName("sin compra la tarea llega bloqueada y sin enunciado")
        fun sinCompraBloquea() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(purchaseRepository.existsPurchaseNormalized(1L, studentEmail)).thenReturn(false)
            `when`(lessonRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(1L))
                .thenReturn(listOf(lesson()))
            `when`(assignmentRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(1L))
                .thenReturn(listOf(assignment()))

            val result = service.listForStudent(1L, studentEmail)

            assertEquals(1, result.size)
            assertTrue(result[0].locked)
            assertNull(result[0].statement, "El enunciado no puede viajar sin compra")
            assertEquals("Conversor de unidades", result[0].title)
        }

        @Test
        @DisplayName("con compra el enunciado viaja completo")
        fun conCompraDesbloquea() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(purchaseRepository.existsPurchaseNormalized(1L, studentEmail)).thenReturn(true)
            `when`(lessonRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(1L))
                .thenReturn(listOf(lesson()))
            `when`(assignmentRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(1L))
                .thenReturn(listOf(assignment()))

            val result = service.listForStudent(1L, studentEmail)

            assertFalse(result[0].locked)
            assertNotNull(result[0].statement)
            assertEquals("Variables y tipos de datos", result[0].lessonTitle)
        }

        @Test
        @DisplayName("un visitante sin sesion tampoco ve el enunciado")
        fun visitanteBloqueado() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(lessonRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(1L))
                .thenReturn(emptyList())
            `when`(assignmentRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(1L))
                .thenReturn(listOf(assignment()))

            val result = service.listForStudent(1L, null)

            assertTrue(result[0].locked)
            assertNull(result[0].statement)
            verify(purchaseRepository, never()).existsPurchaseNormalized(anyLong(), anyString())
        }

        @Test
        @DisplayName("un curso inexistente responde 404")
        fun cursoInexistente() {
            `when`(courseRepository.findById(99L)).thenReturn(Optional.empty())
            assertThrows(NotFoundException::class.java) {
                service.listForStudent(99L, studentEmail)
            }
        }
    }

    // =====================================================
    // Regla 4: solo el dueño del curso gestiona
    // =====================================================
    @Nested
    @DisplayName("Regla 4 - solo el dueño del curso gestiona")
    inner class Ownership {

        @Test
        @DisplayName("el instructor dueño puede crear")
        fun duenoCrea() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail(instructorEmail))
                .thenReturn(Optional.of(user(instructorEmail, Role.INSTRUCTOR)))
            `when`(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson()))
            `when`(assignmentRepository.findByCourseIdAndLessonIdAndActiveTrue(1L, 10L)).thenReturn(null)
            `when`(assignmentRepository.countByCourseIdAndActiveTrue(1L)).thenReturn(0)
            `when`(assignmentRepository.save(anyObj<Assignment>())).thenAnswer { it.arguments[0] }

            val result = service.create(1L, validDto(), instructorEmail)

            assertEquals("Conversor de unidades", result.title)
            assertEquals("CODE", result.type)
            assertEquals(1, result.orderIndex)
        }

        @Test
        @DisplayName("un instructor ajeno recibe 403, no 200")
        fun instructorAjenoRechazado() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail("otro@dieletech.com"))
                .thenReturn(Optional.of(user("otro@dieletech.com", Role.INSTRUCTOR)))

            assertThrows(ForbiddenException::class.java) {
                service.create(1L, validDto(), "otro@dieletech.com")
            }
            verify(assignmentRepository, never()).save(anyObj<Assignment>())
        }

        @Test
        @DisplayName("un ADMIN puede gestionar cualquier curso")
        fun adminPuedeTodo() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail("admin@dieletech.com"))
                .thenReturn(Optional.of(user("admin@dieletech.com", Role.ADMIN)))
            `when`(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson()))
            `when`(assignmentRepository.findByCourseIdAndLessonIdAndActiveTrue(1L, 10L)).thenReturn(null)
            `when`(assignmentRepository.countByCourseIdAndActiveTrue(1L)).thenReturn(3)
            `when`(assignmentRepository.save(anyObj<Assignment>())).thenAnswer { it.arguments[0] }

            val result = service.create(1L, validDto(), "admin@dieletech.com")

            assertEquals(4, result.orderIndex)
        }

        @Test
        @DisplayName("un curso sin instructor asignado solo lo gestiona un ADMIN")
        fun cursoHuerfano() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course(owner = null)))
            `when`(userRepository.findByEmail(instructorEmail))
                .thenReturn(Optional.of(user(instructorEmail, Role.INSTRUCTOR)))

            assertThrows(ForbiddenException::class.java) {
                service.create(1L, validDto(), instructorEmail)
            }
        }
    }

    // =====================================================
    // Reglas 2 y 3: integridad del modulo
    // =====================================================
    @Nested
    @DisplayName("Reglas 2 y 3 - integridad del modulo")
    inner class ModuleIntegrity {

        @Test
        @DisplayName("un modulo no admite una segunda tarea")
        fun moduloOcupado() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail(instructorEmail))
                .thenReturn(Optional.of(user(instructorEmail, Role.INSTRUCTOR)))
            `when`(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson()))
            `when`(assignmentRepository.findByCourseIdAndLessonIdAndActiveTrue(1L, 10L))
                .thenReturn(assignment(id = 55L))

            val e = assertThrows(IllegalArgumentException::class.java) {
                service.create(1L, validDto(), instructorEmail)
            }
            assertTrue(e.message!!.contains("ya tiene una tarea"))
            verify(assignmentRepository, never()).save(anyObj<Assignment>())
        }

        @Test
        @DisplayName("editar la propia tarea del modulo no choca consigo misma")
        fun editarNoChocaConsigoMisma() {
            val existing = assignment(id = 100L)
            `when`(assignmentRepository.findByIdAndActiveTrue(100L)).thenReturn(existing)
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail(instructorEmail))
                .thenReturn(Optional.of(user(instructorEmail, Role.INSTRUCTOR)))
            `when`(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson()))
            `when`(assignmentRepository.findByCourseIdAndLessonIdAndActiveTrue(1L, 10L))
                .thenReturn(existing)
            `when`(assignmentRepository.save(anyObj<Assignment>())).thenAnswer { it.arguments[0] }

            val result = service.update(100L, validDto().copy(title = "Titulo corregido"), instructorEmail)

            assertEquals("Titulo corregido", result.title)
        }

        @Test
        @DisplayName("no se puede colgar la tarea de una leccion de otro curso")
        fun leccionDeOtroCurso() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail(instructorEmail))
                .thenReturn(Optional.of(user(instructorEmail, Role.INSTRUCTOR)))
            `when`(lessonRepository.findById(77L)).thenReturn(Optional.of(lesson(id = 77L, courseId = 2L)))

            val e = assertThrows(IllegalArgumentException::class.java) {
                service.create(1L, validDto(lessonId = 77L), instructorEmail)
            }
            assertTrue(e.message!!.contains("otro curso"))
        }

        @Test
        @DisplayName("una tarea sin leccion es del curso completo y no valida modulo")
        fun tareaDeCursoCompleto() {
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail(instructorEmail))
                .thenReturn(Optional.of(user(instructorEmail, Role.INSTRUCTOR)))
            `when`(assignmentRepository.countByCourseIdAndActiveTrue(1L)).thenReturn(0)
            `when`(assignmentRepository.save(anyObj<Assignment>())).thenAnswer { it.arguments[0] }

            val result = service.create(1L, validDto(lessonId = null), instructorEmail)

            assertNull(result.lessonId)
            verify(lessonRepository, never()).findById(anyLong())
        }
    }

    // =====================================================
    // Archivado logico
    // =====================================================
    @Nested
    @DisplayName("Archivado logico")
    inner class Archiving {

        @Test
        @DisplayName("archivar no borra la fila, solo la desactiva")
        fun archivarDesactiva() {
            val existing = assignment()
            `when`(assignmentRepository.findByIdAndActiveTrue(100L)).thenReturn(existing)
            `when`(courseRepository.findById(1L)).thenReturn(Optional.of(course()))
            `when`(userRepository.findByEmail(instructorEmail))
                .thenReturn(Optional.of(user(instructorEmail, Role.INSTRUCTOR)))

            service.archive(100L, instructorEmail)

            assertFalse(existing.active)
            verify(assignmentRepository).save(existing)
            verify(assignmentRepository, never()).delete(anyObj<Assignment>())
        }

        @Test
        @DisplayName("archivar una tarea inexistente responde 404")
        fun archivarInexistente() {
            `when`(assignmentRepository.findByIdAndActiveTrue(404L)).thenReturn(null)
            assertThrows(NotFoundException::class.java) {
                service.archive(404L, instructorEmail)
            }
        }
    }
}
