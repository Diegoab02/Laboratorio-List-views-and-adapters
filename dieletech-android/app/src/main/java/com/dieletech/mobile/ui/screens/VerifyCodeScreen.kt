package com.dieletech.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyCodeScreen(email: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var resending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var cooldown by remember { mutableIntStateOf(0) }

    // Cooldown timer for resend
    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000L)
            cooldown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.size(80.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Verifica tu correo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Enviamos un codigo de 6 digitos a:",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            email,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Success state
        if (success) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Cuenta verificada",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ya puedes iniciar sesion",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("verify_code/{email}") { inclusive = true }
                        popUpTo("register") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Login, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ir a iniciar sesion", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            return
        }

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

        // Code input
        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
            label = { Text("Codigo de verificacion") },
            placeholder = { Text("000000") },
            leadingIcon = { Icon(Icons.Outlined.Pin, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (code.length == 6) {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            },
            supportingText = { Text("${code.length}/6 digitos") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Verify button
        Button(
            onClick = {
                if (code.length != 6) {
                    error = "Ingresa el codigo completo de 6 digitos"
                    return@Button
                }
                loading = true; error = null
                scope.launch {
                    val result = AuthRepository.verifyCode(email.trim().lowercase(), code)
                    loading = false
                    result.onSuccess { success = true }
                        .onFailure { error = it.message ?: "Codigo incorrecto" }
                }
            },
            enabled = !loading && code.length == 6,
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
                if (loading) "Verificando..." else "Verificar cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Resend code
        OutlinedButton(
            onClick = {
                resending = true; error = null
                scope.launch {
                    val result = AuthRepository.resendCode(email.trim().lowercase())
                    resending = false
                    result.onSuccess {
                        cooldown = 60
                        code = ""
                    }.onFailure {
                        error = it.message ?: "Error al reenviar el codigo"
                    }
                }
            },
            enabled = !resending && cooldown == 0,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (resending) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                when {
                    resending -> "Reenviando..."
                    cooldown > 0 -> "Reenviar en ${cooldown}s"
                    else -> "Reenviar codigo"
                },
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate("login") { popUpTo("register") { inclusive = true } } }) {
            Text("Volver a inicio de sesion", fontSize = 14.sp)
        }
    }
}
