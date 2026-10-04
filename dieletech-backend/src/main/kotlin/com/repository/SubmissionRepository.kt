package com.dieletech.backend.repository

import com.dieletech.backend.model.Submission
import com.dieletech.backend.model.SubmissionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SubmissionRepository : JpaRepository<Submission, Long> {

    /**
     * El correo se compara normalizado por el mismo motivo que en
     * PurchaseRepository: una diferencia de mayusculas dejaba al
     * estudiante sin ver lo suyo.
     */
    @Query(
        "SELECT s FROM Submission s WHERE s.assignmentId = :assignmentId " +
        "AND LOWER(TRIM(s.userEmail)) = LOWER(TRIM(:email))"
    )
    fun findMine(
        @Param("assignmentId") assignmentId: Long,
        @Param("email") email: String
    ): Submission?

    @Query(
        "SELECT s FROM Submission s WHERE s.courseId = :courseId " +
        "AND LOWER(TRIM(s.userEmail)) = LOWER(TRIM(:email))"
    )
    fun findMineByCourse(
        @Param("courseId") courseId: Long,
        @Param("email") email: String
    ): List<Submission>

    @Query(
        "SELECT s FROM Submission s WHERE LOWER(TRIM(s.userEmail)) = LOWER(TRIM(:email))"
    )
    fun findAllMine(@Param("email") email: String): List<Submission>

    fun findByCourseIdAndStatusOrderBySubmittedAtAsc(
        courseId: Long,
        status: SubmissionStatus
    ): List<Submission>

    fun findByCourseIdOrderBySubmittedAtDesc(courseId: Long): List<Submission>

    fun countByAssignmentIdAndStatus(assignmentId: Long, status: SubmissionStatus): Int
}
