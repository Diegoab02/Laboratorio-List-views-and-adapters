package com.dieletech.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.dieletech.mobile.data.api.ProfileRepository
import com.dieletech.mobile.data.model.UpdateProfileRequest
import com.dieletech.mobile.data.model.UserProfileNet
import kotlinx.coroutines.launch

/** HU-14 · Editar perfil del usuario autenticado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    var profile by remember { mutableStateOf<UserProfileNet?>(null) }
    var loading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }
    var github by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true
            val p = ProfileRepository.me()
            profile = p
            if (p != null) {
                name = p.name
                displayName = p.displayName.orEmpty()
                bio = p.bio.orEmpty()
                jobTitle = p.jobTitle.orEmpty()
                phone = p.phone.orEmpty()
                city = p.city.orEmpty()
                country = p.country.orEmpty()
                linkedin = p.linkedinUrl.orEmpty()
                github = p.githubUrl.orEmpty()
                website = p.websiteUrl.orEmpty()
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2563EB))
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (profile == null) {
            EmptyState("⚠️", "No se pudo cargar", "Reintenta más tarde", null)
        } else {
            Column(
                Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Text("Datos personales", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(name, { name = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(displayName, { displayName = it }, label = { Text("Nombre para mostrar") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(bio, { bio = it.take(500) }, label = { Text("Biografía") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(jobTitle, { jobTitle = it }, label = { Text("Cargo") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(phone, { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(city, { city = it }, label = { Text("Ciudad") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(country, { country = it }, label = { Text("País") }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Text("Enlaces", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(linkedin, { linkedin = it }, label = { Text("LinkedIn") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(github, { github = it }, label = { Text("GitHub") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(website, { website = it }, label = { Text("Sitio web") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                message?.let {
                    Text(it, color = if (isError) Color(0xFFB91C1C) else Color(0xFF15803D), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            val r = ProfileRepository.update(
                                UpdateProfileRequest(
                                    name = name.ifBlank { null },
                                    displayName = displayName.ifBlank { null },
                                    bio = bio.ifBlank { null },
                                    jobTitle = jobTitle.ifBlank { null },
                                    phone = phone.ifBlank { null },
                                    city = city.ifBlank { null },
                                    country = country.ifBlank { null },
                                    linkedinUrl = linkedin.ifBlank { null },
                                    githubUrl = github.ifBlank { null },
                                    websiteUrl = website.ifBlank { null }
                                )
                            )
                            saving = false
                            r.fold(
                                onSuccess = { profile = it; message = "Perfil actualizado"; isError = false },
                                onFailure = { message = it.message; isError = true }
                            )
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (saving) "Guardando..." else "Guardar cambios") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
