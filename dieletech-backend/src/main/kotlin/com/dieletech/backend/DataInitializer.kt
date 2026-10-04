package com.dieletech.backend

import com.dieletech.backend.model.Course
import com.dieletech.backend.model.Lesson
import com.dieletech.backend.repository.CourseRepository
import com.dieletech.backend.repository.LessonRepository
import com.dieletech.backend.repository.PurchaseRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Siembra y mantiene el catalogo base.
 *
 * Es idempotente y de tipo upsert: en cada arranque actualiza el contenido
 * de los cursos semilla (descripciones, temario, videos, cupos) sin duplicar
 * registros ni perder compras. Ademas recalcula studentCount desde la tabla
 * purchases, de modo que el numero de estudiantes SIEMPRE refleja matriculas
 * reales y nunca una cifra sembrada.
 */
@Component
@Order(1)
class DataInitializer(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val purchaseRepository: PurchaseRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val seeds = catalogSeed()
        val existing = courseRepository.findAll().associateBy { it.title }

        var created = 0
        var updated = 0

        for (seed in seeds) {
            val current = existing[seed.title]
            if (current == null) {
                val saved = courseRepository.save(seed.toCourse())
                syncLessons(saved, seed)
                created++
            } else {
                seed.applyTo(current)
                val saved = courseRepository.save(current)
                syncLessons(saved, seed)
                updated++
            }
        }

        val corrected = recalculateStudentCounts()

        println("Catalogo Dieletech: $created cursos creados, $updated actualizados, $corrected contadores sincronizados")
    }

    /**
     * Recalcula el numero de matriculados de cada curso contando compras
     * reales. Borra de raiz cualquier cifra inflada de versiones anteriores.
     */
    private fun recalculateStudentCounts(): Int {
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

    /** Crea las lecciones del curso la primera vez; despues solo repara faltantes. */
    private fun syncLessons(course: Course, seed: CourseSeed) {
        val existing = lessonRepository
            .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(course.id)
            .associateBy { it.title }

        val toSave = mutableListOf<Lesson>()

        seed.lessons.forEachIndexed { index, l ->
            val found = existing[l.title]
            if (found == null) {
                toSave.add(
                    Lesson(
                        courseId = course.id,
                        title = l.title,
                        description = l.description,
                        videoUrl = l.videoUrl,
                        orderIndex = index + 1,
                        durationMinutes = l.minutes,
                        contentType = "VIDEO",
                        freePreview = l.free
                    )
                )
            } else {
                // Mantiene sincronizado el video y el flag de muestra gratuita.
                found.videoUrl = l.videoUrl
                found.description = l.description
                found.durationMinutes = l.minutes
                found.freePreview = l.free
                found.orderIndex = index + 1
                toSave.add(found)
            }
        }

        if (toSave.isNotEmpty()) lessonRepository.saveAll(toSave)
    }

    // ══════════════════════════════════════════════════════════
    //  Definicion del catalogo
    // ══════════════════════════════════════════════════════════

    private data class LessonSeed(
        val title: String,
        val description: String,
        val videoUrl: String,
        val minutes: Int,
        val free: Boolean = false
    )

    private data class CourseSeed(
        val title: String,
        val description: String,
        val longDescription: String,
        val objectives: List<String>,
        val audience: List<String>,
        val technology: String,
        val level: String,
        val price: Double,
        val duration: Int,
        val capacity: Int,
        val imageUrl: String,
        val prerequisites: List<String>,
        val previewVideoUrl: String,
        val instructorName: String,
        val lessons: List<LessonSeed>
    ) {
        fun toCourse() = Course(
            title = title,
            description = description,
            longDescription = longDescription,
            learningObjectives = objectives.joinToString("|"),
            targetAudience = audience.joinToString("|"),
            technology = technology,
            level = level,
            price = price,
            duration = duration,
            capacity = capacity,
            imageUrl = imageUrl,
            studentCount = 0,
            curriculum = lessons.joinToString("|") { it.title },
            prerequisites = prerequisites.joinToString("|"),
            previewVideoUrl = previewVideoUrl,
            instructorName = instructorName
        )

        /** Actualiza contenido sin tocar id, compras ni studentCount. */
        fun applyTo(c: Course) {
            c.description = description
            c.longDescription = longDescription
            c.learningObjectives = objectives.joinToString("|")
            c.targetAudience = audience.joinToString("|")
            c.technology = technology
            c.level = level
            c.duration = duration
            c.imageUrl = imageUrl
            c.curriculum = lessons.joinToString("|") { it.title }
            c.prerequisites = prerequisites.joinToString("|")
            c.previewVideoUrl = previewVideoUrl
            c.instructorName = instructorName
            if (c.capacity <= 0) c.capacity = capacity
            c.updatedAt = java.time.LocalDateTime.now()
        }
    }

    /** Construye la URL de embed con salto al minuto del tema. */
    private fun yt(videoId: String, startSeconds: Int = 0): String =
        if (startSeconds > 0) "https://www.youtube.com/embed/$videoId?start=$startSeconds"
        else "https://www.youtube.com/embed/$videoId"

    private fun catalogSeed(): List<CourseSeed> = listOf(

        // ─────────────────── PYTHON ───────────────────
        CourseSeed(
            title = "Fundamentos de Python",
            description = "Aprende programacion desde cero con Python: sintaxis, estructuras de datos, POO y un proyecto real de gestion.",
            longDescription = """
                Python es hoy el lenguaje de entrada mas solicitado del mercado: automatizacion, analisis de datos,
                backend e inteligencia artificial comparten la misma base que aprenderas aqui.

                El curso arranca sin dar nada por sabido. Empiezas instalando el entorno y escribiendo tu primera
                linea, y terminas construyendo un sistema de gestion completo que lee y escribe archivos, valida
                entradas del usuario y organiza la logica en clases.

                Cada modulo cierra con un ejercicio que se corrige solo: escribes codigo, lo ejecutas y ves el
                resultado. No hay teoria suelta; todo lo que se explica se usa inmediatamente en el proyecto que
                vas armando clase a clase.

                La leccion 1 es de acceso libre para que evalues el estilo del curso antes de decidir.
            """.trimIndent(),
            objectives = listOf(
                "Escribir programas en Python usando variables, condicionales y ciclos con soltura",
                "Modelar problemas reales con listas, diccionarios, tuplas y conjuntos",
                "Organizar codigo en funciones y modulos reutilizables",
                "Leer y escribir archivos de texto, CSV y JSON",
                "Aplicar Programacion Orientada a Objetos: clases, herencia y encapsulamiento",
                "Entregar un sistema de gestion funcional como proyecto final"
            ),
            audience = listOf(
                "Personas sin experiencia previa en programacion",
                "Estudiantes de carreras tecnicas que necesitan una base solida",
                "Profesionales de otras areas que quieren automatizar tareas repetitivas"
            ),
            technology = "Python",
            level = "Principiante",
            price = 79900.0,
            duration = 40,
            capacity = 60,
            imageUrl = "/images/python-course.png",
            prerequisites = listOf(
                "Computador con Windows, macOS o Linux",
                "Python 3.10 o superior instalado",
                "Editor de codigo (VS Code recomendado)",
                "Ninguna experiencia previa en programacion"
            ),
            previewVideoUrl = yt("chPhlsHoEPo"),
            instructorName = "Equipo Dieletech",
            lessons = listOf(
                LessonSeed("Introduccion a Python y configuracion del entorno",
                    "Que es Python, por que domina el mercado y como dejar tu entorno listo para programar. Leccion de muestra gratuita.",
                    yt("chPhlsHoEPo"), 35, free = true),
                LessonSeed("Variables y tipos de datos",
                    "Numeros, cadenas, booleanos y conversion entre tipos. El sistema de tipos de Python en la practica.",
                    yt("chPhlsHoEPo", 900), 40),
                LessonSeed("Estructuras de control: if, for y while",
                    "Toma de decisiones y repeticion. Cuando conviene cada ciclo y como evitar bucles infinitos.",
                    yt("chPhlsHoEPo", 2400), 45),
                LessonSeed("Colecciones: listas, diccionarios y tuplas",
                    "La estructura de datos correcta para cada problema, con comprensiones de listas.",
                    yt("chPhlsHoEPo", 4200), 50),
                LessonSeed("Funciones y modulos",
                    "Parametros, retorno, alcance de variables y como dividir un programa en modulos.",
                    yt("chPhlsHoEPo", 6000), 45),
                LessonSeed("Manejo de archivos y excepciones",
                    "Lectura y escritura de texto, CSV y JSON. Control de errores con try/except.",
                    yt("chPhlsHoEPo", 8400), 40),
                LessonSeed("Programacion Orientada a Objetos",
                    "Clases, objetos, herencia y encapsulamiento aplicados al proyecto del curso.",
                    yt("chPhlsHoEPo", 10800), 55),
                LessonSeed("Proyecto final: sistema de gestion",
                    "Integracion de todo lo aprendido en una aplicacion de consola con persistencia en archivos.",
                    yt("chPhlsHoEPo", 13200), 60)
            )
        ),

        // ─────────────────── WEB ───────────────────
        CourseSeed(
            title = "HTML, CSS y JavaScript Avanzado",
            description = "Domina el frontend moderno: HTML semantico, CSS Grid y Flexbox, JavaScript ES6+ y consumo de APIs REST.",
            longDescription = """
                Este curso cubre la trinidad del desarrollo web con un enfoque profesional: no se trata de copiar
                plantillas, sino de entender por que una pagina se comporta como lo hace.

                Empiezas por HTML semantico y accesibilidad, porque una estructura correcta es lo que hace que un
                sitio sea indexable y usable por lectores de pantalla. Luego entras a CSS moderno, donde Grid y
                Flexbox reemplazan los trucos de posicionamiento del pasado y resuelven layouts responsivos con
                unas pocas lineas.

                La segunda mitad es JavaScript: manipulacion del DOM, eventos, programacion asincrona con
                Promises y async/await, y consumo de APIs REST reales. Cierras construyendo una pagina interactiva
                que consulta un servicio externo y renderiza los resultados.

                La leccion 1 es de acceso libre.
            """.trimIndent(),
            objectives = listOf(
                "Estructurar paginas con HTML5 semantico y criterios de accesibilidad",
                "Resolver cualquier layout responsivo con CSS Grid y Flexbox",
                "Dominar JavaScript ES6+: arrow functions, destructuring, modulos y spread",
                "Manipular el DOM y gestionar eventos sin librerias externas",
                "Consumir APIs REST con fetch, Promises y async/await",
                "Publicar una pagina interactiva funcionando en produccion"
            ),
            audience = listOf(
                "Quienes ya escribieron HTML basico y quieren profesionalizarse",
                "Desarrolladores backend que necesitan defenderse en el frontend",
                "Estudiantes que preparan su portafolio web"
            ),
            technology = "HTML/CSS/JS",
            level = "Intermedio",
            price = 99900.0,
            duration = 60,
            capacity = 80,
            imageUrl = "/images/web-course.png",
            prerequisites = listOf(
                "Nociones basicas de programacion (variables y condicionales)",
                "Editor de codigo instalado",
                "Navegador moderno con herramientas de desarrollador"
            ),
            previewVideoUrl = yt("G3e-cpL7ofc"),
            instructorName = "Equipo Dieletech",
            lessons = listOf(
                LessonSeed("HTML5 semantico y accesibilidad",
                    "Etiquetas con significado, jerarquia de encabezados y atributos ARIA. Leccion de muestra gratuita.",
                    yt("G3e-cpL7ofc"), 40, free = true),
                LessonSeed("CSS moderno: el modelo de caja y selectores",
                    "Especificidad, cascada, unidades relativas y variables CSS.",
                    yt("G3e-cpL7ofc", 1800), 45),
                LessonSeed("Flexbox en profundidad",
                    "Ejes, alineacion y distribucion. Patrones de layout resueltos con Flexbox.",
                    yt("G3e-cpL7ofc", 3600), 50),
                LessonSeed("CSS Grid y diseno responsivo",
                    "Grid areas, media queries y estrategia mobile-first.",
                    yt("G3e-cpL7ofc", 5400), 55),
                LessonSeed("JavaScript ES6+",
                    "let/const, arrow functions, destructuring, spread, template literals y modulos.",
                    yt("G3e-cpL7ofc", 7200), 60),
                LessonSeed("Manipulacion del DOM y eventos",
                    "Seleccion de nodos, creacion dinamica de elementos y delegacion de eventos.",
                    yt("G3e-cpL7ofc", 9000), 50),
                LessonSeed("Asincronia: Promises, async/await y fetch",
                    "El event loop, manejo de errores en codigo asincrono y llamadas a APIs REST.",
                    yt("G3e-cpL7ofc", 10800), 55),
                LessonSeed("Proyecto: pagina interactiva con API",
                    "Aplicacion que consulta un servicio externo, filtra y renderiza resultados.",
                    yt("G3e-cpL7ofc", 12600), 65)
            )
        ),

        // ─────────────────── GIT ───────────────────
        CourseSeed(
            title = "Git y Control de Versiones",
            description = "Git y GitHub de cero a flujo profesional: ramas, merges, resolucion de conflictos y Pull Requests.",
            longDescription = """
                Git es el requisito silencioso de toda oferta de trabajo en desarrollo. No aparece como "deseable":
                se asume. Este curso cierra esa brecha en veinte horas.

                Aprendes el modelo mental primero (que es realmente un commit, que es HEAD, por que una rama es solo
                un puntero) y despues los comandos. Ese orden importa: quien entiende el modelo deja de memorizar
                comandos y empieza a resolver situaciones nuevas por su cuenta.

                Dedicamos un modulo completo a la resolucion de conflictos, que es donde la mayoria se traba, y otro
                a los flujos de trabajo profesionales: GitFlow, trunk-based, Pull Requests con revision de codigo y
                proteccion de ramas.

                La leccion 1 es de acceso libre.
            """.trimIndent(),
            objectives = listOf(
                "Explicar el modelo de datos de Git: commits, arbol, HEAD y referencias",
                "Trabajar con ramas: crear, fusionar, rebasar y eliminar sin miedo",
                "Resolver conflictos de merge con criterio en lugar de por ensayo y error",
                "Colaborar en GitHub con Pull Requests y revision de codigo",
                "Recuperar trabajo perdido con reflog, reset y revert",
                "Aplicar un flujo de trabajo profesional en equipo"
            ),
            audience = listOf(
                "Desarrolladores que aun trabajan copiando carpetas con fecha",
                "Equipos que quieren estandarizar su forma de colaborar",
                "Estudiantes que preparan su primer trabajo en equipo"
            ),
            technology = "Git",
            level = "Principiante",
            price = 49900.0,
            duration = 20,
            capacity = 100,
            imageUrl = "/images/git-course.png",
            prerequisites = listOf(
                "Manejo basico de la terminal",
                "Git instalado",
                "Cuenta gratuita en GitHub"
            ),
            previewVideoUrl = yt("RGOj5yH7evk"),
            instructorName = "Equipo Dieletech",
            lessons = listOf(
                LessonSeed("Que es Git y por que lo necesitas",
                    "Control de versiones distribuido y el modelo de datos de Git. Leccion de muestra gratuita.",
                    yt("RGOj5yH7evk"), 20, free = true),
                LessonSeed("Comandos esenciales: init, add, commit, status",
                    "El area de staging explicada de verdad y como escribir buenos mensajes de commit.",
                    yt("RGOj5yH7evk", 600), 25),
                LessonSeed("Historial: log, diff, show y reflog",
                    "Navegar el historial y recuperar trabajo que parecia perdido.",
                    yt("RGOj5yH7evk", 1500), 20),
                LessonSeed("Ramas: branch, checkout y switch",
                    "Por que una rama es solo un puntero y como eso lo hace todo mas simple.",
                    yt("RGOj5yH7evk", 2400), 25),
                LessonSeed("Merge, rebase y resolucion de conflictos",
                    "Diferencia real entre merge y rebase, y como resolver conflictos con criterio.",
                    yt("RGOj5yH7evk", 3300), 30),
                LessonSeed("GitHub: remotos, push, pull y fork",
                    "Sincronizacion con el repositorio remoto y trabajo sobre proyectos de terceros.",
                    yt("RGOj5yH7evk", 4200), 25),
                LessonSeed("Pull Requests y revision de codigo",
                    "Abrir, revisar y fusionar PRs. Proteccion de ramas y checks automaticos.",
                    yt("RGOj5yH7evk", 5100), 25),
                LessonSeed("Flujos profesionales: GitFlow y trunk-based",
                    "Cuando conviene cada estrategia segun el tamano y ritmo del equipo.",
                    yt("RGOj5yH7evk", 6000), 30)
            )
        ),

        // ─────────────────── SPRING BOOT ───────────────────
        CourseSeed(
            title = "Desarrollo Backend con Spring Boot",
            description = "APIs REST profesionales con Spring Boot: JPA, Spring Security, JWT, testing con JUnit y despliegue.",
            longDescription = """
                Spring Boot es el estandar de facto para backend empresarial en el ecosistema Java. Este curso te
                lleva desde el primer endpoint hasta una API segura, probada y desplegada.

                El recorrido sigue el orden en que se construye un sistema real: primero la capa de datos con JPA y
                el diseno de entidades, luego los servicios con la logica de negocio, despues los controladores REST
                y finalmente la seguridad con Spring Security y JWT.

                Un modulo completo esta dedicado a testing: pruebas unitarias con JUnit y Mockito, y pruebas de
                integracion con MockMvc. Esta es la diferencia entre un proyecto de portafolio y uno que pasa una
                revision tecnica.

                El curso usa exactamente el stack sobre el que corre esta plataforma. La leccion 1 es gratuita.
            """.trimIndent(),
            objectives = listOf(
                "Construir APIs REST siguiendo convenciones y codigos HTTP correctos",
                "Modelar y persistir datos con Spring Data JPA y relaciones entre entidades",
                "Implementar autenticacion y autorizacion con Spring Security y JWT",
                "Validar entradas con Bean Validation y manejar errores de forma centralizada",
                "Escribir pruebas unitarias y de integracion con JUnit 5, Mockito y MockMvc",
                "Desplegar la aplicacion y configurar perfiles por entorno"
            ),
            audience = listOf(
                "Desarrolladores Java que quieren dar el salto a backend moderno",
                "Estudiantes de arquitectura de software que necesitan un caso real",
                "Quienes ya usan Spring Boot pero sin seguridad ni pruebas"
            ),
            technology = "Java",
            level = "Avanzado",
            price = 129900.0,
            duration = 80,
            capacity = 40,
            imageUrl = "/images/spring-course.png",
            prerequisites = listOf(
                "Java 17 o superior instalado",
                "Programacion Orientada a Objetos solida",
                "SQL basico y una base de datos relacional",
                "Gradle o Maven"
            ),
            previewVideoUrl = yt("9SGDpanrc8U"),
            instructorName = "Equipo Dieletech",
            lessons = listOf(
                LessonSeed("Fundamentos de Spring Boot e inyeccion de dependencias",
                    "Contenedor de Spring, beans, autoconfiguracion y estructura del proyecto. Leccion gratuita.",
                    yt("9SGDpanrc8U"), 50, free = true),
                LessonSeed("Construyendo tu primera API REST",
                    "Controladores, verbos HTTP, codigos de estado y diseno de rutas.",
                    yt("9SGDpanrc8U", 1800), 55),
                LessonSeed("Persistencia con Spring Data JPA",
                    "Entidades, repositorios, consultas derivadas y JPQL.",
                    yt("9SGDpanrc8U", 3600), 60),
                LessonSeed("Relaciones entre entidades y DTOs",
                    "OneToMany, ManyToMany, carga perezosa y por que nunca exponer entidades directamente.",
                    yt("9SGDpanrc8U", 5400), 55),
                LessonSeed("Validacion y manejo global de errores",
                    "Bean Validation y RestControllerAdvice para respuestas de error consistentes.",
                    yt("9SGDpanrc8U", 7200), 45),
                LessonSeed("Spring Security y autenticacion con JWT",
                    "Cadena de filtros, generacion y validacion de tokens, y proteccion de endpoints por rol.",
                    yt("9SGDpanrc8U", 9000), 70),
                LessonSeed("Testing: JUnit 5, Mockito y MockMvc",
                    "Pruebas unitarias de servicios y de integracion de controladores.",
                    yt("9SGDpanrc8U", 10800), 60),
                LessonSeed("Despliegue y configuracion por perfiles",
                    "Perfiles dev y prod, variables de entorno y empaquetado para produccion.",
                    yt("9SGDpanrc8U", 12600), 45)
            )
        ),

        // ─────────────────── REACT ───────────────────
        CourseSeed(
            title = "React 19 Avanzado",
            description = "Aplicaciones React escalables: hooks avanzados, Context, React Router, rendimiento y testing.",
            longDescription = """
                React domina el frontend, pero la distancia entre "hacer un contador" y "mantener una aplicacion
                grande" es enorme. Este curso cubre esa distancia.

                Partimos de los hooks que casi nadie usa bien: useEffect y sus dependencias, useMemo y useCallback
                aplicados donde de verdad importan, useReducer para estado complejo y hooks personalizados para
                extraer logica reutilizable.

                Sigue la arquitectura de la aplicacion: Context API para estado global sin librerias externas, React
                Router para navegacion y rutas protegidas, y patrones de composicion que evitan que los componentes
                crezcan sin control.

                Cerramos con rendimiento (por que se re-renderiza lo que no deberia y como medirlo) y testing con
                Vitest y Testing Library. El proyecto final es una aplicacion completa conectada a una API REST.
            """.trimIndent(),
            objectives = listOf(
                "Usar correctamente useState, useEffect, useMemo, useCallback y useReducer",
                "Extraer logica reutilizable en hooks personalizados",
                "Manejar estado global con Context API sin sobrecargar la aplicacion",
                "Implementar navegacion y rutas protegidas con React Router",
                "Diagnosticar y corregir problemas de rendimiento con React DevTools",
                "Escribir pruebas de componentes con Vitest y Testing Library"
            ),
            audience = listOf(
                "Desarrolladores que ya hicieron proyectos pequenos en React",
                "Quienes migran de otro framework frontend",
                "Equipos que necesitan estandarizar patrones en su codebase"
            ),
            technology = "React",
            level = "Avanzado",
            price = 119900.0,
            duration = 70,
            capacity = 50,
            imageUrl = "/images/react-course.png",
            prerequisites = listOf(
                "JavaScript ES6+ con soltura",
                "HTML y CSS",
                "Node.js y npm instalados",
                "Haber construido al menos una aplicacion sencilla en React"
            ),
            previewVideoUrl = yt("CgkZ7MvWUAA"),
            instructorName = "Equipo Dieletech",
            lessons = listOf(
                LessonSeed("Repaso critico: componentes, props y estado",
                    "Los errores mas comunes en los fundamentos y como evitarlos. Leccion gratuita.",
                    yt("CgkZ7MvWUAA"), 40, free = true),
                LessonSeed("useEffect a fondo",
                    "Array de dependencias, limpieza de efectos y por que se ejecuta dos veces en modo estricto.",
                    yt("CgkZ7MvWUAA", 1800), 55),
                LessonSeed("useMemo, useCallback y memo",
                    "Cuando estas optimizaciones ayudan y cuando solo agregan complejidad.",
                    yt("CgkZ7MvWUAA", 3600), 50),
                LessonSeed("useReducer y hooks personalizados",
                    "Estado complejo con reducers y extraccion de logica en hooks propios.",
                    yt("CgkZ7MvWUAA", 5400), 55),
                LessonSeed("Context API y estado global",
                    "Proveedores, consumo eficiente y como evitar re-renderizados en cascada.",
                    yt("CgkZ7MvWUAA", 7200), 50),
                LessonSeed("React Router y rutas protegidas",
                    "Navegacion, parametros, rutas anidadas y guardas de autenticacion.",
                    yt("CgkZ7MvWUAA", 9000), 45),
                LessonSeed("Rendimiento y React DevTools",
                    "Medir antes de optimizar: profiler, listas virtualizadas y code splitting.",
                    yt("CgkZ7MvWUAA", 10800), 50),
                LessonSeed("Proyecto full-stack con API REST",
                    "Aplicacion completa con autenticacion, consumo de API y pruebas.",
                    yt("CgkZ7MvWUAA", 12600), 65)
            )
        ),

        // ─────────────────── MYSQL ───────────────────
        CourseSeed(
            title = "Base de Datos MySQL",
            description = "Diseno y administracion de bases de datos relacionales: normalizacion, SQL avanzado, indices y transacciones.",
            longDescription = """
                Una aplicacion rara vez falla por el lenguaje que usa; falla por como modelo sus datos. Este curso
                trata la base de datos como lo que es: la decision de arquitectura mas dificil de revertir.

                Empiezas por el diseno conceptual: entidades, relaciones y cardinalidad, y luego normalizacion hasta
                tercera forma normal, entendiendo que problema resuelve cada forma y cuando conviene desnormalizar
                a proposito.

                La parte de SQL va mas alla del SELECT basico: JOINs de todos los tipos, subconsultas, funciones de
                ventana y agregaciones complejas. Despues entras en rendimiento, que es donde se separan los
                perfiles junior de los senior: como funciona un indice B-tree, como leer un plan de ejecucion con
                EXPLAIN y por que una consulta se vuelve lenta al crecer la tabla.

                Cierra con transacciones ACID, niveles de aislamiento y estrategias de respaldo.
            """.trimIndent(),
            objectives = listOf(
                "Disenar un modelo relacional partiendo de requisitos de negocio",
                "Aplicar normalizacion hasta 3FN y justificar cuando desnormalizar",
                "Escribir consultas avanzadas con JOINs, subconsultas y funciones de ventana",
                "Optimizar consultas leyendo planes de ejecucion con EXPLAIN",
                "Garantizar integridad con transacciones y niveles de aislamiento",
                "Implementar una estrategia de respaldo y recuperacion"
            ),
            audience = listOf(
                "Desarrolladores que escriben SQL sin entender que pasa por debajo",
                "Estudiantes de bases de datos que necesitan practica real",
                "Profesionales que administran datos y enfrentan problemas de rendimiento"
            ),
            technology = "MySQL",
            level = "Intermedio",
            price = 89900.0,
            duration = 50,
            capacity = 70,
            imageUrl = "/images/mysql-course.png",
            prerequisites = listOf(
                "SQL basico: SELECT, INSERT, UPDATE y DELETE",
                "MySQL 8 instalado",
                "MySQL Workbench o cliente equivalente"
            ),
            previewVideoUrl = yt("7S_tz1z_5bA"),
            instructorName = "Equipo Dieletech",
            lessons = listOf(
                LessonSeed("Modelo relacional y diseno conceptual",
                    "Entidades, atributos, relaciones y cardinalidad. Leccion de muestra gratuita.",
                    yt("7S_tz1z_5bA"), 35, free = true),
                LessonSeed("Normalizacion hasta 3FN",
                    "Que problema resuelve cada forma normal y cuando desnormalizar a proposito.",
                    yt("7S_tz1z_5bA", 1200), 40),
                LessonSeed("DDL: tablas, tipos de datos y restricciones",
                    "Eleccion correcta de tipos, claves primarias y foraneas, CHECK y UNIQUE.",
                    yt("7S_tz1z_5bA", 2400), 40),
                LessonSeed("Consultas avanzadas: JOINs y subconsultas",
                    "INNER, LEFT, RIGHT y FULL. Subconsultas correlacionadas y CTEs.",
                    yt("7S_tz1z_5bA", 3600), 50),
                LessonSeed("Agregaciones y funciones de ventana",
                    "GROUP BY, HAVING, ROW_NUMBER, RANK y particiones.",
                    yt("7S_tz1z_5bA", 4800), 45),
                LessonSeed("Indices y optimizacion con EXPLAIN",
                    "Como funciona un indice B-tree y como leer un plan de ejecucion.",
                    yt("7S_tz1z_5bA", 6000), 50),
                LessonSeed("Transacciones ACID y concurrencia",
                    "COMMIT, ROLLBACK, niveles de aislamiento y bloqueos.",
                    yt("7S_tz1z_5bA", 7200), 45),
                LessonSeed("Respaldo, recuperacion y procedimientos almacenados",
                    "mysqldump, restauracion, stored procedures y triggers.",
                    yt("7S_tz1z_5bA", 8400), 45)
            )
        )
    )
}
