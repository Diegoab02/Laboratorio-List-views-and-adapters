package com.dieletech.backend.repository

import com.dieletech.backend.model.Lesson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LessonRepository : JpaRepository<Lesson, Long> {
    fun findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId: Long): List<Lesson>
    fun countByCourseIdAndActiveTrue(courseId: Long): Int
}
