package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.CourseProgressNet
import com.dieletech.mobile.data.model.Lesson
import com.dieletech.mobile.data.model.LessonProgressNet
import com.dieletech.mobile.data.model.LessonProgressUpdateRequest
import com.dieletech.mobile.data.model.toLesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * HU-07 / HU-08 — lecciones y progreso.
 *
 * Prioridad: backend real. Si no responde (sin red, servidor caido) cae a
 * los videos de muestra para que la demo del laboratorio siga siendo
 * reproducible desde el aula. No hay mock de PROGRESO: el progreso real
 * requiere persistencia y falsearlo rompe la trazabilidad.
 */
object LessonRepository {

    private val api = RetrofitClient.api

    // Videos públicos (Google sample bucket) — fallback de demo.
    private const val V1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    private const val V2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
    private const val V3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
    private const val V4 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
    private const val V5 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"

    private val mockByCourse: Map<Long, List<Lesson>> = mapOf(
        1L to listOf(
            Lesson(101, 1, "Bienvenida y filosofía de Python", "Qué es Python, dónde se usa y por qué es tu mejor primer lenguaje.", V1, 320, 1),
            Lesson(102, 1, "Variables, tipos y operadores", "Fundamentos sintácticos para escribir tu primer script.", V2, 480, 2),
            Lesson(103, 1, "Estructuras de control (if / for / while)", "Toma de decisiones y repetición.", V3, 540, 3),
            Lesson(104, 1, "Funciones y módulos", "Reutilizar código como un profesional.", V4, 610, 4),
            Lesson(105, 1, "Proyecto final: mini calculadora CLI", "Aplicas todo en un proyecto real.", V5, 720, 5),
        ),
        2L to listOf(
            Lesson(201, 2, "Estructura semántica en HTML5", "Etiquetas correctas = mejor SEO y accesibilidad.", V1, 360, 1),
            Lesson(202, 2, "Layouts modernos con Flexbox y Grid", "Diseños responsivos sin sufrir.", V2, 540, 2),
            Lesson(203, 2, "JavaScript esencial para el DOM", "Interactividad sin frameworks.", V3, 600, 3),
            Lesson(204, 2, "Fetch API y consumo de servicios", "Conectar tu front con un backend real.", V4, 480, 4),
        ),
        3L to listOf(
            Lesson(301, 3, "¿Qué es Git y por qué lo necesitas?", "Historia, control de versiones y flujo básico.", V1, 300, 1),
            Lesson(302, 3, "Comandos esenciales: add, commit, push", "El día a día de un desarrollador.", V2, 420, 2),
            Lesson(303, 3, "Branches, merge y resolución de conflictos", "Trabajar en equipo sin romper todo.", V3, 540, 3),
        ),
        4L to listOf(
            Lesson(401, 4, "Introducción a Spring Boot 3", "Filosofía, autoconfiguración y starters.", V1, 500, 1),
            Lesson(402, 4, "Tu primer REST Controller", "GET, POST, PUT, DELETE en minutos.", V2, 620, 2),
            Lesson(403, 4, "JPA + MySQL: persistencia real", "Del entity al endpoint.", V3, 700, 3),
            Lesson(404, 4, "Seguridad con JWT", "Login, tokens y endpoints protegidos.", V4, 780, 4),
        ),
        5L to listOf(
            Lesson(501, 5, "Componentes y JSX", "El átomo de React.", V1, 420, 1),
            Lesson(502, 5, "Hooks: useState y useEffect", "Estado y efectos secundarios.", V2, 540, 2),
            Lesson(503, 5, "Consumo de APIs y React Query", "Datos remotos sin dolor.", V3, 600, 3),
        ),
        6L to listOf(
            Lesson(601, 6, "Modelado relacional desde cero", "Entidades, relaciones y normalización.", V1, 480, 1),
            Lesson(602, 6, "SELECT avanzado: JOINs y subconsultas", "Consultas que resuelven problemas reales.", V2, 600, 2),
            Lesson(603, 6, "Índices y optimización", "Cuando tu query tarda 3 segundos, aquí está la respuesta.", V3, 540, 3),
        ),
    )

    suspend fun byCourse(courseId: Long): List<Lesson> = withContext(Dispatchers.IO) {
        val remote = runCatching { api.lessonsByCourse(courseId) }.getOrNull()
        if (!remote.isNullOrEmpty()) remote.map { it.toLesson() }
        else mockByCourse[courseId].orEmpty()
    }

    suspend fun findLesson(lessonId: Long): Lesson? = withContext(Dispatchers.IO) {
        mockByCourse.values.flatten().firstOrNull { it.id == lessonId }
    }

    suspend fun saveProgress(
        lessonId: Long,
        email: String,
        completed: Boolean?,
        lastPositionSeconds: Int?
    ): LessonProgressNet? = withContext(Dispatchers.IO) {
        runCatching {
            api.saveLessonProgress(
                lessonId,
                LessonProgressUpdateRequest(email = email, completed = completed, lastPositionSeconds = lastPositionSeconds)
            )
        }.getOrNull()
    }

    suspend fun courseProgress(courseId: Long, email: String): CourseProgressNet? = withContext(Dispatchers.IO) {
        runCatching { api.courseProgress(courseId, email) }.getOrNull()
    }
}
