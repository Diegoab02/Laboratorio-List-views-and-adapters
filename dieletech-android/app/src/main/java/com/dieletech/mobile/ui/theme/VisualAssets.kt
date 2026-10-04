package com.dieletech.mobile.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Web
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ÚNICO punto de personalización visual por curso.
 * Cambia aquí los colores, el ícono, la imagen hero o el gradiente y
 * el resto de la app se actualiza sola (CatalogScreen, CourseDetail,
 * LessonList, VideoPlayer, banners...).
 *
 * Para agregar un curso nuevo: añade un entry a [byCourseId].
 */
data class CourseVisual(
    val accent: Color,
    val accentDark: Color,
    val heroUrl: String,
    val icon: ImageVector,
    val tagline: String
) {
    val gradient: Brush
        get() = Brush.linearGradient(listOf(accent, accentDark))
}

object VisualAssets {

    val default = CourseVisual(
        accent = Color(0xFF2563EB),
        accentDark = Color(0xFF1E3A8A),
        heroUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200",
        icon = Icons.Filled.Code,
        tagline = "Tu ruta hacia tu 1° en Tech"
    )

    private val byCourseId: Map<Long, CourseVisual> = mapOf(
        1L to CourseVisual( // Python
            accent = Color(0xFF3776AB),
            accentDark = Color(0xFFFFD43B),
            heroUrl = "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=1200",
            icon = Icons.Filled.DataObject,
            tagline = "Aprende Python desde cero"
        ),
        2L to CourseVisual( // HTML/CSS/JS
            accent = Color(0xFFE34F26),
            accentDark = Color(0xFF264DE4),
            heroUrl = "https://images.unsplash.com/photo-1621839673705-6617adf9e890?w=1200",
            icon = Icons.Filled.Web,
            tagline = "El trío que hace vivir la web"
        ),
        3L to CourseVisual( // Git
            accent = Color(0xFFF05032),
            accentDark = Color(0xFF181717),
            heroUrl = "https://images.unsplash.com/photo-1556075798-4825dfaaf498?w=1200",
            icon = Icons.Filled.Merge,
            tagline = "Versiona como los profesionales"
        ),
        4L to CourseVisual( // Spring Boot
            accent = Color(0xFF6DB33F),
            accentDark = Color(0xFF1B5E20),
            heroUrl = "https://images.unsplash.com/photo-1517180102446-f3ece451e9d8?w=1200",
            icon = Icons.Filled.DeveloperMode,
            tagline = "Backends que aguantan producción"
        ),
        5L to CourseVisual( // React
            accent = Color(0xFF61DAFB),
            accentDark = Color(0xFF20232A),
            heroUrl = "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=1200",
            icon = Icons.Filled.Language,
            tagline = "UIs modernas y reactivas"
        ),
        6L to CourseVisual( // MySQL
            accent = Color(0xFF4479A1),
            accentDark = Color(0xFF1F3A57),
            heroUrl = "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=1200",
            icon = Icons.Filled.Storage,
            tagline = "Datos bien modelados, apps rápidas"
        ),
    )

    fun forCourse(courseId: Long): CourseVisual = byCourseId[courseId] ?: default
}
