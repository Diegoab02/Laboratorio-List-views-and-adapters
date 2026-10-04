package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.AssignmentRepository
import com.dieletech.mobile.data.api.CourseRepository
import com.dieletech.mobile.data.api.SessionManager
import com.dieletech.mobile.data.model.Course
import com.dieletech.mobile.data.model.StudentAssignmentNet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * HU-43 · Panel unificado del estudiante.
 *
 * El backend aún no expone /api/me/panel en todas las ramas del proyecto,
 * así que esta vista agrega del cliente combinando getMyCourses + myCourseView.
 * Cuando el endpoint esté estable basta reemplazar la agregación.
 */
@Composable
fun StudentPanelScreen(navController: NavController) {
    val context = LocalContext.current
    val email = SessionManager.email(context)
    var loading by remember { mutableStateOf(true) }
    var pending by remember { mutableStateOf<List<Pair<Course, StudentAssignmentNet>>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(email) {
        if (email.isBlank()) { loading = false; return@LaunchedEffect }
        scope.launch {
            loading = true
            val myCourses = CourseRepository.getMyCourses(email)
            val assembled = coroutineScope {
                myCourses.map { c -> async { c to AssignmentRepository.myCourseView(c.id) } }.awaitAll()
            }
            pending = assembled.flatMap { (course, list) ->
                list.filter { it.submission?.status != "GRADED" }.map { course to it }
            }.sortedBy { it.second.daysLeft ?: Long.MAX_VALUE }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Mi panel · pendientes",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )
        when {
            email.isBlank() -> EmptyState("🔒", "Inicia sesión", "Entra a tu cuenta para ver tus pendientes", { navController.navigate("login") })
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            pending.isEmpty() -> EmptyState("🎉", "¡Al día!", "No tienes tareas pendientes en tus cursos", null)
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pending) { (course, sa) ->
                    PendingRow(course, sa) { navController.navigate("assignment/${sa.assignment.id}") }
                }
            }
        }
    }
}

@Composable
private fun PendingRow(course: Course, sa: StudentAssignmentNet, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(course.title, fontSize = 12.sp, color = Color(0xFF4B5563))
            Spacer(Modifier.height(2.dp))
            Text(sa.assignment.title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            val d = sa.daysLeft
            val label = when {
                d == null -> sa.submission?.status ?: "Sin entregar"
                d < 0 -> "Vencida hace ${-d} día(s)"
                d == 0L -> "Entrega hoy"
                else -> "Faltan $d día(s)"
            }
            val color = when {
                d != null && d < 0 -> Color(0xFFB91C1C)
                d != null && d <= 2 -> Color(0xFFB45309)
                else -> Color(0xFF2563EB)
            }
            Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
