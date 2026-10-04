package com.dieletech.backend.model

/**
 * Forma en que el estudiante entrega el ejercicio practico (HU-37).
 * El tipo no cambia la validacion de fondo, solo la interfaz de entrega:
 * el servidor exige contenido no vacio en los tres casos.
 */
enum class AssignmentType {
    /** Fragmento de codigo escrito en el editor. */
    CODE,

    /** Enlace a un archivo o repositorio. */
    FILE,

    /** Respuesta redactada. */
    TEXT
}
