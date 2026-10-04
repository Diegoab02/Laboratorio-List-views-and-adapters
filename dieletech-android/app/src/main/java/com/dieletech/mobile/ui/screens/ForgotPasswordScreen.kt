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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.AuthRepository
import kotlinx.coroutines.launch

/**
 * HU-03: Recuperacion de contrasena - Paso 1
 * El usuario ingresa su correo y el backend envia un codigo de 6 digitos.
 */
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }

    val emailPattern = android.util.Patterns.EMAIL_ADDRESS
    val emailValid = email.isNotBlank() && emailPattern.matcher(email.trim()).matches()

    fun submit() {
        if (!emailValid) {
            error = "Ingresa un correo electronico valido"
            return
        }
        focusManager.clearFocus()
        loading = true
        error = null
        scope.launch {
            val result = AuthRepository.forgotPassword(email.trim().lowercase())
            loading = false
            result.onSuccess { sent = true }
                .onFailure { error = it.message ?: "No se pudo enviar el codigo" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.LockReset,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        Text(
            "Recupera tu contrasena",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Te enviaremos un codigo de 6 digitos para que puedas crear una nueva contrasena.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

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

        AnimatedVisibility(visible = sent) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MarkEmailRead, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Revisa tu correo", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Si ${email.trim().lowercase()} esta registrado, recibiras un codigo de 6 digitos. Expira en 30 minutos.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

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
                        tint = if (emailValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            isError = email.isNotEmpty() && !emailValid,
            supportingText = {
                if (email.isNotEmpty() && !emailValid)
                    Text("Ingresa un correo valido", color = MaterialTheme.colorScheme.error)
            },
            enabled = !loading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { submit() },
            enabled = !loading && emailValid,
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
                if (loading) "Enviando..." else if (sent) "Reenviar codigo" else "Enviar codigo",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(visible = sent) {
            Button(
                onClick = { navController.navigate("reset_password/${email.trim().lowercase()}") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Ya tengo el codigo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Volver a iniciar sesion", fontSize = 14.sp)
        }
    }
}
