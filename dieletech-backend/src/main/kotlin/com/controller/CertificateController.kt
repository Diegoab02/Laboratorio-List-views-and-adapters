package com.controller

import com.dieletech.backend.dto.CertificateVerificationDTO
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import service.CertificateService

/** HU-11: certificados del estudiante y verificacion publica. */
@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = ["*"])
class CertificateController(private val certificateService: CertificateService) {

    /** Certificados del usuario autenticado. */
    @GetMapping("/mine")
    fun mine(authentication: Authentication?): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesion no valida")
        return ResponseEntity.ok(certificateService.listMine(email))
    }

    /** Descarga del PDF. Solo el titular del certificado. */
    @GetMapping("/{code}/pdf")
    fun download(
        @PathVariable code: String,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesion no valida")
        return try {
            val cert = certificateService.getOwned(code, email)
            val pdf = certificateService.renderPdf(cert)
            val filename = "Certificado-Dieletech-${cert.code}.pdf"
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
        }
    }

    /**
     * Verificacion publica: sin sesion, por codigo. No devuelve el correo
     * del estudiante, solo lo necesario para confirmar el certificado.
     */
    @GetMapping("/verify/{code}")
    fun verify(@PathVariable code: String): ResponseEntity<CertificateVerificationDTO> =
        ResponseEntity.ok(certificateService.verify(code))
}
