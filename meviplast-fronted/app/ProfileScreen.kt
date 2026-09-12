package com.meviplast.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meviplast.app.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf(Session.userName ?: "") }
    var email by remember { mutableStateOf(Session.token?.let { "" } ?: "") } // Simulado o cargar de perfi real
    
    // Al inicio cargamos el email real desde el perfil
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getProfile(Session.bearer())
            if (response.isSuccessful) {
                nombre = response.body()?.user?.UserName ?: nombre
                email = response.body()?.user?.Email ?: ""
            }
        } catch (_: Exception) {}
    }

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    
    var isUpdatingProfile by remember { mutableStateOf(false) }
    var isUpdatingPass by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SECCIÓN 1: DATOS PERSONALES
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Datos Personales", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdatingProfile
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdatingProfile
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (nombre.isNotBlank() && email.isNotBlank()) {
                                scope.launch {
                                    isUpdatingProfile = true
                                    try {
                                        val resp = RetrofitClient.api.updateProfile(Session.bearer(), UpdateProfileRequest(nombre, email))
                                        if (resp.isSuccessful) {
                                            Session.userName = nombre
                                            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                                        } else {
                                            snackbarHostState.showSnackbar("Error al actualizar perfil")
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Fallo: ${e.message}")
                                    } finally { isUpdatingProfile = false }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdatingProfile
                    ) {
                        if (isUpdatingProfile) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar Cambios")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN 2: SEGURIDAD
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seguridad", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Contraseña Actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdatingPass
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdatingPass
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirmar Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdatingPass
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (newPass != confirmPass) {
                                scope.launch { snackbarHostState.showSnackbar("Las contraseñas no coinciden") }
                                return@Button
                            }
                            if (currentPass.isBlank() || newPass.isBlank()) return@Button
                            
                            scope.launch {
                                isUpdatingPass = true
                                try {
                                    val resp = RetrofitClient.api.changePassword(Session.bearer(), ChangePasswordRequest(currentPass, newPass))
                                    if (resp.isSuccessful) {
                                        snackbarHostState.showSnackbar("Contraseña actualizada con éxito")
                                        currentPass = ""; newPass = ""; confirmPass = ""
                                    } else {
                                        snackbarHostState.showSnackbar("Error: Contraseña actual incorrecta")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Fallo: ${e.message}")
                                } finally { isUpdatingPass = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        enabled = !isUpdatingPass
                    ) {
                        if (isUpdatingPass) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Actualizar Contraseña")
                        }
                    }
                }
            }
        }
    }
}
