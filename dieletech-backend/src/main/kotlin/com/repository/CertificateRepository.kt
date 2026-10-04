package com.dieletech.backend.repository

import com.dieletech.backend.model.Certificate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CertificateRepository : JpaRepository<Certificate, Long> {

    fun findByCode(code: String): Optional<Certificate>
    fun existsByCode(code: String): Boolean
    fun countByCourseId(courseId: Long): Long

    @Query(
        "SELECT c FROM Certificate c " +
        "WHERE LOWER(TRIM(c.userEmail)) = LOWER(TRIM(:email)) " +
        "ORDER BY c.issuedAt DESC"
    )
    fun findMine(@Param("email") email: String): List<Certificate>

    @Query(
        "SELECT c FROM Certificate c " +
        "WHERE LOWER(TRIM(c.userEmail)) = LOWER(TRIM(:email)) AND c.courseId = :courseId"
    )
    fun findForCourse(
        @Param("email") email: String,
        @Param("courseId") courseId: Long
    ): Optional<Certificate>
}
