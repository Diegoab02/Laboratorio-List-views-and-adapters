package com.dieletech.mobile.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dieletech.mobile.data.api.CourseRepository
import com.dieletech.mobile.data.api.SessionManager
import com.dieletech.mobile.data.model.Course
import com.dieletech.mobile.data.model.PurchaseRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(courseId: Long, navController: NavController) {
    val context = LocalContext.current
    var course by remember { mutableStateOf<Course?>(null) }
    var loading by remember { mutableStateOf(true) }
    var processing by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf(SessionManager.name(context)) }
    var email by remember { mutableStateOf(SessionManager.email(context)) }
    var selectedPayment by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCVC by remember { mutableStateOf("") }
    // Campos obligatorios que exige el backend (PurchaseRequestDTO)
    var documentId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Espejo de las restricciones de PurchaseRequestDTO en el backend.
    val nameValid = fullName.trim().length in 5..100 &&
        fullName.trim().all { it.isLetter() || it == ' ' || it == '.' || it == '\'' || it == '-' }
    val documentValid = documentId.trim().length in 6..15 && documentId.trim().all { it.isDigit() }
    val phoneValid = phone.trim().length in 7..20 &&
        phone.trim().all { it.isDigit() || it in "+ ()-" }
    val paymentValid = selectedPayment in listOf("card", "pse", "paypal")
    val formValid = nameValid && documentValid && phoneValid && paymentValid &&
        acceptTerms && course?.soldOut != true

    val scope = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        scope.launch {
            course = CourseRepository.getCourseById(courseId)
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Compra") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            course?.let { c ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Curso Seleccionado", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(c.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(c.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total:", fontWeight = FontWeight.Bold)
                                Text(
                                    "$${String.format("%,.0f", c.price)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Datos de facturacion", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Todos los campos marcados con * son obligatorios.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    errorMsg?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                msg,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMsg = null },
                        label = { Text("Nombre completo *") },
                        isError = fullName.isNotEmpty() && !nameValid,
                        supportingText = {
                            if (fullName.isNotEmpty() && !nameValid)
                                Text("Minimo 5 caracteres, solo letras")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = documentId,
                        onValueChange = { new ->
                            if (new.all { it.isDigit() } && new.length <= 15) {
                                documentId = new; errorMsg = null
                            }
                        },
                        label = { Text("Documento de identidad *") },
                        placeholder = { Text("1020304050") },
                        isError = documentId.isNotEmpty() && !documentValid,
                        supportingText = {
                            if (documentId.isNotEmpty() && !documentValid)
                                Text("Entre 6 y 15 digitos")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; errorMsg = null },
                        label = { Text("Telefono de contacto *") },
                        placeholder = { Text("+57 300 123 4567") },
                        isError = phone.isNotEmpty() && !phoneValid,
                        supportingText = {
                            if (phone.isNotEmpty() && !phoneValid)
                                Text("Entre 7 y 20 digitos")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        label = { Text("Correo electronico") },
                        readOnly = true,
                        enabled = false,
                        supportingText = { Text("La matricula queda ligada al correo de tu sesion") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Metodo de pago *", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "card" to "Tarjeta de credito/debito",
                        "pse" to "PSE - Debito bancario",
                        "paypal" to "PayPal"
                    ).forEach { (id, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPayment == id,
                                onClick = { selectedPayment = id; errorMsg = null }
                            )
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    if (selectedPayment == "card") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Numero de tarjeta") },
                            placeholder = { Text("4111 1111 1111 1111") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it },
                                label = { Text("MM/YY") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = cardCVC,
                                onValueChange = { cardCVC = it },
                                label = { Text("CVC") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = acceptTerms,
                            onCheckedChange = { acceptTerms = it; errorMsg = null }
                        )
                        Text(
                            "Acepto los terminos y condiciones y la politica de tratamiento de datos personales de Dieletech. *",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 12.dp)
                        )
                    }

                    if (c.soldOut) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Este curso agoto sus ${c.capacity} cupos.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!formValid) {
                                errorMsg = "Completa todos los campos obligatorios."
                                return@Button
                            }
                            processing = true
                            errorMsg = null
                            scope.launch {
                                val result = CourseRepository.purchaseCourse(
                                    PurchaseRequest(
                                        courseId = c.id,
                                        fullName = fullName.trim(),
                                        email = email.trim().lowercase(),
                                        documentId = documentId.trim(),
                                        phone = phone.trim(),
                                        paymentMethod = selectedPayment,
                                        amount = c.price,
                                        acceptTerms = acceptTerms
                                    )
                                )
                                processing = false
                                if (result.success) {
                                    navController.navigate("success/${Uri.encode(c.title)}") {
                                        popUpTo("main")
                                    }
                                } else {
                                    errorMsg = result.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !processing && formValid,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (processing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                if (c.soldOut) "Cupos agotados"
                                else "Pagar $${String.format("%,.0f", c.price)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!formValid && !c.soldOut) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Completa todos los campos obligatorios para habilitar el pago.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tu información es segura y encriptada 🔒",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
