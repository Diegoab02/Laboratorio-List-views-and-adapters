package com.controller

import com.dieletech.backend.dto.EnrolledStudentDTO
import com.dieletech.backend.dto.PurchaseRequestDTO
import com.dieletech.backend.dto.PurchaseResponseDTO
import com.dieletech.backend.model.Purchase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import service.PurchaseService

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = ["*"])
class PurchaseController(private val purchaseService: PurchaseService) {

    /**
     * @Valid activa las restricciones del DTO: sin nombre, documento,
     * telefono, metodo de pago o aceptacion de terminos la peticion se
     * rechaza con 400 y el mensaje exacto del campo que falta.
     */
    @PostMapping
    fun createPurchase(@Valid @RequestBody request: PurchaseRequestDTO): ResponseEntity<PurchaseResponseDTO> {
        return try {
            val response = purchaseService.processPurchase(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                PurchaseResponseDTO(
                    success = false,
                    orderId = "",
                    courseId = request.courseId,
                    courseName = "",
                    amount = request.amount,
                    message = e.message ?: "No se pudo procesar la compra"
                )
            )
        }
    }

    @GetMapping("/user/{email}")
    fun getPurchasesByEmail(@PathVariable email: String): ResponseEntity<List<Purchase>> =
        ResponseEntity.ok(purchaseService.getPurchasesByEmail(email))

    @GetMapping("/check")
    fun checkPurchase(
        @RequestParam courseId: Long,
        @RequestParam email: String
    ): ResponseEntity<Map<String, Boolean>> =
        ResponseEntity.ok(mapOf("hasPurchased" to purchaseService.hasUserPurchasedCourse(courseId, email)))

    /** Matriculados de un curso con progreso real (panel instructor). */
    @GetMapping("/course/{courseId}/students")
    fun getEnrolledStudents(@PathVariable courseId: Long): ResponseEntity<List<EnrolledStudentDTO>> =
        ResponseEntity.ok(purchaseService.getEnrolledStudents(courseId))
}
