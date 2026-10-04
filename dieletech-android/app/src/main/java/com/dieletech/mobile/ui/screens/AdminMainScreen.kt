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
fun AdminMainScreen(navController: NavController) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Administración", "Cursos", "Usuarios", "Sistema")
    val icons = listOf(Icons.Filled.AdminPanelSettings, Icons.Filled.MenuBook, Icons.Filled.People, Icons.Filled.Settings)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titles[tab], fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFDC2626) // Red for admin
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
                        label = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                0 -> AdminDashboardTab()
                1 -> AdminCoursesTab(navController)
                2 -> AdminUsersTab()
                else -> AdminSystemTab(navController)
            }
        }
    }
}

@Composable
private fun AdminDashboardTab() {
    val context = LocalContext.current
    val name = SessionManager.name(context).split(" ").firstOrNull() ?: "Admin"
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
        // Admin welcome banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B))))
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Panel de Administrador", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                }
                Spacer(Modifier.height(8.dp))
                Text("Hola, $name", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Control total de la plataforma", color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(24.dp))

        // Stats grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard("Cursos", "${courses.size}", Icons.Filled.MenuBook, Color(0xFFDC2626), Modifier.weight(1f))
            AdminStatCard("Estudiantes", "${courses.sumOf { it.studentCount }}", Icons.Filled.People, Color(0xFF2563EB), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminStatCard("Horas", "${courses.sumOf { it.duration }}", Icons.Filled.AccessTime, Color(0xFF10B981), Modifier.weight(1f))
            AdminStatCard("Tecnologías", "${courses.map { it.technology }.distinct().size}", Icons.Filled.Code, Color(0xFFF59E0B), Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text("Acciones administrativas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        AdminActionCard(Icons.Filled.PersonAdd, "Gestionar usuarios", "Crear, editar, cambiar roles", Color(0xFF2563EB)) {}
        Spacer(Modifier.height(8.dp))
        AdminActionCard(Icons.Filled.Add, "Nuevo curso", "Agregar un curso al catálogo", Color(0xFF10B981)) {}
        Spacer(Modifier.height(8.dp))
        AdminActionCard(Icons.Filled.Assessment, "Reportes", "Ver métricas de la plataforma", Color(0xFFF59E0B)) {}
        Spacer(Modifier.height(8.dp))
        AdminActionCard(Icons.Filled.Storage, "Base de datos", "Estado del sistema y BD", Color(0xFF6366F1)) {}
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun AdminActionCard(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
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
private fun AdminCoursesTab(navController: NavController) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Todos los cursos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${courses.size} cursos en la plataforma", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            FilledTonalButton(onClick = {}) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Nuevo")
            }
        }
        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(courses) { course ->
                    AdminCourseCard(course) { navController.navigate("course/${course.id}") }
                }
            }
        }
    }
}

@Composable
private fun AdminCourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.title, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${course.technology} · ${course.level}",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Text("$${String.format("%,.0f", course.price)}", fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626), fontSize = 15.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.width(4.dp))
                    Text("${course.studentCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.width(4.dp))
                    Text("${course.duration}h", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {}, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.People, contentDescription = null,
            modifier = Modifier.size(64.dp), tint = Color(0xFFDC2626).copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text("Gestión de usuarios", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Administra los usuarios de la plataforma, asigna roles (Estudiante, Instructor, Admin) y gestiona permisos.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Crear usuario")
        }
    }
}

@Composable
private fun AdminSystemTab(navController: NavController) {
    val context = LocalContext.current
    val name = SessionManager.name(context)
    val email = SessionManager.email(context)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        // Profile card
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFDC2626)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (name.isNotBlank()) name.first().uppercase() else "A",
                        color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFDC2626).copy(alpha = 0.1f)) {
                        Text("ADMINISTRADOR", modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                AdminProfileAction("Editar perfil") { navController.navigate("profile/edit") }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                AdminProfileAction("Mapa de navegacion (HU-44)") { navController.navigate("nav_map") }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                AdminProfileAction("Verificar certificado") { navController.navigate("cert/verify") }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Sistema", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                SystemInfoRow("Versión", "1.0.0")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SystemInfoRow("Backend", "Spring Boot 3.2")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SystemInfoRow("Base de datos", "MySQL")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SystemInfoRow("API", "http://10.0.2.2:8080")
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
private fun SystemInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}


@androidx.compose.runtime.Composable
private fun AdminProfileAction(text: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        androidx.compose.material3.Text(text, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.weight(1f))
        androidx.compose.material3.Icon(
            androidx.compose.material.icons.Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = androidx.compose.ui.Modifier.size(20.dp),
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}
