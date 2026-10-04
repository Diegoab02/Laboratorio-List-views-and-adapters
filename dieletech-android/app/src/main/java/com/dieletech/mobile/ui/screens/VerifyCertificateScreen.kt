package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.layout.*
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
import com.dieletech.mobile.data.api.CertificateRepository
import com.dieletech.mobile.data.model.CertificateVerificationNet
import kotlinx.coroutines.launch

/** HU-12 · Verificación pública de un certificado por su código. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCertificateScreen(navController: NavController) {
    var code by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CertificateVerificationNet?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar certificado", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2563EB))
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Ingresa el código del certificado:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(32) },
                singleLine = true,
                label = { Text("Código") },
                placeholder = { Text("Ej. DIEL-ABC12345") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        result = CertificateRepository.verify(code)
                        loading = false
                    }
                },
                enabled = code.length >= 4 && !loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Verificando..." else "Verificar") }

            Spacer(Modifier.height(16.dp))
            result?.let { r ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (r.valid) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (r.valid) "✅ Certificado válido" else "❌ No válido",
                            color = if (r.valid) Color(0xFF15803D) else Color(0xFFB91C1C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(r.message)
                        if (r.valid) {
                            Spacer(Modifier.height(10.dp))
                            r.studentName?.let { Text("Estudiante: $it", fontSize = 13.sp) }
                            r.courseTitle?.let { Text("Curso: $it", fontSize = 13.sp) }
                            r.instructorName?.let { Text("Instructor: $it", fontSize = 13.sp) }
                            r.courseHours?.let { Text("Horas: $it", fontSize = 13.sp) }
                            r.score?.let { Text("Puntaje: $it/100", fontSize = 13.sp) }
                            r.issuedAt?.let { Text("Emitido: $it", fontSize = 13.sp) }
                        }
                    }
                }
            }
        }
    }
}
