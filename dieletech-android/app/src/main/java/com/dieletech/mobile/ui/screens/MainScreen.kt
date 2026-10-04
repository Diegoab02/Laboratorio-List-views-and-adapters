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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.CourseRepository
import com.dieletech.mobile.data.api.SessionManager
import com.dieletech.mobile.data.model.Course
import kotlinx.coroutines.launch

/**
 * HU-03 · Dashboard del estudiante.
 * HU-04 · Catálogo.
 * HU-05 · Mis Cursos.
 * HU-43 · Mi Panel (pendientes agregados).
 * HU-02 / HU-14 · Perfil, con accesos a certificados, verificación y
 * el mapa de navegación del laboratorio (HU-44).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("DielTech", "Catálogo", "Mis Cursos", "Mi Panel", "Perfil")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titles[tab], fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("Catálogo") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    label = { Text("Mis Cursos") }
                )
                NavigationBarItem(
                    selected = tab == 3, onClick = { tab = 3 },
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                    label = { Text("Panel") }
                )
                NavigationBarItem(
                    selected = tab == 4, onClick = { tab = 4 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                0 -> HomeTab(onGoCatalog = { tab = 1 }, onGoCursos = { tab = 2 }, onGoPanel = { tab = 3 })
                1 -> CatalogTab(navController)
                2 -> MisCursosTab(navController)
                3 -> StudentPanelScreen(navController)
                else -> PerfilTab(navController)
            }
        }
    }
}

@Composable
fun HomeTab(onGoCatalog: () -> Unit, onGoCursos: () -> Unit, onGoPanel: () -> Unit) {
    val context = LocalContext.current
    val rawName = SessionManager.name(context)
    val firstName = if (rawName.isNotBlank()) rawName.split(" ").first() else "estudiante"

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E40AF))))
                .padding(24.dp)
        ) {
            Column {
                Text("¡Hola, $firstName! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text("Tu ruta hacia tu 1° en Tech", color = Color.White.copy(alpha = 0.9f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("¿Qué quieres hacer hoy?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        HomeAction("📚", "Explorar catálogo", "Descubre nuevos cursos y compra", onGoCatalog)
        Spacer(Modifier.height(12.dp))
        HomeAction("🎓", "Mis cursos", "Continúa donde te quedaste", onGoCursos)
        Spacer(Modifier.height(12.dp))
        HomeAction("🗂️", "Mi panel", "Tareas y entregas pendientes en todos tus cursos", onGoPanel)
        Spacer(Modifier.height(24.dp))
        Text("Rutas populares", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RouteChip("🐍", "Python", Modifier.weight(1f))
            RouteChip("🌐", "Web", Modifier.weight(1f))
            RouteChip("📚", "Git", Modifier.weight(1f))
        }
    }
}

@Composable
fun HomeAction(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 32.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun RouteChip(icon: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(1.dp), shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CatalogTab(navController: NavController) {
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            courses = CourseRepository.getAllCourses()
            loading = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(courses) { course ->
                CourseCard(course = course, onClick = { navController.navigate("course/${course.id}") })
            }
        }
    }
}

@Composable
fun MisCursosTab(navController: NavController) {
    val context = LocalContext.current
    val email = SessionManager.email(context)
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(email) {
        scope.launch {
            courses = if (email.isBlank()) emptyList() else CourseRepository.getMyCourses(email)
            loading = false
        }
    }

    when {
        !SessionManager.isLoggedIn(context) ->
            EmptyState("🔒", "Inicia sesión", "Entra a tu cuenta para ver tus cursos inscritos") {
                navController.navigate("login")
            }
        loading ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        courses.isEmpty() ->
            EmptyState("📚", "Aún no tienes cursos", "Compra un curso del catálogo para empezar a aprender", null)
        else ->
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(courses) { course ->
                    MyCourseCard(course = course,
                        onOpen = { navController.navigate("course/${course.id}") },
                        onTasks = { navController.navigate("assignments/${course.id}") },
                        onQuiz = { navController.navigate("quiz/${course.id}") }
                    )
                }
            }
    }
}

@Composable
private fun MyCourseCard(course: Course, onOpen: () -> Unit, onTasks: () -> Unit, onQuiz: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(course.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text("${course.technology}  ·  ${course.level}", fontSize = 12.sp, color = Color(0xFF4B5563))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpen, modifier = Modifier.weight(1f)) { Text("Abrir") }
                OutlinedButton(onClick = onTasks, modifier = Modifier.weight(1f)) { Text("Tareas") }
                OutlinedButton(onClick = onQuiz, modifier = Modifier.weight(1f)) { Text("Evaluación") }
            }
        }
    }
}

@Composable
fun EmptyState(icon: String, title: String, subtitle: String, onAction: (() -> Unit)?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        if (onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text("Iniciar sesión") }
        }
    }
}

@Composable
fun PerfilTab(navController: NavController) {
    val context = LocalContext.current
    val name = SessionManager.name(context)
    val email = SessionManager.email(context)
    val logged = SessionManager.isLoggedIn(context)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (name.isNotBlank()) name.first().uppercase() else "?",
                color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(if (logged) name else "Invitado", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            if (logged) email else "No has iniciado sesión",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(28.dp))

        if (logged) {
            ProfileAction("✏️", "Editar perfil") { navController.navigate("profile/edit") }
            Spacer(Modifier.height(10.dp))
            ProfileAction("🎓", "Mis certificados") { navController.navigate("certificates") }
            Spacer(Modifier.height(10.dp))
            ProfileAction("🔎", "Verificar certificado") { navController.navigate("cert/verify") }
            Spacer(Modifier.height(10.dp))
            ProfileAction("🗺️", "Mapa de navegación (HU-44)") { navController.navigate("nav_map") }

            Spacer(Modifier.height(28.dp))
            OutlinedButton(
                onClick = {
                    SessionManager.logout(context)
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar sesión") }
        } else {
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Iniciar sesión") }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { navController.navigate("cert/verify") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("🔎  Verificar certificado") }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { navController.navigate("nav_map") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("🗺️  Mapa de navegación (HU-44)") }
        }
    }
}

@Composable
private fun ProfileAction(icon: String, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}
