package com.meviplast.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meviplast.app.data.DataStoreManager
import com.meviplast.app.data.Session
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Deseas salir de Meviplast?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        dataStoreManager.clearToken()
                        Session.clear()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                }) { Text("Salir", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MEVIPLAST", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Mi Perfil")
                    }
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hola, ${Session.userName ?: "Usuario"}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bienvenido al panel de control",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 1. INVENTARIO (Admin, Supervisor, Almacenista, Vendedor)
            if (Session.isAdmin || Session.isSupervisor || Session.isAlmacenista || Session.isVendedor) {
                MenuButton(
                    text = if (Session.isVendedor) "Inventario de Productos" else "Inventario de Materias",
                    icon = Icons.Default.Inventory,
                    onClick = { navController.navigate("inventory") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. PRODUCCIÓN (Admin, Supervisor, Operario)
            if (Session.isAdmin || Session.isSupervisor || Session.isOperario) {
                MenuButton(
                    text = "Tareas de Producción",
                    icon = Icons.Default.PrecisionManufacturing,
                    onClick = { navController.navigate("production") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. VENTAS (Solo Administrador y Vendedor)
            if (Session.isAdmin || Session.isVendedor) {
                MenuButton(
                    text = "Registro de Ventas",
                    icon = Icons.Default.ShoppingCart,
                    onClick = { navController.navigate("sales") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. GESTIÓN DE USUARIOS (Solo Administrador)
            if (Session.isAdmin) {
                MenuButton(
                    text = "Gestión de Usuarios",
                    icon = Icons.Default.Group,
                    onClick = { navController.navigate("users") },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. REPORTES (Admin, Supervisor)
            if (Session.isAdmin || Session.isSupervisor) {
                MenuButton(
                    text = "Reportes Globales",
                    icon = Icons.Default.Assessment,
                    onClick = { navController.navigate("reports") },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
