package com.dieletech.backend.repository

import com.dieletech.backend.model.LessonProgress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LessonProgressRepository : JpaRepository<LessonProgress, Long> {

    fun findByUserEmailAndLessonId(userEmail: String, lessonId: Long): LessonProgress?

    fun findByUserEmailAndCourseId(userEmail: String, courseId: Long): List<LessonProgress>

    @Query(
        "SELECT COUNT(lp) FROM LessonProgress lp " +
        "WHERE LOWER(TRIM(lp.userEmail)) = LOWER(TRIM(:email)) " +
        "AND lp.courseId = :courseId AND lp.completed = true"
    )
    fun countCompletedByEmailAndCourse(
        @Param("email") email: String,
        @Param("courseId") courseId: Long
    ): Int

    /**
     * Comprobacion de una sola leccion, normalizada por el mismo motivo
     * que el conteo: una diferencia de mayusculas daba por incompleta una
     * leccion que el estudiante si habia terminado (HU-38).
     */
    @Query(
        "SELECT COUNT(lp) > 0 FROM LessonProgress lp " +
        "WHERE LOWER(TRIM(lp.userEmail)) = LOWER(TRIM(:email)) " +
        "AND lp.lessonId = :lessonId AND lp.completed = true"
    )
    fun isLessonCompleted(
        @Param("email") email: String,
        @Param("lessonId") lessonId: Long
    ): Boolean
}
