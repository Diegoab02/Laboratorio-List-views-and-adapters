package com.dieletech.backend.repository

import com.dieletech.backend.model.Assignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AssignmentRepository : JpaRepository<Assignment, Long> {

    fun findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId: Long): List<Assignment>

    fun countByCourseIdAndActiveTrue(courseId: Long): Int

    /** Un modulo tiene cero o una tarea: se comprueba antes de crear. */
    fun findByCourseIdAndLessonIdAndActiveTrue(courseId: Long, lessonId: Long): Assignment?

    fun findByIdAndActiveTrue(id: Long): Assignment?
}
