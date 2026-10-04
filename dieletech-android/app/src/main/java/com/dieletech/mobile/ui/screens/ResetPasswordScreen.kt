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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * HU-03: Recuperacion de contrasena - Paso 2
 * El usuario ingresa el codigo de 6 digitos recibido por correo y su nueva contrasena.
 */
@Composable
fun ResetPasswordScreen(email: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPw by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableIntStateOf(0) }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown -= 1
        }
    }

    // Tras exito, volver al login automaticamente
    LaunchedEffect(success) {
        if (success) {
            delay(2000)
            navController.navigate("login") { popUpTo("login") { inclusive = true } }
        }
    }

    val codeValid = code.length == 6
    val passwordValid = password.length >= 6
    val confirmValid = confirm.isNotEmpty() && confirm == password
    val formValid = codeValid && passwordValid && confirmValid

    fun submit() {
        when {
            !codeValid -> { error = "El codigo debe tener 6 digitos"; return }
            !passwordValid -> { error = "La contrasena debe tener minimo 6 caracteres"; return }
            !confirmValid -> { error = "Las contrasenas no coinciden"; return }
        }
        focusManager.clearFocus()
        loading = true
        error = null
        scope.launch {
            val result = AuthRepository.resetPassword(code.trim(), password)
            loading = false
            result.onSuccess { success = true }
                .onFailure { error = it.message ?: "Codigo invalido o expirado" }
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
            Icons.Outlined.Password,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        Text(
            "Nueva contrasena",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ingresa el codigo que enviamos a $email y elige tu nueva contrasena.",
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

        AnimatedVisibility(visible = success) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Contrasena actualizada. Redirigiendo al inicio de sesion...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Codigo de 6 digitos
        OutlinedTextField(
            value = code,
            onValueChange = { new -> if (new.length <= 6 && new.all { it.isDigit() }) { code = new; error = null } },
            label = { Text("Codigo de verificacion") },
            placeholder = { Text("000000") },
            leadingIcon = { Icon(Icons.Outlined.Pin, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                Text(
                    "${code.length}/6",
                    fontSize = 12.sp,
                    color = if (codeValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            enabled = !loading && !success,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Nueva contrasena
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Nueva contrasena") },
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
            isError = password.isNotEmpty() && !passwordValid,
            supportingText = {
                if (password.isNotEmpty() && !passwordValid)
                    Text("Minimo 6 caracteres", color = MaterialTheme.colorScheme.error)
            },
            enabled = !loading && !success,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Confirmar contrasena
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = { Text("Confirmar contrasena") },
            leadingIcon = { Icon(Icons.Outlined.LockReset, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (confirm.isNotEmpty()) {
                    Icon(
                        if (confirmValid) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        null,
                        tint = if (confirmValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirm.isNotEmpty() && !confirmValid,
            supportingText = {
                if (confirm.isNotEmpty() && !confirmValid)
                    Text("Las contrasenas no coinciden", color = MaterialTheme.colorScheme.error)
            },
            enabled = !loading && !success,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { submit() },
            enabled = !loading && !success && formValid,
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
                if (loading) "Actualizando..." else "Cambiar contrasena",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = {
                if (resendCooldown == 0) {
                    resendCooldown = 60
                    scope.launch {
                        AuthRepository.forgotPassword(email)
                            .onFailure { error = it.message }
                    }
                }
            },
            enabled = resendCooldown == 0 && !loading && !success,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (resendCooldown > 0) "Reenviar codigo en ${resendCooldown}s" else "No recibi el codigo. Reenviar",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        TextButton(
            onClick = { navController.navigate("login") { popUpTo("login") { inclusive = true } } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Volver a iniciar sesion", fontSize = 14.sp)
        }
    }
}
