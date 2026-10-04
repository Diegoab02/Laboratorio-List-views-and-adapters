package service

import com.dieletech.backend.dto.CourseDTO
import com.dieletech.backend.dto.CourseSummaryDTO
import com.dieletech.backend.model.Course
import com.dieletech.backend.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class CourseService(private val courseRepository: CourseRepository) {

    fun getAllCourses(): List<CourseSummaryDTO> =
        courseRepository.findByActiveTrue().map { it.toSummary() }

    fun getCourseById(id: Long): CourseDTO? =
        courseRepository.findById(id).orElse(null)?.toDetail()

    fun getCoursesByTechnology(technology: String): List<CourseSummaryDTO> =
        courseRepository.findByTechnology(technology).filter { it.active }.map { it.toSummary() }

    fun getCoursesByLevel(level: String): List<CourseSummaryDTO> =
        courseRepository.findByLevel(level).filter { it.active }.map { it.toSummary() }

    fun searchCourses(query: String): List<CourseSummaryDTO> =
        courseRepository
            .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query)
            .filter { it.active }
            .map { it.toSummary() }

    /** Divide un campo separado por '|' descartando vacios. */
    private fun String?.pipeList(): List<String> =
        this?.split("|")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    private fun Course.toDetail() = CourseDTO(
        id = id,
        title = title,
        description = description,
        longDescription = longDescription,
        technology = technology,
        level = level,
        price = price,
        duration = duration,
        imageUrl = imageUrl,
        studentCount = studentCount,
        capacity = capacity,
        seatsAvailable = seatsAvailable,
        soldOut = isSoldOut,
        curriculum = curriculum.pipeList(),
        prerequisites = prerequisites.pipeList(),
        learningObjectives = learningObjectives.pipeList(),
        targetAudience = targetAudience.pipeList(),
        previewVideoUrl = previewVideoUrl,
        instructorName = instructorName
    )

    private fun Course.toSummary() = CourseSummaryDTO(
        id = id,
        title = title,
        description = description,
        technology = technology,
        level = level,
        price = price,
        duration = duration,
        imageUrl = imageUrl,
        studentCount = studentCount,
        capacity = capacity,
        seatsAvailable = seatsAvailable,
        soldOut = isSoldOut,
        previewVideoUrl = previewVideoUrl,
        instructorName = instructorName
    )
}
