package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.CertificateRepository
import com.dieletech.mobile.data.model.CertificateNet
import kotlinx.coroutines.launch

/** HU-12 · Lista de certificados emitidos al estudiante. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatesScreen(navController: NavController) {
    var items by remember { mutableStateOf<List<CertificateNet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            loading = true
            items = CertificateRepository.mine()
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis certificados", fontWeight = FontWeight.Bold, color = Color.White) },
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
                items.isEmpty() -> EmptyState("🎓", "Aún no tienes certificados", "Completa un curso y su evaluación final para obtener uno", null)
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { c -> CertificateCard(c) }
                }
            }
        }
    }
}

@Composable
private fun CertificateCard(c: CertificateNet) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(
            Modifier.background(
                Brush.horizontalGradient(listOf(Color(0xFF1E40AF), Color(0xFF4F46E5)))
            ).padding(16.dp)
        ) {
            Text("DIELTECH", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(c.courseTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("Otorgado a ${c.studentName}", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Puntaje", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                    Text("${c.score}/100", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Horas", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                    Text("${c.courseHours} h", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Código", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                    Text(c.code, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("Emitido: ${c.issuedAt}", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
        }
    }
}
