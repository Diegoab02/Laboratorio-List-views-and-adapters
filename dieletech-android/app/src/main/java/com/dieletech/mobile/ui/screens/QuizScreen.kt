package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.clickable
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
import com.dieletech.mobile.data.api.QuizRepository
import com.dieletech.mobile.data.model.QuestionResultNet
import com.dieletech.mobile.data.model.QuizInfoNet
import com.dieletech.mobile.data.model.SubmitAnswerRequest
import kotlinx.coroutines.launch

/** HU-10 · Evaluación final del curso con retroalimentación por pregunta. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(courseId: Long, navController: NavController) {
    var quiz by remember { mutableStateOf<QuizInfoNet?>(null) }
    var loading by remember { mutableStateOf(true) }
    val answers = remember { mutableStateMapOf<Long, Int>() }
    var submitting by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<QuestionResultNet>>(emptyList()) }
    var finalScore by remember { mutableStateOf<Int?>(null) }
    var passed by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        scope.launch {
            loading = true
            quiz = QuizRepository.get(courseId)
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluación final", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2563EB))
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            when {
                loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                quiz == null -> EmptyState("⚠️", "No se pudo cargar", "Reintenta más tarde", null)
                !quiz!!.available -> {
                    Text("Evaluación bloqueada", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(quiz!!.blockedReason ?: "Completa todas las lecciones y tareas para desbloquear la evaluación.")
                    Spacer(Modifier.height(12.dp))
                    Text("Progreso: ${quiz!!.lessonsCompleted} / ${quiz!!.totalLessons} lecciones", fontSize = 13.sp)
                }
                finalScore != null -> {
                    val color = if (passed) Color(0xFF15803D) else Color(0xFFB91C1C)
                    Text(if (passed) "¡Aprobaste!" else "No alcanzaste el puntaje", color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Puntaje: $finalScore / 100  ·  Mínimo requerido: ${quiz!!.passingScore}")
                    Spacer(Modifier.height(16.dp))
                    results.forEach { r ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(r.text, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                r.options.forEachIndexed { i, opt ->
                                    val mark = when {
                                        i == r.correctIndex -> "✅"
                                        i == r.selectedIndex -> "❌"
                                        else -> "  "
                                    }
                                    Text("$mark $opt", fontSize = 13.sp)
                                }
                                r.explanation?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Nota: $it", fontSize = 12.sp, color = Color(0xFF4B5563))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
                else -> {
                    Text(quiz!!.courseTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${quiz!!.totalQuestions} preguntas  ·  Aprueba con ${quiz!!.passingScore}/100", fontSize = 12.sp, color = Color(0xFF4B5563))
                    Spacer(Modifier.height(16.dp))
                    quiz!!.questions.forEach { q ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(q.text, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                q.options.forEachIndexed { i, opt ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { answers[q.id] = i }.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(selected = answers[q.id] == i, onClick = { answers[q.id] = i })
                                        Spacer(Modifier.width(4.dp))
                                        Text(opt, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }
                    Button(
                        onClick = {
                            scope.launch {
                                submitting = true
                                val payload = quiz!!.questions.mapNotNull { q ->
                                    answers[q.id]?.let { SubmitAnswerRequest(q.id, it) }
                                }
                                if (payload.size != quiz!!.questions.size) {
                                    error = "Responde todas las preguntas"
                                    submitting = false
                                    return@launch
                                }
                                val r = QuizRepository.submit(courseId, payload)
                                submitting = false
                                r.fold(
                                    onSuccess = {
                                        finalScore = it.score
                                        passed = it.passed
                                        results = it.results
                                        error = null
                                    },
                                    onFailure = { error = it.message }
                                )
                            }
                        },
                        enabled = !submitting,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (submitting) "Enviando..." else "Enviar evaluación") }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
