package com.dieletech.backend

import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * Rellena las columnas que se agregaron a tablas que ya tenian filas.
 *
 * Con spring.jpa.hibernate.ddl-auto=update, Hibernate crea la columna nueva
 * pero deja NULL en los registros existentes. Los valores por defecto de
 * Kotlin (var notifyEmail: Boolean = true) solo aplican al construir el
 * objeto en memoria: no son un DEFAULT de la base de datos. Al leer esa
 * fila, Hibernate intenta asignar NULL a un tipo primitivo y falla con
 * "Null value was assigned to a property of primitive type".
 *
 * Corre antes que cualquier otro inicializador y es idempotente: en una
 * base ya sana no actualiza ninguna fila.
 */
@Component
@Order(0)
class SchemaBackfill(private val jdbc: JdbcTemplate) : CommandLineRunner {

    private data class Fix(val table: String, val column: String, val value: String)

    override fun run(vararg args: String?) {
        val fixes = listOf(
            // Perfil de usuario (HU-14)
            Fix("users", "notify_email", "TRUE"),
            Fix("users", "notify_new_courses", "TRUE"),
            Fix("users", "notify_progress", "TRUE"),
            Fix("users", "public_profile", "FALSE"),
            Fix("users", "theme_preference", "'system'"),
            Fix("users", "accent_color", "'#2563eb'"),
            Fix("users", "language_preference", "'es'"),
            Fix("users", "updated_at", "CURRENT_TIMESTAMP"),

            // Cupos y contadores de curso (HU-21)
            Fix("courses", "capacity", "50"),
            Fix("courses", "student_count", "0"),

            // Leccion de muestra gratuita (HU-23)
            Fix("lessons", "free_preview", "FALSE"),

            // Datos de facturacion del checkout (HU-06)
            Fix("purchases", "document_id", "''"),
            Fix("purchases", "phone", "''")
        )

        var repaired = 0
        for (f in fixes) {
            try {
                val n = jdbc.update(
                    "UPDATE ${f.table} SET ${f.column} = ${f.value} WHERE ${f.column} IS NULL"
                )
                if (n > 0) {
                    println("Backfill: ${f.table}.${f.column} -> $n fila(s)")
                    repaired += n
                }
            } catch (e: Exception) {
                // La tabla o la columna aun no existe (base nueva): no hay nada que reparar.
            }
        }

        if (repaired > 0) {
            println("Backfill de esquema: $repaired valor(es) nulo(s) corregido(s)")
        }
    }
}
