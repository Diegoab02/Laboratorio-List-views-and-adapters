package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.AssignmentRepository
import com.dieletech.mobile.data.api.SessionManager
import com.dieletech.mobile.data.model.StudentAssignmentNet
import kotlinx.coroutines.launch

/**
 * HU-37 / HU-38 · Lista de tareas del estudiante en un curso.
 * Reusa /api/courses/{id}/assignments/mine, que ya trae la entrega en el
 * mismo viaje (canSubmit, blockedReason, daysLeft).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentListScreen(courseId: Long, navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var items by remember { mutableStateOf<List<StudentAssignmentNet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        scope.launch {
            loading = true
            items = AssignmentRepository.myCourseView(courseId)
            loading = false
            if (items.isEmpty() && !SessionManager.isLoggedIn(context)) {
                error = "Inicia sesion para ver las tareas"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas del curso", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2563EB))
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                items.isEmpty() -> EmptyState(
                    icon = "📘",
                    title = "Aun no hay tareas",
                    subtitle = error ?: "El instructor aun no publica tareas para este curso",
                    onAction = null
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { sa ->
                        AssignmentRow(sa) {
                            navController.navigate("assignment/${sa.assignment.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentRow(sa: StudentAssignmentNet, onClick: () -> Unit) {
    val status = when {
        sa.submission?.status == "GRADED" -> "Calificada" to Color(0xFF15803D)
        sa.submission?.status == "SUBMITTED" -> "Entregada" to Color(0xFFB45309)
        sa.submission?.status == "DRAFT" -> "Borrador" to Color(0xFF6B7280)
        sa.assignment.locked -> "Bloqueada" to Color(0xFFB91C1C)
        else -> "Sin entregar" to Color(0xFFB45309)
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(sa.assignment.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(status.first, color = status.second, fontSize = 11.sp) })
            }
            sa.assignment.lessonTitle?.let {
                Spacer(Modifier.height(4.dp))
                Text("Módulo: $it", fontSize = 12.sp, color = Color(0xFF4B5563))
            }
            sa.daysLeft?.let { days ->
                Spacer(Modifier.height(6.dp))
                val txt = when {
                    days < 0 -> "Vencida hace ${-days} día(s)"
                    days == 0L -> "Entrega hoy"
                    else -> "Faltan $days día(s)"
                }
                Text(txt, fontSize = 12.sp, color = if (days < 0) Color(0xFFB91C1C) else Color(0xFF374151))
            }
            sa.blockedReason?.let {
                Spacer(Modifier.height(6.dp))
                Text("⚠️ $it", fontSize = 12.sp, color = Color(0xFFB91C1C))
            }
            sa.submission?.score?.let {
                Spacer(Modifier.height(6.dp))
                Text("Nota: ${"%.1f".format(it)} / ${sa.assignment.maxScore}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}
