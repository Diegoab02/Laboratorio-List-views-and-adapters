package com.dieletech.backend.dto

/** Certificado del estudiante. */
data class CertificateDTO(
    val code: String,
    val courseId: Long,
    val courseTitle: String,
    val courseHours: Int,
    val studentName: String,
    val instructorName: String?,
    val score: Int,
    val issuedAt: String,
    val verifyUrl: String
)

/**
 * Respuesta publica de verificacion. No expone el correo del estudiante:
 * cualquiera con el codigo puede confirmar el certificado sin obtener
 * datos de contacto.
 */
data class CertificateVerificationDTO(
    val valid: Boolean,
    val message: String,
    val code: String,
    val studentName: String? = null,
    val courseTitle: String? = null,
    val courseHours: Int? = null,
    val instructorName: String? = null,
    val score: Int? = null,
    val issuedAt: String? = null
)
