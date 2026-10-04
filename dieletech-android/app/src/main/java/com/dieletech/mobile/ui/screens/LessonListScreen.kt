package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dieletech.mobile.data.api.LessonRepository
import com.dieletech.mobile.data.local.ProgressStore
import com.dieletech.mobile.data.model.Lesson
import com.dieletech.mobile.ui.components.CourseHero
import com.dieletech.mobile.ui.components.CourseProgressBar
import com.dieletech.mobile.ui.components.LessonRow
import com.dieletech.mobile.ui.theme.VisualAssets
import kotlinx.coroutines.flow.first

/**
 * HU-07 + HU-08: pantalla de lecciones.
 *   - Muestra banner del curso (personalizable en VisualAssets.kt).
 *   - Muestra barra de progreso del curso.
 *   - Lista de lecciones con estado (completada / en progreso).
 *   - Al tocar una lección navega al reproductor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    courseId: Long,
    courseTitle: String,
    onBack: () -> Unit,
    onOpenLesson: (Long) -> Unit
) {
    val ctx = LocalContext.current
    val store = remember { ProgressStore(ctx) }
    val visual = remember(courseId) { VisualAssets.forCourse(courseId) }

    var lessons by remember { mutableStateOf<List<Lesson>>(emptyList()) }
    var completedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var inProgressIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(courseId) {
        loading = true
        val list = LessonRepository.byCourse(courseId)
        val done = mutableSetOf<Long>()
        val inProg = mutableSetOf<Long>()
        for (l in list) {
            val isDone = store.isCompleted(l.id).first()
            val pos = store.lastPositionMs(l.id).first()
            if (isDone) done.add(l.id)
            else if (pos > 5000) inProg.add(l.id)
        }
        lessons = list
        completedIds = done
        inProgressIds = inProg
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(courseTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { inner ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = visual.accent)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CourseHero(
                    title = courseTitle,
                    subtitle = "${lessons.size} lecciones · ${totalMinutes(lessons)} min",
                    visual = visual
                )
            }
            item {
                CourseProgressBar(
                    percent = if (lessons.isEmpty()) 0f else completedIds.size.toFloat() / lessons.size,
                    completed = completedIds.size,
                    total = lessons.size,
                    color = visual.accent
                )
            }
            item {
                Text(
                    text = "Lecciones",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(lessons, key = { it.id }) { lesson ->
                LessonRow(
                    lesson = lesson,
                    completed = lesson.id in completedIds,
                    inProgress = lesson.id in inProgressIds,
                    accent = visual.accent,
                    onClick = { onOpenLesson(lesson.id) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private fun totalMinutes(lessons: List<Lesson>) = lessons.sumOf { it.durationSec } / 60
