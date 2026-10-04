package com.dieletech.backend.repository

import com.dieletech.backend.model.Purchase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PurchaseRepository : JpaRepository<Purchase, Long> {

    // Mantiene compatibilidad con llamadas existentes (búsqueda exacta).
    fun findByUserEmail(userEmail: String): List<Purchase>

    fun findByCourseId(courseId: Long): List<Purchase>
    fun countByCourseId(courseId: Long): Long
    fun findByOrderId(orderId: String): Purchase?

    // Verificación exacta (compat) y normalizada.
    fun existsByCourseIdAndUserEmail(courseId: Long, userEmail: String): Boolean

    /**
     * Búsqueda robusta: ignora mayúsculas/espacios.
     * Fix del bug "Mis cursos no aparece" cuando el email guardado difiere en
     * capitalización o espacios del email del usuario logueado.
     */
    @Query(
        "SELECT p FROM Purchase p " +
        "WHERE LOWER(TRIM(p.userEmail)) = LOWER(TRIM(:email))"
    )
    fun findByUserEmailNormalized(@Param("email") email: String): List<Purchase>

    @Query(
        "SELECT COUNT(p) > 0 FROM Purchase p " +
        "WHERE p.courseId = :courseId " +
        "AND LOWER(TRIM(p.userEmail)) = LOWER(TRIM(:email))"
    )
    fun existsPurchaseNormalized(
        @Param("courseId") courseId: Long,
        @Param("email") email: String
    ): Boolean
}
