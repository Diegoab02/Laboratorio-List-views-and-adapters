package com.controller

import com.dieletech.backend.dto.SubmitQuizDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import service.QuizService

/** HU-10: evaluacion final del curso. */
@RestController
@RequestMapping("/api/courses/{courseId}/quiz")
@CrossOrigin(origins = ["*"])
class QuizController(private val quizService: QuizService) {

    @GetMapping
    fun getQuiz(
        @PathVariable courseId: Long,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Inicia sesion para ver la evaluacion")
        return try {
            ResponseEntity.ok(quizService.getQuiz(courseId, email))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
        }
    }

    @PostMapping("/attempts")
    fun submit(
        @PathVariable courseId: Long,
        @Valid @RequestBody dto: SubmitQuizDTO,
        authentication: Authentication?
    ): ResponseEntity<Any> {
        val email = authentication?.name
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Inicia sesion para presentar la evaluacion")
        return try {
            ResponseEntity.ok(quizService.submit(courseId, email, dto))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message ?: "No se pudo completar la operacion")
        }
    }
}
