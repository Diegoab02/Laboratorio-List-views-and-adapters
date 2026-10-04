package com.dieletech.backend.repository

import com.dieletech.backend.model.QuizAttempt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface QuizAttemptRepository : JpaRepository<QuizAttempt, Long> {

    @Query(
        "SELECT a FROM QuizAttempt a " +
        "WHERE LOWER(TRIM(a.userEmail)) = LOWER(TRIM(:email)) AND a.courseId = :courseId " +
        "ORDER BY a.attemptedAt DESC"
    )
    fun findAttempts(
        @Param("email") email: String,
        @Param("courseId") courseId: Long
    ): List<QuizAttempt>

    @Query(
        "SELECT COUNT(a) > 0 FROM QuizAttempt a " +
        "WHERE LOWER(TRIM(a.userEmail)) = LOWER(TRIM(:email)) " +
        "AND a.courseId = :courseId AND a.passed = true"
    )
    fun hasPassed(
        @Param("email") email: String,
        @Param("courseId") courseId: Long
    ): Boolean

    fun countByCourseId(courseId: Long): Long
}
