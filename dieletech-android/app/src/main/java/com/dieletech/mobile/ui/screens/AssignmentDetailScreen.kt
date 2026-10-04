package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.dieletech.mobile.data.model.StudentAssignmentNet
import kotlinx.coroutines.launch

/**
 * HU-38 · Entrega del estudiante: borrador y envío definitivo.
 *
 * Las reglas de negocio están replicadas aquí como validaciones de UI, pero
 * la autoridad final es el backend (mismo mensaje que el frontend web).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailScreen(assignmentId: Long, navController: NavController) {
    var sa by remember { mutableStateOf<StudentAssignmentNet?>(null) }
    var loading by remember { mutableStateOf(true) }
    var content by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(assignmentId) {
        scope.launch {
            loading = true
            val fresh = AssignmentRepository.myAssignment(assignmentId)
            sa = fresh
            content = fresh?.submission?.content.orEmpty()
            fileUrl = fresh?.submission?.fileUrl.orEmpty()
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entregar tarea", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2563EB))
            )
        }
    ) { padding ->
        when {
            loading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            sa == null -> EmptyState("⚠️", "No se encontró la tarea", "Vuelve atrás e intenta otra vez", null)
            else -> {
                val a = sa!!.assignment
                val sub = sa!!.submission
                val canSubmit = sa!!.canSubmit && sub?.status != "GRADED"
                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(a.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tipo: ${a.type}  ·  Puntaje máximo: ${a.maxScore}  ·  Plazo: ${a.dueOffsetDays} día(s)",
                        fontSize = 12.sp, color = Color(0xFF4B5563)
                    )
                    a.lessonTitle?.let {
                        Spacer(Modifier.height(4.dp))
                        Text("Módulo: $it", fontSize = 12.sp, color = Color(0xFF4B5563))
                    }
                    Spacer(Modifier.height(16.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Enunciado", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                a.statement ?: "Enunciado bloqueado — compra el curso para verlo.",
                                fontSize = 14.sp
                            )
                        }
                    }

                    sa!!.blockedReason?.let {
                        Spacer(Modifier.height(12.dp))
                        Text("⚠️ $it", color = Color(0xFFB91C1C), fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it.take(20000) },
                        label = { Text("Tu entrega") },
                        placeholder = { Text("Escribe aquí tu respuesta...") },
                        minLines = 6,
                        enabled = canSubmit,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${content.length}/20000", fontSize = 11.sp, color = Color(0xFF6B7280))

                    if (a.type == "FILE") {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = fileUrl,
                            onValueChange = { fileUrl = it.take(1000) },
                            label = { Text("Enlace al archivo (http/https)") },
                            enabled = canSubmit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    message?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            it,
                            color = if (messageIsError) Color(0xFFB91C1C) else Color(0xFF15803D),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { saveSubmission(scope, assignmentId, content, fileUrl, true,
                                onLoading = { saving = it },
                                onMessage = { msg, isErr -> message = msg; messageIsError = isErr },
                                onReload = {
                                    scope.launch { sa = AssignmentRepository.myAssignment(assignmentId) }
                                }
                            ) },
                            enabled = canSubmit && !saving,
                            modifier = Modifier.weight(1f)
                        ) { Text("Guardar borrador") }
                        Button(
                            onClick = { saveSubmission(scope, assignmentId, content, fileUrl, false,
                                onLoading = { saving = it },
                                onMessage = { msg, isErr -> message = msg; messageIsError = isErr },
                                onReload = {
                                    scope.launch { sa = AssignmentRepository.myAssignment(assignmentId) }
                                }
                            ) },
                            enabled = canSubmit && !saving && content.length >= 10,
                            modifier = Modifier.weight(1f)
                        ) { Text("Entregar") }
                    }

                    sub?.let {
                        Spacer(Modifier.height(20.dp))
                        Card(shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Estado de la entrega", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("Estado: ${it.status}", fontSize = 13.sp)
                                it.submittedAt?.let { s -> Text("Enviada: $s", fontSize = 12.sp, color = Color(0xFF4B5563)) }
                                it.gradedAt?.let { g ->
                                    Text("Calificada: $g", fontSize = 12.sp, color = Color(0xFF4B5563))
                                }
                                it.score?.let { score ->
                                    Spacer(Modifier.height(6.dp))
                                    Text("Nota: ${"%.1f".format(score)} / ${a.maxScore}", fontWeight = FontWeight.SemiBold)
                                }
                                it.feedback?.let { fb ->
                                    Spacer(Modifier.height(6.dp))
                                    Text("Retroalimentación: $fb", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun saveSubmission(
    scope: kotlinx.coroutines.CoroutineScope,
    assignmentId: Long,
    content: String,
    fileUrl: String,
    draft: Boolean,
    onLoading: (Boolean) -> Unit,
    onMessage: (String, Boolean) -> Unit,
    onReload: () -> Unit
) {
    scope.launch {
        onLoading(true)
        val result = AssignmentRepository.submit(
            assignmentId = assignmentId,
            content = content,
            fileUrl = fileUrl.ifBlank { null },
            draft = draft
        )
        onLoading(false)
        result.fold(
            onSuccess = {
                onMessage(if (draft) "Borrador guardado" else "Entrega registrada", false)
                onReload()
            },
            onFailure = { onMessage(it.message ?: "No se pudo guardar", true) }
        )
    }
}
