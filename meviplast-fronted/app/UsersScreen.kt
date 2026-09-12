package com.meviplast.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meviplast.app.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(onBack: () -> Unit) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var stats by remember { mutableStateOf<UserStatsResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Campos para creación
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Roles específicos de Meviplast
    val rolesOptions = listOf(
        "Administrador" to 1,
        "Supervisor" to 2,
        "Operario" to 3,
        "Almacenista" to 4,
        "Vendedor" to 5
    )
    var selectedRole by remember { mutableStateOf(rolesOptions[0]) }
    var expandedRoles by remember { mutableStateOf(false) }

    fun cargarDatos() {
        loading = true
        scope.launch {
            try {
                val respUsers = RetrofitClient.api.getUsers(Session.bearer(), 50)
                val respStats = RetrofitClient.api.getStats(Session.bearer())
                
                if (respUsers.isSuccessful) users = respUsers.body()?.users ?: emptyList()
                if (respStats.isSuccessful) stats = respStats.body()
            } catch (e: Exception) {
                message = "Error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun cambiarRol(userId: Int, roleId: Int) {
        scope.launch {
            try {
                val resp = RetrofitClient.api.updateRoles(
                    Session.bearer(),
                    userId,
                    UpdateRolesRequest(listOf(roleId))
                )
                if (resp.isSuccessful) {
                    message = "✅ Rol actualizado correctamente"
                    cargarDatos()
                } else {
                    message = "❌ Error al actualizar"
                }
            } catch (e: Exception) {
                message = "⚠️ Fallo: ${e.message}"
            }
        }
    }

    fun eliminarUsuario(id: Int) {
        if (id == Session.userId) {
            message = "❌ No puedes eliminarte a ti mismo"
            return
        }
        scope.launch {
            try {
                val response = RetrofitClient.api.deleteUser(Session.bearer(), id)
                if (response.isSuccessful) {
                    message = "✅ Usuario eliminado"
                    cargarDatos()
                } else {
                    message = "❌ Error ${response.code()}"
                }
            } catch (e: Exception) {
                message = "⚠️ Error: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Sección Estadísticas
                stats?.let { s ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen Global", fontWeight = FontWeight.Bold)
                            Text("Total: ${s.total_users ?: 0} usuarios", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Distribución de roles en 2 columnas para que se vea estético
                            s.roles_distribution?.chunked(2)?.forEach { rowRoles ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowRoles.forEach { r ->
                                        Text(
                                            text = "${r.role?.TypeRole}: ${r.user_count}",
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (rowRoles.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Formulario de Creación
                Text("Crear Nuevo Usuario", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())

                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedRoles,
                        onExpandedChange = { expandedRoles = !expandedRoles }
                    ) {
                        OutlinedTextField(
                            value = selectedRole.first,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol a asignar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoles) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRoles,
                            onDismissRequest = { expandedRoles = false }
                        ) {
                            rolesOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.first) },
                                    onClick = {
                                        selectedRole = option
                                        expandedRoles = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
                            message = "❌ Completa los campos"
                            return@Button
                        }
                        isCreating = true
                        scope.launch {
                            try {
                                val reg = RetrofitClient.api.register(RegisterRequest(nombre, email, password, 1))
                                if (reg.isSuccessful) {
                                    val id = reg.body()?.user?.id
                                    if (id != null) {
                                        RetrofitClient.api.updateRoles(Session.bearer(), id, UpdateRolesRequest(listOf(selectedRole.second)))
                                        message = "✅ Usuario '${selectedRole.first}' creado"
                                        nombre = ""; email = ""; password = ""
                                        cargarDatos()
                                    }
                                }
                            } catch (e: Exception) { message = "Fallo: ${e.message}" }
                            finally { isCreating = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating
                ) {
                    if (isCreating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Crear Usuario")
                }

                if (message.isNotEmpty()) {
                    Text(message, color = Color.Blue, modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buscador
                Text("Lista de Usuarios", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar...") },
                    trailingIcon = { Icon(Icons.Default.Search, null) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (loading) {
                item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else {
                val filteredUsers = users.filter { 
                    it.UserName.contains(searchQuery, ignoreCase = true) || 
                    it.Email.contains(searchQuery, ignoreCase = true) 
                }
                
                items(filteredUsers) { user ->
                    UserCard(user, rolesOptions, 
                        onRoleChange = { roleId -> cambiarRol(user.id, roleId) },
                        onDelete = { eliminarUsuario(user.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, roles: List<Pair<String, Int>>, onRoleChange: (Int) -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.UserName, fontWeight = FontWeight.Bold)
                    Text(user.Email, fontSize = 12.sp, color = Color.Gray)
                    val currentRoles = user.roles?.joinToString { it.TypeRole } ?: "Sin rol"
                    Text("Rol actual: $currentRoles", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                Row {
                    // Botón Eliminar
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }

                    // Menú para cambiar rol
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Cambiar Rol", tint = Color.Gray)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            Text("Asignar nuevo rol:", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                            roles.forEach { (name, id) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        onRoleChange(id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
