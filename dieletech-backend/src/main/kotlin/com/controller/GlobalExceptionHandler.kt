package com.dieletech.backend.controller

import com.dieletech.backend.error.ConflictException
import com.dieletech.backend.error.ForbiddenException
import com.dieletech.backend.error.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Errores de @Valid: devuelve el primer mensaje legible y el mapa
     * completo campo -> error, para que el formulario pueda resaltar
     * exactamente el campo que fallo.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val fieldErrors = e.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Valor invalido")
        }
        val firstMessage = fieldErrors.values.firstOrNull() ?: "Datos invalidos"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "message" to firstMessage,
                "errors" to fieldErrors
            )
        )
    }

    /** 404: el recurso no existe. */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<Map<String, Any>> =
        body(HttpStatus.NOT_FOUND, e.message ?: "El recurso no existe")

    /** 403: la cuenta no puede operar sobre este recurso. */
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(e: ForbiddenException): ResponseEntity<Map<String, Any>> =
        body(HttpStatus.FORBIDDEN, e.message ?: "Tu cuenta no tiene permiso para esta accion")

    /** 409: el estado del recurso impide la operacion. */
    @ExceptionHandler(ConflictException::class)
    fun handleConflict(e: ConflictException): ResponseEntity<Map<String, Any>> =
        body(HttpStatus.CONFLICT, e.message ?: "La operacion entra en conflicto con el estado actual")

    /** Cualquier otra regla de negocio sigue respondiendo 400. */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntime(e: RuntimeException): ResponseEntity<Map<String, Any>> =
        body(HttpStatus.BAD_REQUEST, e.message ?: "Error en la solicitud")

    private fun body(status: HttpStatus, message: String): ResponseEntity<Map<String, Any>> =
        ResponseEntity.status(status).body(
            mapOf("status" to status.value(), "message" to message)
        )
}
