package com.dieletech.backend.model

/**
 * Ciclo de vida de una entrega (HU-38).
 * DRAFT se puede sobrescribir cuantas veces haga falta.
 * SUBMITTED todavia admite reenvio.
 * GRADED la cierra: a partir de ahi el estudiante no puede cambiarla,
 * porque la nota ya quedo escrita contra ese contenido.
 */
enum class SubmissionStatus {
    DRAFT,
    SUBMITTED,
    GRADED
}
