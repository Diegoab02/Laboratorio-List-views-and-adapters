package com.dieletech.backend.repository

import com.dieletech.backend.model.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository : JpaRepository<Question, Long> {
    fun findByCourseIdAndActiveTrueOrderByOrderIndexAsc(courseId: Long): List<Question>
    fun countByCourseIdAndActiveTrue(courseId: Long): Int
}
