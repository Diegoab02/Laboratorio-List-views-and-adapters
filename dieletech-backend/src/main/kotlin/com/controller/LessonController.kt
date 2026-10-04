package com.controller

import com.dieletech.backend.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import service.LessonService

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["*"])
class LessonController(private val lessonService: LessonService) {

    /**
     * HU-07. El parametro email permite desbloquear el contenido de quien
     * compro el curso. Sin email, solo se ven las lecciones marcadas como
     * preview gratuito.
     */
    @GetMapping("/courses/{courseId}/lessons")
    fun getLessons(
        @PathVariable courseId: Long,
        @RequestParam(required = false) email: String?
    ): ResponseEntity<List<LessonDTO>> =
        ResponseEntity.ok(lessonService.getLessonsByCourse(courseId, email))

    @PostMapping("/lessons/{lessonId}/progress")
    fun updateProgress(
        @PathVariable lessonId: Long,
        @RequestBody dto: LessonProgressUpdateDTO
    ): ResponseEntity<Any> = try {
        ResponseEntity.ok(lessonService.updateProgress(lessonId, dto))
    } catch (e: Exception) {
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("message" to (e.message ?: "No se pudo registrar el progreso")))
    }

    @GetMapping("/courses/{courseId}/progress")
    fun getCourseProgress(
        @PathVariable courseId: Long,
        @RequestParam email: String
    ): ResponseEntity<CourseProgressDTO> =
        ResponseEntity.ok(lessonService.getCourseProgress(courseId, email))
}
