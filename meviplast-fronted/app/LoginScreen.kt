package com.meviplast.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meviplast.app.data.DataStoreManager
import com.meviplast.app.data.LoginRequest
import com.meviplast.app.data.RetrofitClient
import com.meviplast.app.data.Session
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MEVIPLAST",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Gestión de Producción", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor completa todos los campos"
                    return@Button
                }
                loading = true
                errorMessage = ""
                scope.launch {
                    try {
                        val response = RetrofitClient.api.login(LoginRequest(email, password))
                        if (response.isSuccessful) {
                            val body = response.body()
                            // Guardar sesión
                            Session.startSession(body?.token, body?.user)
                            
                            // Persistir token (Reto 3 de la guía anterior aplicado aquí)
                            body?.token?.let { dataStoreManager.saveToken(it) }
                            
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            errorMessage = "Error: Credenciales incorrectas"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de conexión: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Entrar")
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text(text = "¿No tienes cuenta? Regístrate aquí")
        }
    }
}
