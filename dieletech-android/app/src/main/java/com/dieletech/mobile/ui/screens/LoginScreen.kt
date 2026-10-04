package com.dieletech.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.AuthRepository
import com.dieletech.mobile.data.api.SessionManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPw by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val emailPattern = android.util.Patterns.EMAIL_ADDRESS
    val emailValid = email.isNotBlank() && emailPattern.matcher(email.trim()).matches()
    val passwordValid = password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
        Spacer(Modifier.height(32.dp))

        Text(
            "Bienvenido de vuelta",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ingresa tus credenciales para continuar",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

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

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
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
            onValueChange = { password = it; error = null },
            label = { Text("Contrasena") },
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
            visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        // Forgot password link (HU-03)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { navController.navigate("forgot_password") }) {
                Text(
                    "Olvidaste tu contrasena?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Login button
        Button(
            onClick = {
                if (!emailValid) {
                    error = "Ingresa un correo electronico valido"; return@Button
                }
                if (!passwordValid) {
                    error = "Ingresa tu contrasena"; return@Button
                }
                loading = true; error = null
                scope.launch {
                    val result = AuthRepository.login(email.trim().lowercase(), password)
                    loading = false
                    result.onSuccess {
                        SessionManager.save(context, it.token, it.name, it.email, it.role)
                        val dest = when (it.role) {
                            "INSTRUCTOR" -> "instructor_main"
                            "ADMIN" -> "admin_main"
                            else -> "main"
                        }
                        navController.navigate(dest) { popUpTo("login") { inclusive = true } }
                    }.onFailure {
                        val msg = it.message ?: "Error al iniciar sesion"
                        // If user is not verified, redirect to verify screen
                        if (msg.contains("verificad", ignoreCase = true) || msg.contains("verify", ignoreCase = true)) {
                            navController.navigate("verify_code/${email.trim().lowercase()}")
                        } else {
                            error = msg
                        }
                    }
                }
            },
            enabled = !loading && emailValid && passwordValid,
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
                if (loading) "Ingresando..." else "Iniciar sesion",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Register link
        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("No tienes cuenta? ", fontSize = 14.sp)
            Text("Registrate gratis", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // Explore without login
        OutlinedButton(
            onClick = {
                navController.navigate("main") { popUpTo("login") { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Explore, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Explorar catalogo sin cuenta", fontSize = 14.sp)
        }
    }
}
