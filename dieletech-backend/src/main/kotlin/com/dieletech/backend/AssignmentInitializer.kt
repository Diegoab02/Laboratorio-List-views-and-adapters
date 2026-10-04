package com.dieletech.backend

import com.dieletech.backend.model.Assignment
import com.dieletech.backend.model.AssignmentType
import com.dieletech.backend.repository.AssignmentRepository
import com.dieletech.backend.repository.CourseRepository
import com.dieletech.backend.repository.LessonRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * HU-37: siembra un ejercicio practico por modulo de cada curso semilla.
 *
 * Corre despues de DataInitializer y QuizInitializer. Es idempotente por
 * (curso, leccion): reiniciar el backend no duplica tareas ni pisa el
 * enunciado que haya editado el instructor. Los modulos que ya tienen
 * tarea activa se saltan sin tocar nada.
 */
@Component
@Order(3)
class AssignmentInitializer(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val assignmentRepository: AssignmentRepository
) : CommandLineRunner {

    private class A(
        val title: String,
        val type: AssignmentType,
        val days: Int,
        val statement: String
    )

    @Transactional
    override fun run(vararg args: String?) {
        val banks = bank()
        var created = 0

        for (course in courseRepository.findAll()) {
            val tasks = banks[course.title] ?: continue
            val lessons = lessonRepository
                .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(course.id)

            lessons.forEachIndexed { index, lesson ->
                val spec = tasks.getOrNull(index) ?: return@forEachIndexed
                val already = assignmentRepository
                    .findByCourseIdAndLessonIdAndActiveTrue(course.id, lesson.id)
                if (already != null) return@forEachIndexed

                assignmentRepository.save(
                    Assignment(
                        courseId = course.id,
                        lessonId = lesson.id,
                        title = spec.title,
                        statement = spec.statement.trimIndent(),
                        type = spec.type,
                        maxScore = 100.0,
                        dueOffsetDays = spec.days,
                        orderIndex = index + 1
                    )
                )
                created++
            }
        }

        if (created > 0) println("AssignmentInitializer: $created tareas practicas creadas")
    }

    private fun bank(): Map<String, List<A>> = mapOf(

        "Fundamentos de Python" to listOf(
            A("Configura tu entorno y ejecuta tu primer script", AssignmentType.CODE, 3, """
                Instala Python 3.10 o superior y crea un archivo hola.py que imprima tu nombre,
                la version de Python que tienes instalada y la fecha de hoy.
                Entrega el codigo completo y, al final, pega la salida exacta que viste en la consola.
            """),
            A("Conversor de unidades con validacion", AssignmentType.CODE, 4, """
                Escribe un programa que pida al usuario una temperatura en grados Celsius y la
                convierta a Fahrenheit y Kelvin.
                El programa no puede caerse si el usuario escribe texto en lugar de un numero:
                debe avisar y volver a pedir el dato.
                Entrega el codigo y explica en dos lineas como manejaste la conversion de tipos.
            """),
            A("Clasificador de numeros con ciclos", AssignmentType.CODE, 4, """
                Pide al usuario diez numeros enteros y clasificalos en tres grupos: pares, impares
                y multiplos de cinco. Un numero puede caer en mas de un grupo.
                Muestra al final cuantos hay en cada grupo y cual fue el mayor y el menor.
                Usa al menos un ciclo for y un condicional compuesto.
            """),
            A("Agenda de contactos con diccionarios", AssignmentType.CODE, 5, """
                Modela una agenda con un diccionario donde la clave sea el nombre y el valor una
                tupla con telefono y correo.
                Implementa agregar, buscar, eliminar y listar ordenado alfabeticamente.
                Usa una comprension de listas al menos una vez y explica por que la usaste ahi.
            """),
            A("Refactorizacion en funciones y modulos", AssignmentType.CODE, 5, """
                Toma la agenda del modulo anterior y divide su logica en funciones con un solo
                proposito cada una. Separa esas funciones en un modulo agenda.py e importalas
                desde main.py.
                Entrega los dos archivos y justifica en tres lineas donde pusiste el limite
                entre lo que va en cada archivo.
            """),
            A("Persistencia en CSV y JSON con manejo de errores", AssignmentType.CODE, 6, """
                Amplia la agenda para que guarde los contactos en un archivo JSON y pueda
                exportarlos a CSV.
                El programa debe seguir funcionando si el archivo no existe, si esta vacio o si
                tiene JSON malformado: cada caso con su propio mensaje.
                Entrega el codigo y la lista de los errores que capturaste.
            """),
            A("Rediseno de la agenda con POO", AssignmentType.CODE, 7, """
                Reescribe la agenda usando clases: una clase Contacto y una clase Agenda que la
                administre. Aplica encapsulamiento con propiedades y crea una subclase
                ContactoEmpresarial que agregue cargo y empresa.
                Entrega el codigo y explica que gano el programa frente a la version con
                diccionarios.
            """),
            A("Proyecto final: sistema de gestion", AssignmentType.FILE, 10, """
                Entrega el sistema de gestion completo del curso: persistencia en archivos,
                validacion de entradas, organizacion en clases y un menu de consola.
                Sube el proyecto a un repositorio publico de GitHub y entrega el enlace.
                El repositorio debe incluir un README que explique como ejecutarlo.
            """)
        ),

        "HTML, CSS y JavaScript Avanzado" to listOf(
            A("Maqueta semantica y accesible", AssignmentType.CODE, 3, """
                Maqueta la pagina de inicio de un blog usando solo etiquetas semanticas:
                header, nav, main, article, aside y footer. Nada de div para estructura.
                Todas las imagenes llevan alt descriptivo y la jerarquia de titulos va de h1 a h3
                sin saltos. Entrega el HTML.
            """),
            A("Sistema de tarjetas con el modelo de caja", AssignmentType.CODE, 4, """
                Estiliza tres tarjetas de producto controlando margin, padding, border y
                box-sizing. Define los colores como variables CSS en :root.
                Ninguna medida puede estar escrita dos veces: si un valor se repite, va a una
                variable. Entrega el CSS y la captura del resultado.
            """),
            A("Barra de navegacion con Flexbox", AssignmentType.CODE, 4, """
                Construye una barra de navegacion con logo a la izquierda, enlaces al centro y
                un boton a la derecha, usando exclusivamente Flexbox.
                En pantallas menores a 600px los enlaces se apilan.
                Entrega el codigo y explica que hacen justify-content y align-items en tu solucion.
            """),
            A("Rejilla responsiva con CSS Grid", AssignmentType.CODE, 5, """
                Arma una galeria de nueve imagenes que muestre tres columnas en escritorio, dos
                en tablet y una en movil, usando Grid y un unico media query si te hace falta.
                Intenta resolverlo primero con auto-fit y minmax antes de escribir media queries.
                Entrega el codigo y di cual de las dos vias usaste y por que.
            """),
            A("Refactorizacion a ES6+", AssignmentType.CODE, 5, """
                Te damos un fragmento escrito con var, funciones anonimas y concatenacion de
                cadenas. Reescribelo con const y let, funciones flecha, template literals,
                desestructuracion y parametros por defecto.
                Entrega el antes, el despues y una lista de que cambiaste en cada linea.
            """),
            A("Lista de tareas manipulando el DOM", AssignmentType.CODE, 6, """
                Construye una lista de tareas que permita agregar, marcar como completada y
                eliminar, sin recargar la pagina y sin librerias.
                Usa delegacion de eventos: un solo listener en el contenedor, no uno por boton.
                Entrega el codigo y explica por que la delegacion es mejor aqui.
            """),
            A("Consumo de API con async/await", AssignmentType.CODE, 6, """
                Consume una API publica con fetch y async/await, y muestra los resultados en
                pantalla.
                Maneja los tres estados de forma visible: cargando, exito y error. Prueba el
                error apagando la red o usando una URL invalida.
                Entrega el codigo y la captura de los tres estados.
            """),
            A("Proyecto: pagina interactiva con API", AssignmentType.FILE, 10, """
                Entrega una pagina de una sola vista que consuma una API real, filtre resultados
                en el cliente y sea utilizable con teclado.
                Publicala en GitHub Pages y entrega el enlace junto al del repositorio.
            """)
        ),

        "Git y Control de Versiones" to listOf(
            A("Diagnostico de un flujo sin control de versiones", AssignmentType.TEXT, 2, """
                Describe una situacion real, tuya o de un equipo que conozcas, en la que se
                perdio trabajo o se piso codigo por no usar control de versiones.
                Explica en un parrafo que comando de Git habria evitado ese problema y por que.
            """),
            A("Primer repositorio con historial limpio", AssignmentType.CODE, 3, """
                Crea un repositorio local, agrega tres archivos y haz tres commits separados,
                uno por archivo, con mensajes en imperativo y menores a 50 caracteres.
                Entrega la salida de git log --oneline y el contenido de tu .gitignore.
            """),
            A("Investigacion del historial", AssignmentType.TEXT, 3, """
                Sobre el repositorio del ejercicio anterior, responde con el comando exacto que
                usaste en cada caso: que cambio introdujo el segundo commit, que lineas se
                modificaron entre el primero y el tercero, y como recuperarias un commit que
                borraste por error.
            """),
            A("Trabajo en ramas", AssignmentType.CODE, 4, """
                Crea una rama feature/login, haz dos commits en ella, vuelve a main y haz un
                commit distinto.
                Entrega la salida de git log --oneline --graph --all y explica en dos lineas
                que muestra el grafico.
            """),
            A("Resolucion de un conflicto real", AssignmentType.CODE, 5, """
                Provoca un conflicto a proposito: edita la misma linea del mismo archivo en dos
                ramas y fusionalas.
                Resuelvelo a mano y entrega el archivo final, el mensaje del merge y una
                explicacion de que significan los marcadores que Git dejo en el archivo.
            """),
            A("Repositorio remoto en GitHub", AssignmentType.FILE, 4, """
                Sube el repositorio a GitHub, configura el remoto origin y haz push de main y
                de una rama.
                Entrega el enlace al repositorio. Debe tener al menos cinco commits y un README.
            """),
            A("Pull Request con revision", AssignmentType.FILE, 5, """
                Abre un Pull Request desde una rama hacia main en tu propio repositorio.
                La descripcion debe decir que cambia, por que y como probarlo.
                Deja al menos un comentario de revision sobre una linea concreta antes de
                fusionarlo. Entrega el enlace al PR.
            """),
            A("Eleccion de flujo de trabajo", AssignmentType.TEXT, 5, """
                Compara GitFlow y trunk-based development para dos escenarios: un equipo de
                quince personas con releases mensuales y un equipo de dos que despliega a diario.
                Recomienda uno para cada caso y sustenta la decision con tres argumentos
                concretos, no con generalidades.
            """)
        ),

        "Desarrollo Backend con Spring Boot" to listOf(
            A("Inyeccion de dependencias en la practica", AssignmentType.CODE, 3, """
                Crea un servicio y un controlador donde el servicio se inyecte por constructor.
                Luego escribe, comentada, la version con inyeccion por campo y explica en tres
                lineas por que la version por constructor es la recomendada.
            """),
            A("API REST con los verbos correctos", AssignmentType.CODE, 5, """
                Construye un CRUD completo para una entidad a tu eleccion.
                Cada operacion debe responder con el codigo HTTP que le corresponde: 200, 201,
                204, 400 y 404 segun el caso.
                Entrega el controlador y una tabla que asocie cada endpoint con su codigo.
            """),
            A("Persistencia con Spring Data JPA", AssignmentType.CODE, 5, """
                Define una entidad con sus anotaciones, su repositorio y al menos dos consultas
                derivadas del nombre del metodo mas una con @Query.
                Explica que columnas genera Hibernate y por que declaraste nullable = false
                donde lo hiciste.
            """),
            A("Relaciones y DTOs", AssignmentType.CODE, 6, """
                Modela una relacion uno a muchos entre dos entidades y expon el resultado a
                traves de DTOs, nunca devolviendo la entidad directamente.
                Explica en tres lineas que problema concreto evita el DTO aqui.
            """),
            A("Validacion y manejo global de errores", AssignmentType.CODE, 6, """
                Agrega Bean Validation al DTO de entrada y un @RestControllerAdvice que devuelva
                el mapa campo a error.
                Prueba con una peticion invalida y entrega la respuesta JSON exacta que produjo
                tu manejador.
            """),
            A("Seguridad con JWT", AssignmentType.CODE, 8, """
                Protege la API con Spring Security y JWT. Define al menos una ruta publica y una
                que exija rol.
                Demuestra con tres peticiones: sin token debe dar 401, con token de rol
                incorrecto 403, y con el token correcto 200.
                Entrega la configuracion y las tres respuestas.
            """),
            A("Pruebas con JUnit 5, Mockito y MockMvc", AssignmentType.CODE, 7, """
                Escribe al menos seis pruebas: cuatro unitarias del servicio con Mockito y dos
                del controlador con MockMvc.
                Debe haber al menos una prueba por cada camino de error, no solo del camino feliz.
                Entrega el codigo y la salida de la ejecucion.
            """),
            A("Configuracion por perfiles y despliegue", AssignmentType.FILE, 7, """
                Separa la configuracion en perfiles dev y prod, y deja fuera del control de
                versiones cualquier credencial usando un archivo de ejemplo.
                Entrega el repositorio y explica que pasaria si esa credencial se hubiera
                subido alguna vez.
            """)
        ),

        "React 19 Avanzado" to listOf(
            A("Diagnostico de un componente mal escrito", AssignmentType.CODE, 3, """
                Te damos un componente que muta el estado directamente y pasa props innecesarias.
                Corrigelo y explica, linea por linea, que estaba mal y que consecuencia visible
                tenia cada error.
            """),
            A("useEffect sin fugas", AssignmentType.CODE, 5, """
                Escribe un componente que se suscriba a un evento o a un temporizador y lo
                limpie al desmontarse.
                Demuestra con la consola que la limpieza ocurre, y explica que pasa si se omite
                el arreglo de dependencias y que pasa si se deja vacio.
            """),
            A("Optimizacion medida, no supuesta", AssignmentType.CODE, 6, """
                Toma una lista que se re-renderiza de mas y optimizala con useMemo, useCallback
                o memo, segun corresponda.
                Mide antes y despues con el Profiler y entrega ambas capturas.
                Si alguna de las tres no mejoro nada, dilo: saber cuando no usarlas cuenta.
            """),
            A("useReducer y hook personalizado", AssignmentType.CODE, 6, """
                Reescribe un formulario con varios estados relacionados usando useReducer, y
                extrae la logica reutilizable a un hook propio.
                Explica por que useReducer encaja mejor que varios useState en este caso.
            """),
            A("Estado global con Context", AssignmentType.CODE, 6, """
                Implementa un contexto de sesion con usuario, login y logout, consumido desde al
                menos tres componentes en distinto nivel.
                Explica que problema concreto resolviste y por que no bastaba con pasar props.
            """),
            A("Rutas protegidas", AssignmentType.CODE, 6, """
                Configura React Router con rutas publicas, privadas y una ruta solo para
                administradores.
                Un usuario sin sesion que entre a una ruta privada debe ir al login y volver a
                donde queria despues de entrar. Entrega el codigo y describe ese flujo.
            """),
            A("Auditoria de rendimiento", AssignmentType.TEXT, 5, """
                Perfila tu aplicacion con React DevTools y reporta los tres componentes que mas
                tiempo consumen.
                Para cada uno: por que tarda, que cambiarias y cuanto esperas ganar.
            """),
            A("Proyecto full-stack con API REST", AssignmentType.FILE, 10, """
                Entrega una aplicacion React que consuma una API real con autenticacion,
                manejo de errores visible y estados de carga.
                Nada de datos simulados: si la API falla, la pantalla muestra el error.
                Entrega el repositorio y el enlace desplegado.
            """)
        ),

        "Base de Datos MySQL" to listOf(
            A("Modelo conceptual de un caso real", AssignmentType.FILE, 4, """
                Elige un negocio pequeno y real y modela su base de datos: entidades, atributos,
                relaciones y cardinalidades, en un diagrama entidad-relacion.
                Entrega el diagrama y la lista de supuestos que tuviste que asumir.
            """),
            A("Normalizacion hasta 3FN", AssignmentType.TEXT, 5, """
                Te damos una tabla con datos repetidos y dependencias parciales.
                Llevala a 1FN, 2FN y 3FN mostrando el estado en cada paso y explicando que
                anomalia de insercion, borrado o actualizacion elimina cada forma normal.
            """),
            A("Creacion del esquema con restricciones", AssignmentType.CODE, 5, """
                Escribe el DDL de tu modelo con tipos de datos adecuados, claves primarias y
                foraneas, NOT NULL, UNIQUE y CHECK donde corresponda.
                Justifica por que elegiste cada tipo: un VARCHAR(255) por defecto en todo no
                es una decision de diseno.
            """),
            A("Consultas con JOINs y subconsultas", AssignmentType.CODE, 6, """
                Resuelve cinco preguntas de negocio sobre tu esquema.
                Al menos una debe usar LEFT JOIN, una INNER JOIN y una subconsulta correlacionada.
                Entrega cada consulta con su resultado y la pregunta que responde en lenguaje
                natural.
            """),
            A("Agregaciones y funciones de ventana", AssignmentType.CODE, 6, """
                Calcula un ranking y un acumulado usando funciones de ventana.
                Luego resuelve el mismo ranking sin ellas y compara: cual se lee mejor y cual
                rinde mejor sobre tu volumen de datos.
            """),
            A("Optimizacion con EXPLAIN", AssignmentType.TEXT, 6, """
                Toma la consulta mas lenta que tengas, ejecuta EXPLAIN y reporta el plan.
                Crea el indice que creas necesario, vuelve a medir y entrega ambos planes con
                los tiempos.
                Si el indice no mejoro nada, explica por que.
            """),
            A("Transacciones y concurrencia", AssignmentType.CODE, 6, """
                Escribe una transaccion que mueva un saldo entre dos cuentas y no pueda dejar el
                sistema a medias.
                Provoca un fallo intencional en el medio y demuestra que el ROLLBACK dejo todo
                como estaba.
                Explica que nivel de aislamiento usaste y que problema evita.
            """),
            A("Respaldo, recuperacion y procedimientos", AssignmentType.FILE, 7, """
                Genera un respaldo con mysqldump, borra una tabla y restaurala desde el respaldo.
                Escribe ademas un procedimiento almacenado que resuelva una operacion repetitiva
                de tu esquema.
                Entrega el script completo y el registro de la recuperacion.
            """)
        )
    )
}
