package service

import com.dieletech.backend.dto.ChangePasswordDTO
import com.dieletech.backend.dto.UpdateProfileDTO
import com.dieletech.backend.dto.UserProfileDTO
import com.dieletech.backend.model.User
import com.dieletech.backend.repository.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class UserProfileService(
    private val userRepository: UserRepository,
    private val purchaseRepository: PurchaseRepository,
    private val lessonRepository: LessonRepository,
    private val lessonProgressRepository: LessonProgressRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun getProfile(email: String): UserProfileDTO {
        val user = userRepository.findByEmail(email.trim().lowercase())
            .orElseThrow { RuntimeException("Usuario no encontrado") }
        return user.toProfileDTO()
    }

    @Transactional
    fun updateProfile(email: String, dto: UpdateProfileDTO): UserProfileDTO {
        val user = userRepository.findByEmail(email.trim().lowercase())
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        // Solo se sobrescribe lo que viene en la peticion: un PUT parcial
        // no debe borrar campos que el formulario no envio.
        dto.name?.takeIf { it.isNotBlank() }?.let { user.name = it.trim() }
        dto.displayName?.let { user.displayName = it.trim().ifBlank { null } }
        dto.bio?.let { user.bio = it.trim().ifBlank { null } }
        dto.jobTitle?.let { user.jobTitle = it.trim().ifBlank { null } }
        dto.phone?.let { user.phone = it.trim().ifBlank { null } }
        dto.country?.let { user.country = it.trim().ifBlank { null } }
        dto.city?.let { user.city = it.trim().ifBlank { null } }
        dto.interests?.let { list ->
            user.interests = list.map { it.trim() }.filter { it.isNotEmpty() }
                .take(10).joinToString("|").ifBlank { null }
        }
        dto.linkedinUrl?.let { user.linkedinUrl = it.trim().ifBlank { null } }
        dto.githubUrl?.let { user.githubUrl = it.trim().ifBlank { null } }
        dto.websiteUrl?.let { user.websiteUrl = it.trim().ifBlank { null } }
        dto.avatarUrl?.let { user.avatarUrl = it.ifBlank { null } }
        dto.themePreference?.takeIf { it.isNotBlank() }?.let { user.themePreference = it }
        dto.accentColor?.takeIf { it.isNotBlank() }?.let { user.accentColor = it }
        dto.languagePreference?.takeIf { it.isNotBlank() }?.let { user.languagePreference = it }
        dto.notifyEmail?.let { user.notifyEmail = it }
        dto.notifyNewCourses?.let { user.notifyNewCourses = it }
        dto.notifyProgress?.let { user.notifyProgress = it }
        dto.publicProfile?.let { user.publicProfile = it }

        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)
        return user.toProfileDTO()
    }

    @Transactional
    fun changePassword(email: String, dto: ChangePasswordDTO): String {
        val user = userRepository.findByEmail(email.trim().lowercase())
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        if (!passwordEncoder.matches(dto.currentPassword, user.password)) {
            throw RuntimeException("La contrasena actual no es correcta")
        }
        if (dto.newPassword != dto.confirmPassword) {
            throw RuntimeException("La nueva contrasena y su confirmacion no coinciden")
        }
        if (passwordEncoder.matches(dto.newPassword, user.password)) {
            throw RuntimeException("La nueva contrasena debe ser diferente a la actual")
        }

        user.password = passwordEncoder.encode(dto.newPassword)
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)
        return "Contrasena actualizada correctamente"
    }

    /** Avatar en data URI. Limite de 500 KB para no saturar la base. */
    @Transactional
    fun updateAvatar(email: String, dataUri: String): UserProfileDTO {
        if (!dataUri.startsWith("data:image/")) {
            throw RuntimeException("El archivo debe ser una imagen")
        }
        if (dataUri.length > 500_000) {
            throw RuntimeException("La imagen supera el limite de 500 KB. Usa una mas liviana.")
        }
        val user = userRepository.findByEmail(email.trim().lowercase())
            .orElseThrow { RuntimeException("Usuario no encontrado") }
        user.avatarUrl = dataUri
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)
        return user.toProfileDTO()
    }

    private fun User.toProfileDTO(): UserProfileDTO {
        val purchases = purchaseRepository.findByUserEmailNormalized(email)

        var lessonsDone = 0
        var completedCourses = 0
        val progressPerCourse = mutableListOf<Int>()

        for (p in purchases) {
            val total = lessonRepository.countByCourseIdAndActiveTrue(p.courseId)
            val done = lessonProgressRepository.countCompletedByEmailAndCourse(email, p.courseId)
            lessonsDone += done
            val pct = if (total > 0) (done * 100) / total else 0
            progressPerCourse.add(pct)
            if (pct >= 100) completedCourses++
        }

        return UserProfileDTO(
            id = id,
            name = name,
            displayName = displayName,
            email = email,
            role = role.name,
            verified = verified,
            avatarUrl = avatarUrl,
            bio = bio,
            jobTitle = jobTitle,
            phone = phone,
            country = country,
            city = city,
            interests = interests?.split("|")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
            linkedinUrl = linkedinUrl,
            githubUrl = githubUrl,
            websiteUrl = websiteUrl,
            themePreference = themePreference,
            accentColor = accentColor,
            languagePreference = languagePreference,
            notifyEmail = notifyEmail,
            notifyNewCourses = notifyNewCourses,
            notifyProgress = notifyProgress,
            publicProfile = publicProfile,
            memberSince = createdAt.format(monthFmt),
            coursesEnrolled = purchases.size,
            coursesCompleted = completedCourses,
            lessonsCompleted = lessonsDone,
            totalInvested = purchases.sumOf { it.amount },
            averageProgress = if (progressPerCourse.isNotEmpty()) progressPerCourse.average().toInt() else 0
        )
    }
}
