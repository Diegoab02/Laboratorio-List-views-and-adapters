package com.controller

import com.dieletech.backend.dto.CourseDTO
import com.dieletech.backend.dto.CourseSummaryDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import service.CourseService

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = ["*"])
class CourseController(private val courseService: CourseService) {

    @GetMapping
    fun getAllCourses(): ResponseEntity<List<CourseSummaryDTO>> {
        val courses = courseService.getAllCourses()
        return ResponseEntity.ok(courses)
    }

    @GetMapping("/{id}")
    fun getCourseById(@PathVariable id: Long): ResponseEntity<CourseDTO> {
        val course = courseService.getCourseById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(course)
    }

    @GetMapping("/technology/{technology}")
    fun getCoursesByTechnology(@PathVariable technology: String): ResponseEntity<List<CourseSummaryDTO>> {
        val courses = courseService.getCoursesByTechnology(technology)
        return ResponseEntity.ok(courses)
    }

    @GetMapping("/level/{level}")
    fun getCoursesByLevel(@PathVariable level: String): ResponseEntity<List<CourseSummaryDTO>> {
        val courses = courseService.getCoursesByLevel(level)
        return ResponseEntity.ok(courses)
    }

    @GetMapping("/search")
    fun searchCourses(@RequestParam q: String): ResponseEntity<List<CourseSummaryDTO>> {
        val courses = courseService.searchCourses(q)
        return ResponseEntity.ok(courses)
    }
}
