package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.SessionManager

/**
 * HU-44 · Laboratorio "Diseño de navegabilidad DIELTECH" (Android)
 *
 * Visualiza, dentro de la propia app, el árbol de navegación de DIELTECH
 * organizado en los tres niveles que propone la presentación de apoyo:
 *   top-level  →  categoría  →  detalle / edición.
 *
 * No es contenido estático de marketing: cada ficha es la ruta Compose real
 * declarada en MainActivity.DielTechApp(), de modo que si alguien agrega una
 * pantalla al grafo también la refleja aquí.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationMapScreen(navController: NavController) {
    val context = LocalContext.current
    val role = SessionManager.role(context)
    var currentRole by remember { mutableStateOf(role.ifBlank { "STUDENT" }) }

    val roles = listOf("STUDENT", "INSTRUCTOR", "ADMIN")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de navegación", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E40AF))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Encabezado del laboratorio
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Laboratorio: List views and adapters",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "HU-44 · Propuesta de navegación DIELTECH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Esquema top-level → categoría → detalle/edición aplicado a DIELTECH, " +
                            "siguiendo la presentación de la clase (slides 1 a 5).",
                        fontSize = 13.sp,
                        color = Color(0xFF374151)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Universidad Piloto de Colombia · Arquitectura de Software + " +
                            "Dispositivos Convergentes · Sprint 7.",
                        fontSize = 11.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Selector de perfil (replica el "Según Perfiles" de SeCoCo)
            Text("Perfil (top-level raíz)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                roles.forEach { r ->
                    FilterChip(
                        selected = currentRole == r,
                        onClick = { currentRole = r },
                        label = { Text(r) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Top-level de este perfil
            LevelBlock(
                title = "① Top-level",
                subtitle = "Punto de entrada tras el login",
                color = Color(0xFF1E40AF),
                items = listOf(topLevelFor(currentRole))
            )

            Spacer(Modifier.height(16.dp))

            LevelBlock(
                title = "② Categorías",
                subtitle = "Pestañas de la bottom bar — sin apilarse en el back stack",
                color = Color(0xFFB45309),
                items = categoriesFor(currentRole)
            )

            Spacer(Modifier.height(16.dp))

            LevelBlock(
                title = "③ Detalle / edición",
                subtitle = "Se apilan al entrar; el botón atrás regresa a la categoría",
                color = Color(0xFF15803D),
                items = detailsFor(currentRole)
            )

            Spacer(Modifier.height(20.dp))

            // Reglas de navegación
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Reglas aplicadas", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Rule("Un solo top-level por sesión (lo decide el rol del JWT).")
                    Rule("Las categorías cambian la pestaña, no empujan al back stack.")
                    Rule("Los detalles sí apilan, para que ‘atrás’ regrese a su categoría.")
                    Rule("Checkout → Success hace popUpTo(course) inclusive=false.")
                    Rule("Logout: navigate(login) { popUpTo(0) } — se limpia la pila.")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Datos del mapa ────────────────────────────────────────────────────────

private data class NavNode(val name: String, val route: String, val source: String)

private fun topLevelFor(role: String): NavNode = when (role) {
    "INSTRUCTOR" -> NavNode("InstructorHome", "instructor_main", "HU-09")
    "ADMIN"      -> NavNode("AdminHome",      "admin_main",      "HU-09")
    else         -> NavNode("StudentHome",    "main",            "HU-03")
}

private fun categoriesFor(role: String): List<NavNode> = when (role) {
    "INSTRUCTOR" -> listOf(
        NavNode("Panel instructor",  "instructor_main?tab=0", "HU-09"),
        NavNode("Mis Cursos",        "instructor_main?tab=1", "HU-09"),
        NavNode("Estudiantes",       "instructor_main?tab=2", "HU-09"),
        NavNode("Por calificar",     "instructor_main?tab=3", "HU-39"),
        NavNode("Métricas",          "instructor_main?tab=4", "HU-41"),
        NavNode("Perfil",            "instructor_main?tab=5", "HU-02"),
    )
    "ADMIN" -> listOf(
        NavNode("Dashboard global",  "admin_main?tab=0", "HU-13"),
        NavNode("Cursos",            "admin_main?tab=1", "HU-09"),
        NavNode("Usuarios",          "admin_main?tab=2", "HU-09"),
        NavNode("Métricas",          "admin_main?tab=3", "HU-41"),
        NavNode("Perfil",            "admin_main?tab=4", "HU-02"),
    )
    else -> listOf(
        NavNode("Inicio",            "main?tab=0", "HU-03"),
        NavNode("Catálogo",          "main?tab=1", "HU-04"),
        NavNode("Mis Cursos",        "main?tab=2", "HU-05"),
        NavNode("Mi Panel",          "main?tab=3", "HU-43"),
        NavNode("Perfil",            "main?tab=4", "HU-02"),
    )
}

private fun detailsFor(role: String): List<NavNode> = when (role) {
    "INSTRUCTOR" -> listOf(
        NavNode("Crear curso",         "admin/course/new",         "HU-09"),
        NavNode("Editar curso",        "admin/course/{id}/edit",   "HU-09"),
        NavNode("Lecciones del curso", "admin/course/{id}/lessons","HU-09"),
        NavNode("Tareas del curso",    "admin/course/{id}/assignments", "HU-37"),
        NavNode("Calificar entrega",   "grade/{submissionId}",     "HU-39"),
        NavNode("Rúbrica",             "rubric/{assignmentId}",    "HU-42"),
    )
    "ADMIN" -> listOf(
        NavNode("Editar curso",     "admin/course/{id}/edit", "HU-09"),
        NavNode("Cambiar rol",      "admin/user/{id}/role",   "HU-09"),
    )
    else -> listOf(
        NavNode("Detalle curso",    "course/{id}",                 "HU-05"),
        NavNode("Checkout",         "checkout/{id}",               "HU-06"),
        NavNode("Confirmación",     "success/{name}",              "HU-06"),
        NavNode("Reproductor",      "video/{courseId}/{lessonId}", "HU-07"),
        NavNode("Entregar tarea",   "assignment/{id}",             "HU-38"),
        NavNode("Evaluación",       "quiz/{courseId}",             "HU-10"),
        NavNode("Certificados",     "certificates",                "HU-12"),
        NavNode("Verificar cert.",  "cert/verify",                 "HU-12"),
        NavNode("Editar perfil",    "profile/edit",                "HU-02"),
    )
}

// ─── Composables de apoyo ──────────────────────────────────────────────────

@Composable
private fun LevelBlock(
    title: String,
    subtitle: String,
    color: Color,
    items: List<NavNode>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
        }
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = 12.sp, color = Color(0xFF4B5563))
        Spacer(Modifier.height(10.dp))
        items.forEach { node ->
            NodeCard(node = node, accent = color)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NodeCard(node: NavNode, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 36.dp)
                    .background(accent, shape = RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(node.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(node.route, fontSize = 11.sp, color = Color(0xFF6B7280))
            }
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = {},
                label = { Text(node.source, fontSize = 10.sp) }
            )
        }
    }
}

@Composable
private fun Rule(text: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("•  ", fontWeight = FontWeight.Bold)
        Text(text, fontSize = 13.sp)
    }
}
