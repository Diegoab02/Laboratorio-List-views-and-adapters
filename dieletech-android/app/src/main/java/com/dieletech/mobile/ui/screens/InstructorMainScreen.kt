package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.CourseRepository
import com.dieletech.mobile.data.api.SessionManager
import com.dieletech.mobile.data.model.Course
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorMainScreen(navController: NavController) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Panel Instructor", "Mis Cursos", "Estudiantes", "Mi Perfil")
    val icons = listOf(Icons.Filled.Dashboard, Icons.Filled.MenuBook, Icons.Filled.Groups, Icons.Filled.Person)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titles[tab], fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7C3AED) // Purple for instructor
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                titles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(icons[index], contentDescription = null) },
                        label = { Text(title.split(" ").last(), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                0 -> InstructorHomeTab()
                1 -> InstructorCoursesTab(navController)
                2 -> InstructorStudentsTab()
                else -> InstructorProfileTab(navController)
            }
        }
    }
}

@Composable
private fun InstructorHomeTab() {
    val context = LocalContext.current
    val name = SessionManager.name(context).split(" ").firstOrNull() ?: "Instructor"
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch { courses = CourseRepository.getAllCourses() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Welcome banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF5B21B6))))
                .padding(24.dp)
        ) {
            Column {
                Text("Panel de Instructor", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text("Bienvenido, $name", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Gestiona tus cursos y contenido", color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(24.dp))

        // Stats cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                icon = Icons.Filled.MenuBook,
                label = "Cursos",
                value = "${courses.size}",
                color = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.Groups,
                label = "Estudiantes",
                value = "${courses.sumOf { it.studentCount }}",
                color = Color(0xFF2563EB),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.AccessTime,
                label = "Horas",
                value = "${courses.sumOf { it.duration }}",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("Acciones rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        QuickActionCard(Icons.Filled.Add, "Crear nuevo curso", "Agrega un curso al catálogo", Color(0xFF7C3AED)) {}
        Spacer(Modifier.height(8.dp))
        QuickActionCard(Icons.Filled.Edit, "Editar contenido", "Modifica lecciones existentes", Color(0xFF2563EB)) {}
        Spacer(Modifier.height(8.dp))
        QuickActionCard(Icons.Filled.BarChart, "Ver estadísticas", "Revisa el rendimiento", Color(0xFF10B981)) {}
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun InstructorCoursesTab(navController: NavController) {
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            courses = CourseRepository.getAllCourses()
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gestión de Cursos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("${courses.size} cursos publicados", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(courses) { course ->
                    InstructorCourseCard(
                        course = course,
                        onOpen = { navController.navigate("course/${course.id}") },
                        onAssignments = { navController.navigate("admin/course/${course.id}/assignments") }
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructorCourseCard(course: Course, onOpen: () -> Unit, onAssignments: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.clickable { onOpen() }) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Tech icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (course.technology.lowercase()) {
                        "python" -> "Py"
                        "react" -> "Re"
                        "java", "spring boot" -> "Jv"
                        "git" -> "Gt"
                        "mysql" -> "DB"
                        else -> course.technology.take(2).uppercase()
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED),
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${course.level} · ${course.studentCount} estudiantes",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format("%,.0f", course.price)}", fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED), fontSize = 14.sp)
                Text("${course.duration}h", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 10.dp)) {
            TextButton(onClick = onAssignments) { Text("Gestionar tareas") }
        }
        }
    }
}

@Composable
private fun InstructorCourseCardActions(onAssignments: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
        TextButton(onClick = onAssignments) { Text("Gestionar tareas") }
    }
}

@Composable
private fun InstructorStudentsTab() {
    // Mock students for now — in real app this would call an API
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Groups, contentDescription = null,
            modifier = Modifier.size(64.dp), tint = Color(0xFF7C3AED).copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text("Estudiantes inscritos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Aquí podrás ver los estudiantes inscritos en tus cursos, su progreso y calificaciones.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = {}) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Actualizar lista")
        }
    }
}

@Composable
private fun InstructorProfileTab(navController: NavController) {
    val context = LocalContext.current
    val name = SessionManager.name(context)
    val email = SessionManager.email(context)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(Color(0xFF7C3AED)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (name.isNotBlank()) name.first().uppercase() else "?",
                color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF7C3AED).copy(alpha = 0.1f)
        ) {
            Text(
                "INSTRUCTOR",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = Color(0xFF7C3AED),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileAction(Icons.Filled.Edit, "Editar perfil") { navController.navigate("profile/edit") }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileAction(Icons.Filled.Map, "Mapa de navegación (HU-44)") { navController.navigate("nav_map") }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileAction(Icons.Filled.Search, "Verificar certificado") { navController.navigate("cert/verify") }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = {
                SessionManager.logout(context)
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
        ) { Text("Cerrar sesión") }
    }
}

@Composable
private fun ProfileItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null,
            modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}


@Composable
private fun ProfileAction(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null,
            modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}
