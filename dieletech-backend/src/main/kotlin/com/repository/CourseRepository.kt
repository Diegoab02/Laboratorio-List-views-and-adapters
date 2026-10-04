package com.dieletech.backend.repository

import com.dieletech.backend.model.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<Course, Long> {
    fun findByActiveTrue(): List<Course>
    fun findByTechnology(technology: String): List<Course>
    fun findByLevel(level: String): List<Course>
    fun findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(title: String, description: String): List<Course>
}
