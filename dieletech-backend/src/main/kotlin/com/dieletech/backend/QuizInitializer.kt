package com.dieletech.backend

import com.dieletech.backend.model.Question
import com.dieletech.backend.repository.CourseRepository
import com.dieletech.backend.repository.QuestionRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Siembra la evaluacion final de cada curso semilla (HU-10).
 * Corre despues de DataInitializer y solo crea las preguntas que faltan,
 * identificandolas por su enunciado, de modo que reiniciar el backend no
 * duplica la evaluacion ni pisa las preguntas que edite el instructor.
 */
@Component
@Order(2)
class QuizInitializer(
    private val courseRepository: CourseRepository,
    private val questionRepository: QuestionRepository
) : CommandLineRunner {

    private class Q(
        val text: String,
        val options: List<String>,
        val correct: Int,
        val why: String
    )

    @Transactional
    override fun run(vararg args: String?) {
        val banks = questionBank()
        var created = 0

        for (course in courseRepository.findAll()) {
            val bank = banks[course.title] ?: continue
            val existing = questionRepository
                .findByCourseIdAndActiveTrueOrderByOrderIndexAsc(course.id)
                .map { it.text }
                .toSet()

            val missing = bank.filter { it.text !in existing }
            if (missing.isEmpty()) continue

            var index = existing.size
            questionRepository.saveAll(
                missing.map { q ->
                    index++
                    Question(
                        courseId = course.id,
                        text = q.text,
                        options = q.options.joinToString("|"),
                        correctIndex = q.correct,
                        explanation = q.why,
                        orderIndex = index
                    )
                }
            )
            created += missing.size
        }

        if (created > 0) println("Evaluaciones Dieletech: $created preguntas cargadas")
    }

    private fun questionBank(): Map<String, List<Q>> = mapOf(

        "Fundamentos de Python" to listOf(
            Q("Que imprime el siguiente codigo: x = [1, 2, 3]; print(x[-1])",
              listOf("1", "3", "Error de indice", "[1, 2, 3]"), 1,
              "Los indices negativos cuentan desde el final, por lo que -1 es el ultimo elemento."),
            Q("Cual es la diferencia principal entre una lista y una tupla en Python?",
              listOf("La tupla solo admite numeros",
                     "La lista es mutable y la tupla es inmutable",
                     "La tupla no puede recorrerse con un ciclo",
                     "No existe diferencia real"), 1,
              "Una vez creada, la tupla no puede modificarse; la lista si."),
            Q("Que estructura usarias para asociar un nombre con un valor y buscarlo rapido?",
              listOf("Una lista", "Una tupla", "Un diccionario", "Un conjunto"), 2,
              "El diccionario almacena pares clave-valor con busqueda promedio en tiempo constante."),
            Q("Para que sirve el bloque try / except?",
              listOf("Para repetir un bloque de codigo",
                     "Para capturar y manejar errores en tiempo de ejecucion",
                     "Para declarar funciones",
                     "Para importar modulos"), 1,
              "Permite que el programa reaccione a un error en lugar de detenerse."),
            Q("En Programacion Orientada a Objetos, que representa una clase?",
              listOf("Una instancia concreta con datos",
                     "Una plantilla que define atributos y comportamiento",
                     "Un archivo de configuracion",
                     "Un tipo de ciclo"), 1,
              "La clase es el molde; el objeto es la instancia creada a partir de ella."),
            Q("Que devuelve len('Dieletech')?",
              listOf("8", "9", "10", "Error"), 1,
              "La cadena tiene nueve caracteres."),
            Q("Cual es la forma correcta de abrir un archivo garantizando que se cierre?",
              listOf("open('a.txt')",
                     "with open('a.txt') as f:",
                     "file = 'a.txt'",
                     "read('a.txt')"), 1,
              "El bloque with cierra el archivo aunque ocurra una excepcion."),
            Q("Que hace una comprension de listas como [x*2 for x in range(3)]?",
              listOf("Genera [0, 2, 4]",
                     "Genera [2, 4, 6]",
                     "Genera [0, 1, 2]",
                     "Produce un error de sintaxis"), 0,
              "range(3) produce 0, 1 y 2, y cada valor se multiplica por dos.")
        ),

        "HTML, CSS y JavaScript Avanzado" to listOf(
            Q("Cual es la ventaja principal del HTML semantico?",
              listOf("Reduce el tamano del archivo",
                     "Mejora la accesibilidad y la indexacion por buscadores",
                     "Acelera JavaScript",
                     "Evita usar CSS"), 1,
              "Las etiquetas con significado permiten a lectores de pantalla y buscadores entender la estructura."),
            Q("Que propiedad de CSS Grid define las columnas de la cuadricula?",
              listOf("grid-columns", "grid-template-columns", "column-count", "flex-direction"), 1,
              "grid-template-columns declara el numero y el tamano de las columnas."),
            Q("En Flexbox, que hace justify-content: space-between?",
              listOf("Centra los elementos",
                     "Distribuye el espacio libre entre los elementos, sin margen en los extremos",
                     "Apila los elementos verticalmente",
                     "Oculta el desbordamiento"), 1,
              "El primer y el ultimo elemento quedan pegados a los bordes y el espacio se reparte entre el resto."),
            Q("Cual es la diferencia entre let y var?",
              listOf("No hay diferencia",
                     "let tiene alcance de bloque y var alcance de funcion",
                     "var es mas moderno",
                     "let solo sirve para numeros"), 1,
              "let respeta el bloque donde se declara; var se eleva al ambito de la funcion."),
            Q("Que devuelve una funcion declarada como async?",
              listOf("Un valor primitivo", "Una Promise", "Un callback", "undefined siempre"), 1,
              "Toda funcion async envuelve su retorno en una Promise."),
            Q("Cual es la forma recomendada de manejar el clic de muchos elementos iguales?",
              listOf("Un listener por elemento",
                     "Delegacion de eventos en el contenedor padre",
                     "Atributo onclick en el HTML",
                     "Un setInterval que revise el DOM"), 1,
              "La delegacion usa un solo listener y funciona tambien con elementos anadidos despues."),
            Q("Que hace fetch() cuando el servidor responde con un 404?",
              listOf("Lanza una excepcion",
                     "Resuelve la promesa con response.ok en false",
                     "Devuelve null",
                     "Reintenta automaticamente"), 1,
              "fetch solo rechaza ante un fallo de red: un 404 es una respuesta valida que hay que comprobar."),
            Q("Para que sirve el atributo alt de una imagen?",
              listOf("Define el tamano",
                     "Describe la imagen para lectores de pantalla y cuando no carga",
                     "Aplica un filtro",
                     "Cambia el formato"), 1,
              "Es el texto alternativo: base de la accesibilidad en imagenes.")
        ),

        "Git y Control de Versiones" to listOf(
            Q("Que hace git add?",
              listOf("Guarda los cambios en el historial",
                     "Mueve los cambios al area de preparacion",
                     "Los envia al repositorio remoto",
                     "Crea una rama nueva"), 1,
              "add pasa los cambios al staging; commit es el que los guarda en el historial."),
            Q("Que es una rama en Git?",
              listOf("Una copia completa del proyecto",
                     "Un puntero movil a un commit",
                     "Una carpeta oculta",
                     "Un respaldo automatico"), 1,
              "Por eso crear y borrar ramas es tan barato: solo se mueve un puntero."),
            Q("Cual es la diferencia entre merge y rebase?",
              listOf("Son sinonimos",
                     "merge crea un commit de union; rebase reescribe los commits sobre otra base",
                     "rebase solo funciona en remoto",
                     "merge borra el historial"), 1,
              "merge conserva la bifurcacion; rebase produce un historial lineal reescribiendo commits."),
            Q("Que comando recupera el historial de referencias para rescatar trabajo perdido?",
              listOf("git log", "git reflog", "git status", "git diff"), 1,
              "reflog registra todos los movimientos de HEAD, incluso los que log ya no muestra."),
            Q("Que ocurre al hacer git push a una rama protegida sin Pull Request?",
              listOf("Se aplica sin restriccion",
                     "El servidor lo rechaza segun la regla de proteccion",
                     "Se crea una rama nueva",
                     "Se borra la rama"), 1,
              "Las reglas de proteccion obligan a pasar por revision antes de integrar."),
            Q("Para que sirve un archivo .gitignore?",
              listOf("Para cifrar archivos",
                     "Para indicar que archivos no deben versionarse",
                     "Para listar colaboradores",
                     "Para documentar el proyecto"), 1,
              "Evita subir dependencias, artefactos de compilacion y credenciales."),
            Q("Que hace git clone?",
              listOf("Copia una rama local",
                     "Descarga el repositorio completo con su historial",
                     "Sincroniza solo el ultimo commit",
                     "Crea un repositorio vacio"), 1,
              "Git es distribuido: cada clon trae el historial completo."),
            Q("Cual es la practica recomendada para un mensaje de commit?",
              listOf("Describir en modo imperativo que hace el cambio",
                     "Escribir solo la fecha",
                     "Poner siempre 'cambios'",
                     "Dejarlo vacio"), 0,
              "Un mensaje claro en imperativo explica el porque del cambio a quien lo lea despues.")
        ),

        "Desarrollo Backend con Spring Boot" to listOf(
            Q("Que hace la anotacion @RestController?",
              listOf("Declara una entidad de base de datos",
                     "Marca una clase como controlador REST que serializa el retorno a JSON",
                     "Configura la seguridad",
                     "Crea un repositorio"), 1,
              "Combina @Controller y @ResponseBody."),
            Q("Cual es el proposito de un DTO?",
              listOf("Reemplazar el repositorio",
                     "Definir el contrato de datos sin exponer la entidad de persistencia",
                     "Acelerar las consultas",
                     "Guardar configuracion"), 1,
              "Exponer entidades directamente filtra el modelo interno y provoca cargas perezosas inesperadas."),
            Q("Que codigo HTTP corresponde a la creacion exitosa de un recurso?",
              listOf("200", "201", "204", "304"), 1,
              "201 Created indica que el recurso fue creado."),
            Q("Para que sirve @Transactional en un metodo de servicio?",
              listOf("Para cachear el resultado",
                     "Para que todas las operaciones se confirmen o se reviertan juntas",
                     "Para ejecutarlo en segundo plano",
                     "Para validar los parametros"), 1,
              "Si algo falla a mitad de camino, no queda un estado parcial en la base de datos."),
            Q("Que hace JwtFilter en la cadena de Spring Security?",
              listOf("Cifra la base de datos",
                     "Lee el token de la cabecera y establece la autenticacion del contexto",
                     "Genera la contrasena",
                     "Envia correos"), 1,
              "Se ejecuta antes del filtro de usuario y contrasena, y resuelve la identidad del solicitante."),
            Q("Que anotacion activa las restricciones del DTO en el controlador?",
              listOf("@Validated en la clase unicamente",
                     "@Valid sobre el parametro del cuerpo",
                     "@Check",
                     "No hace falta ninguna"), 1,
              "Sin @Valid las restricciones del DTO se declaran pero nunca se evaluan."),
            Q("Cual es la ventaja de la autenticacion sin estado con JWT?",
              listOf("Es imposible de revocar",
                     "El servidor no guarda sesion, lo que facilita escalar horizontalmente",
                     "No necesita HTTPS",
                     "Evita validar las credenciales"), 1,
              "Al no haber sesion en memoria, cualquier instancia puede atender cualquier peticion."),
            Q("Que hace @RestControllerAdvice?",
              listOf("Define rutas nuevas",
                     "Centraliza el manejo de excepciones de todos los controladores",
                     "Configura la base de datos",
                     "Documenta la API"), 1,
              "Evita repetir bloques try/catch en cada endpoint.")
        ),

        "React 19 Avanzado" to listOf(
            Q("Cuando se ejecuta el efecto de useEffect con un array de dependencias vacio?",
              listOf("En cada renderizado",
                     "Solo una vez despues del primer montaje",
                     "Nunca",
                     "Solo al desmontar"), 1,
              "Sin dependencias que cambien, el efecto no se vuelve a ejecutar."),
            Q("Para que sirve la funcion de limpieza que retorna useEffect?",
              listOf("Para reiniciar el estado",
                     "Para cancelar suscripciones y temporizadores antes de volver a ejecutar o al desmontar",
                     "Para acelerar el renderizado",
                     "Para validar props"), 1,
              "Sin limpieza quedan suscripciones vivas que provocan fugas de memoria."),
            Q("Cuando conviene useMemo?",
              listOf("En cualquier variable",
                     "Cuando un calculo es costoso y sus entradas cambian poco",
                     "Para reemplazar useState",
                     "Para hacer peticiones HTTP"), 1,
              "Memorizar de mas anade complejidad sin ganancia medible."),
            Q("Que problema resuelve Context API?",
              listOf("El enrutado",
                     "Pasar datos a componentes profundos sin encadenar props",
                     "La validacion de formularios",
                     "El manejo de errores"), 1,
              "Evita el prop drilling a traves de niveles que no usan el dato."),
            Q("Por que React ejecuta los efectos dos veces en modo estricto durante el desarrollo?",
              listOf("Es un error del framework",
                     "Para detectar efectos sin limpieza correcta",
                     "Para acelerar el renderizado",
                     "Porque hay dos raices"), 1,
              "Es una comprobacion intencional que solo ocurre en desarrollo."),
            Q("Que hace la prop key en una lista?",
              listOf("Define el orden visual",
                     "Permite a React identificar cada elemento entre renderizados",
                     "Aplica estilos",
                     "Es opcional y decorativa"), 1,
              "Sin una key estable React reconstruye elementos y pierde su estado."),
            Q("Cuando conviene useReducer sobre useState?",
              listOf("Siempre",
                     "Cuando el estado es complejo y sus transiciones estan relacionadas",
                     "Solo con formularios",
                     "Nunca en React 19"), 1,
              "Concentra las transiciones en un solo lugar y las hace verificables."),
            Q("Que es una ruta protegida en React Router?",
              listOf("Una ruta cifrada",
                     "Un componente que verifica la sesion antes de renderizar el destino",
                     "Una ruta sin parametros",
                     "Una ruta del servidor"), 1,
              "Si no hay sesion valida, redirige al login en lugar de mostrar el contenido.")
        ),

        "Base de Datos MySQL" to listOf(
            Q("Que problema resuelve la tercera forma normal?",
              listOf("La lentitud de las consultas",
                     "Las dependencias transitivas entre atributos no clave",
                     "La falta de indices",
                     "El tamano del disco"), 1,
              "En 3FN ningun atributo no clave depende de otro atributo no clave."),
            Q("Que devuelve un LEFT JOIN?",
              listOf("Solo las filas coincidentes",
                     "Todas las filas de la tabla izquierda y las coincidencias de la derecha",
                     "Solo las filas sin coincidencia",
                     "El producto cartesiano"), 1,
              "Las filas izquierdas sin pareja aparecen con NULL en las columnas de la derecha."),
            Q("Para que sirve EXPLAIN?",
              listOf("Para documentar la consulta",
                     "Para ver el plan de ejecucion que elige el motor",
                     "Para crear un indice",
                     "Para exportar datos"), 1,
              "Muestra si se usa un indice o se recorre la tabla completa."),
            Q("Que garantiza la propiedad de atomicidad en ACID?",
              listOf("Que la transaccion se ejecute rapido",
                     "Que la transaccion se aplique completa o no se aplique en absoluto",
                     "Que los datos esten cifrados",
                     "Que no haya duplicados"), 1,
              "No existe un estado intermedio visible para otras transacciones."),
            Q("Cuando un indice puede perjudicar el rendimiento?",
              listOf("Nunca",
                     "En tablas con muchas escrituras, porque cada insercion debe actualizarlo",
                     "En consultas de solo lectura",
                     "Cuando la tabla es grande"), 1,
              "Los indices aceleran las lecturas a costa de encarecer escrituras y ocupar espacio."),
            Q("Cual es la diferencia entre WHERE y HAVING?",
              listOf("Son equivalentes",
                     "WHERE filtra filas antes de agrupar y HAVING filtra grupos despues",
                     "HAVING solo funciona con JOIN",
                     "WHERE solo sirve con numeros"), 1,
              "HAVING se aplica al resultado de GROUP BY."),
            Q("Que hace una clave foranea?",
              listOf("Acelera las busquedas",
                     "Garantiza que el valor exista en la tabla referenciada",
                     "Cifra la columna",
                     "Genera valores automaticos"), 1,
              "Es el mecanismo de integridad referencial del modelo relacional."),
            Q("Que hace ROLLBACK?",
              listOf("Confirma los cambios",
                     "Deshace los cambios de la transaccion en curso",
                     "Crea un respaldo",
                     "Bloquea la tabla"), 1,
              "Revierte la base al estado previo al inicio de la transaccion.")
        )
    )
}
