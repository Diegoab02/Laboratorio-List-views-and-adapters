package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.dieletech.mobile.data.model.AssignmentAdminNet
import com.dieletech.mobile.data.model.CreateAssignmentRequest
import kotlinx.coroutines.launch

/** HU-37 (admin) · CRUD de tareas por curso para el instructor/admin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAssignmentManagerScreen(courseId: Long, navController: NavController) {
    var items by remember { mutableStateOf<List<AssignmentAdminNet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AssignmentAdminNet?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            loading = true
            items = AssignmentRepository.listAdmin(courseId)
            loading = false
        }
    }

    LaunchedEffect(courseId) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas del curso", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF7C3AED))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showForm = true }, containerColor = Color(0xFF7C3AED)) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva tarea", tint = Color.White)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            message?.let {
                Text(it, Modifier.padding(16.dp), color = Color(0xFF15803D), fontSize = 13.sp)
            }
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                items.isEmpty() -> EmptyState("📝", "Sin tareas", "Crea la primera tarea del curso", null)
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { a -> AdminAssignmentRow(a, onEdit = { editing = a; showForm = true }, onDelete = {
                        scope.launch {
                            val r = AssignmentRepository.deleteAdmin(a.id)
                            r.fold(
                                onSuccess = { message = "Tarea archivada"; reload() },
                                onFailure = { message = it.message }
                            )
                        }
                    }) }
                }
            }
        }

        if (showForm) {
            AssignmentFormDialog(
                initial = editing,
                onDismiss = { showForm = false },
                onConfirm = { req ->
                    scope.launch {
                        val r = if (editing == null)
                            AssignmentRepository.createAdmin(courseId, req)
                        else
                            AssignmentRepository.updateAdmin(editing!!.id, req).map {
                                it
                            }
                        r.fold(
                            onSuccess = {
                                showForm = false
                                message = if (editing == null) "Tarea creada" else "Tarea actualizada"
                                reload()
                            },
                            onFailure = { message = it.message }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminAssignmentRow(a: AssignmentAdminNet, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(a.title, fontWeight = FontWeight.Bold)
                Text("Tipo: ${a.type} · ${a.maxScore} pts · ${a.dueOffsetDays} día(s)", fontSize = 12.sp, color = Color(0xFF4B5563))
                a.lessonTitle?.let { Text("Módulo: $it", fontSize = 12.sp, color = Color(0xFF4B5563)) }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color(0xFFB91C1C)) }
        }
    }
}

@Composable
private fun AssignmentFormDialog(
    initial: AssignmentAdminNet?,
    onDismiss: () -> Unit,
    onConfirm: (CreateAssignmentRequest) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var statement by remember { mutableStateOf(initial?.statement.orEmpty()) }
    var type by remember { mutableStateOf(initial?.type ?: "TEXT") }
    var maxScore by remember { mutableStateOf((initial?.maxScore ?: 100.0).toString()) }
    var dueDays by remember { mutableStateOf((initial?.dueOffsetDays ?: 14).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nueva tarea" else "Editar tarea") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(title, { title = it.take(160) }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(statement, { statement = it.take(4000) }, label = { Text("Enunciado") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text("Tipo", fontSize = 12.sp, color = Color(0xFF4B5563))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("TEXT", "CODE", "FILE").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(maxScore, { maxScore = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Puntaje máx") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(dueDays, { dueDays = it.filter { c -> c.isDigit() } },
                        label = { Text("Plazo (días)") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val max = maxScore.toDoubleOrNull() ?: 100.0
                val d = dueDays.toIntOrNull() ?: 14
                onConfirm(
                    CreateAssignmentRequest(
                        title = title.trim(),
                        statement = statement.trim(),
                        type = type,
                        maxScore = max,
                        dueOffsetDays = d,
                        lessonId = initial?.lessonId,
                        orderIndex = initial?.orderIndex ?: 0
                    )
                )
            }, enabled = title.length >= 5 && statement.length >= 20) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
