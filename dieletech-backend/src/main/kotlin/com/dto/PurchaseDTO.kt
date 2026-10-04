package com.dieletech.backend.dto

import jakarta.validation.constraints.*

/**
 * Checkout con validacion estricta: si falta cualquier dato obligatorio
 * la compra se rechaza con 400 antes de tocar la base de datos.
 */
data class PurchaseRequestDTO(
    @field:NotNull(message = "El curso es obligatorio")
    @field:Positive(message = "Identificador de curso invalido")
    val courseId: Long,

    @field:NotBlank(message = "El nombre completo es obligatorio")
    @field:Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 100 caracteres")
    @field:Pattern(
        regexp = "^[\\p{L} .'-]+$",
        message = "El nombre solo puede contener letras y espacios"
    )
    val fullName: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato valido")
    val email: String,

    @field:NotBlank(message = "El documento de identidad es obligatorio")
    @field:Pattern(
        regexp = "^[0-9]{6,15}$",
        message = "El documento debe tener entre 6 y 15 digitos"
    )
    val documentId: String,

    @field:NotBlank(message = "El telefono es obligatorio")
    @field:Pattern(
        regexp = "^[0-9+ ()-]{7,20}$",
        message = "El telefono no tiene un formato valido"
    )
    val phone: String,

    @field:NotBlank(message = "Selecciona un metodo de pago")
    @field:Pattern(
        regexp = "^(card|paypal|pse)$",
        message = "Metodo de pago no soportado"
    )
    val paymentMethod: String,

    @field:NotNull(message = "El monto es obligatorio")
    @field:Positive(message = "El monto debe ser mayor a cero")
    val amount: Double,

    @field:AssertTrue(message = "Debes aceptar los terminos y condiciones para continuar")
    val acceptTerms: Boolean = false
)

data class PurchaseResponseDTO(
    val success: Boolean,
    val orderId: String,
    val courseId: Long,
    val courseName: String,
    val amount: Double,
    val message: String
)

/** Fila de la lista de estudiantes del panel del instructor. */
data class EnrolledStudentDTO(
    val fullName: String,
    val email: String,
    val orderId: String,
    val purchasedAt: String,
    val amount: Double,
    val progressPercent: Int,
    val completedLessons: Int,
    val totalLessons: Int
)
