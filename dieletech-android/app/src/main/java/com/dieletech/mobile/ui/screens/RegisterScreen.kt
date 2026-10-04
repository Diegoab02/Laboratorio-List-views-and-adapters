package com.dieletech.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.AuthRepository
import kotlinx.coroutines.launch

data class RoleOption(val value: String, val label: String, val icon: ImageVector, val desc: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("STUDENT") }
    var showPw by remember { mutableStateOf(false) }
    var showConfirmPw by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var registeredEmail by remember { mutableStateOf<String?>(null) }

    // Validation states
    val nameValid = name.isNotBlank() && name.trim().length >= 3
    val emailPattern = android.util.Patterns.EMAIL_ADDRESS
    val emailValid = email.isNotBlank() && emailPattern.matcher(email.trim()).matches()
    val passwordValid = password.length >= 6
    val passwordsMatch = password == confirmPassword && confirmPassword.isNotEmpty()

    val roles = listOf(
        RoleOption("STUDENT", "Estudiante", Icons.Filled.School, "Aprende nuevas tecnologias"),
        RoleOption("INSTRUCTOR", "Instructor", Icons.Filled.Person, "Crea y gestiona cursos"),
        RoleOption("ADMIN", "Admin", Icons.Filled.Settings, "Administra la plataforma")
    )

    // If registration was successful, navigate to verify screen
    if (registeredEmail != null) {
        LaunchedEffect(registeredEmail) {
            navController.navigate("verify_code/${registeredEmail}") {
                popUpTo("register") { inclusive = true }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Icon(
            Icons.Filled.School,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Dieletech",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Tu ruta hacia tu 1° empleo en Tech",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Text(
            "Crear cuenta",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Selecciona tu rol y completa tus datos",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Error message
        AnimatedVisibility(visible = error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(error ?: "", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                }
            }
        }

        // Role selector chips
        Text("Tipo de cuenta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            roles.forEach { role ->
                val selected = selectedRole == role.value
                OutlinedCard(
                    onClick = { selectedRole = role.value },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            role.icon,
                            contentDescription = role.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            role.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo") },
            placeholder = { Text("Ej: Diego Martinez") },
            leadingIcon = { Icon(Icons.Outlined.Person, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (name.isNotEmpty()) {
                    Icon(
                        if (nameValid) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        null,
                        tint = if (nameValid) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            isError = name.isNotEmpty() && !nameValid,
            supportingText = {
                if (name.isNotEmpty() && !nameValid)
                    Text("Minimo 3 caracteres", color = MaterialTheme.colorScheme.error)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electronico") },
            placeholder = { Text("tu@email.com") },
            leadingIcon = { Icon(Icons.Outlined.Email, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    Icon(
                        if (emailValid) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        null,
                        tint = if (emailValid) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            isError = email.isNotEmpty() && !emailValid,
            supportingText = {
                if (email.isNotEmpty() && !emailValid)
                    Text("Ingresa un correo valido", color = MaterialTheme.colorScheme.error)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasena") },
            placeholder = { Text("Minimo 6 caracteres") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                IconButton(onClick = { showPw = !showPw }) {
                    Icon(
                        if (showPw) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showPw) "Ocultar" else "Mostrar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            isError = password.isNotEmpty() && !passwordValid,
            supportingText = {
                if (password.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (passwordValid) Icons.Filled.CheckCircle else Icons.Filled.Info,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (passwordValid) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (passwordValid) "Contrasena segura" else "${password.length}/6 caracteres",
                            fontSize = 12.sp,
                            color = if (passwordValid) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Confirm password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contrasena") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (confirmPassword.isNotEmpty()) {
                    IconButton(onClick = { showConfirmPw = !showConfirmPw }) {
                        Icon(
                            if (showConfirmPw) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showConfirmPw) "Ocultar" else "Mostrar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            supportingText = {
                if (confirmPassword.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (passwordsMatch) Icons.Filled.CheckCircle else Icons.Filled.Info,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (passwordsMatch) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (passwordsMatch) "Las contrasenas coinciden" else "Las contrasenas no coinciden",
                            fontSize = 12.sp,
                            color = if (passwordsMatch) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            visualTransformation = if (showConfirmPw) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Register button
        val formValid = nameValid && emailValid && passwordValid && passwordsMatch
        Button(
            onClick = {
                if (!formValid) {
                    error = when {
                        !nameValid -> "El nombre debe tener al menos 3 caracteres"
                        !emailValid -> "Ingresa un correo electronico valido"
                        !passwordValid -> "La contrasena debe tener al menos 6 caracteres"
                        !passwordsMatch -> "Las contrasenas no coinciden"
                        else -> "Completa todos los campos"
                    }
                    return@Button
                }
                loading = true; error = null
                scope.launch {
                    val result = AuthRepository.register(
                        name.trim(), email.trim().lowercase(), password, selectedRole
                    )
                    loading = false
                    result.onSuccess {
                        registeredEmail = email.trim().lowercase()
                    }.onFailure {
                        error = it.message ?: "Error al registrarse"
                    }
                }
            },
            enabled = !loading && formValid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                if (loading) "Creando cuenta..." else "Crear cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ya tienes cuenta? ", fontSize = 14.sp)
            Text("Inicia sesion", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
