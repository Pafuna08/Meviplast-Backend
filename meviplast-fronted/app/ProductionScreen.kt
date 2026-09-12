package com.meviplast.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meviplast.app.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(onBack: () -> Unit) {
    var tasks by remember { mutableStateOf<List<ProductionTask>>(emptyList()) }
    var operarios by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    var selectedTaskForRecord by remember { mutableStateOf<ProductionTask?>(null) }
    var selectedTaskForAssign by remember { mutableStateOf<ProductionTask?>(null) }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isAdminOrSuper = Session.isAdmin || Session.isSupervisor

    fun loadData() {
        scope.launch {
            loading = true
            try {
                val respTasks = RetrofitClient.api.getTasks(Session.bearer())
                if (respTasks.isSuccessful) tasks = respTasks.body() ?: emptyList()
                
                if (isAdminOrSuper) {
                    val respUsers = RetrofitClient.api.getUsers(Session.bearer())
                    if (respUsers.isSuccessful) {
                        operarios = respUsers.body()?.users?.filter { u -> 
                            u.roles?.any { r -> r.TypeRole == "Operario" } == true 
                        } ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    // Diálogo: Crear Tarea (Solo Admin)
    if (showCreateTaskDialog) {
        var desc by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showCreateTaskDialog = false },
            title = { Text("Nueva Orden de Producción") },
            text = {
                Column {
                    OutlinedTextField(desc, { desc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        qty, { qty = it }, 
                        label = { Text("Cantidad Objetivo") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = qty.toIntOrNull()
                        if (desc.isNotBlank() && target != null) {
                            scope.launch {
                                isSaving = true
                                try {
                                    val resp = RetrofitClient.api.createProductionTask(Session.bearer(), CreateTaskRequest(desc, target))
                                    if (resp.isSuccessful) {
                                        snackbarHostState.showSnackbar("Tarea creada")
                                        showCreateTaskDialog = false
                                        loadData()
                                    }
                                } catch (_: Exception) {} finally { isSaving = false }
                            }
                        }
                    },
                    enabled = !isSaving
                ) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showCreateTaskDialog = false }) { Text("Cancelar") } }
        )
    }

    // Diálogo: Registrar Avance
    if (selectedTaskForRecord != null) {
        var quantityText by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) selectedTaskForRecord = null },
            title = { Text("Registrar Avance") },
            text = {
                Column {
                    Text("Tarea: ${selectedTaskForRecord!!.Description}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Cantidad producida") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityText.toIntOrNull()
                        if (qty != null && qty > 0) {
                            scope.launch {
                                isSubmitting = true
                                try {
                                    val response = RetrofitClient.api.recordProduction(
                                        Session.bearer(),
                                        ProductionRecordRequest(selectedTaskForRecord!!.iD_Task, qty)
                                    )
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar("Producción registrada")
                                        selectedTaskForRecord = null
                                        loadData()
                                    }
                                } catch (_: Exception) {} finally { isSubmitting = false }
                            }
                        }
                    }
                ) { Text("Registrar") }
            },
            dismissButton = { TextButton(onClick = { selectedTaskForRecord = null }) { Text("Cancelar") } }
        )
    }

    // Diálogo: Asignar Operario
    if (selectedTaskForAssign != null) {
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) selectedTaskForAssign = null },
            title = { Text("Asignar Responsable") },
            text = {
                Column {
                    Text("Tarea: ${selectedTaskForAssign!!.Description}")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (operarios.isEmpty()) {
                        Text("No hay operarios registrados en el sistema", color = Color.Red)
                    } else {
                        Text("Seleccione un operario:", fontSize = 12.sp, color = Color.Gray)
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(operarios) { operario ->
                                ListItem(
                                    headlineContent = { Text(operario.UserName) },
                                    supportingContent = { Text(operario.Email) },
                                    trailingContent = {
                                        RadioButton(
                                            selected = selectedTaskForAssign!!.AssignedTo == operario.id,
                                            onClick = {
                                                scope.launch {
                                                    isSubmitting = true
                                                    try {
                                                        val resp = RetrofitClient.api.assignTask(
                                                            Session.bearer(),
                                                            AssignTaskRequest(selectedTaskForAssign!!.iD_Task, operario.id)
                                                        )
                                                        if (resp.isSuccessful) {
                                                            snackbarHostState.showSnackbar("Tarea asignada con éxito")
                                                            selectedTaskForAssign = null
                                                            loadData()
                                                        }
                                                    } catch (_: Exception) {} finally { isSubmitting = false }
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedTaskForAssign = null }) { Text("Cerrar") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isAdminOrSuper) "Gestión de Planta" else "Mis Tareas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (Session.isAdmin) {
                FloatingActionButton(onClick = { showCreateTaskDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Tarea")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading && tasks.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (tasks.isEmpty()) {
                Text(text = if (isAdminOrSuper) "No hay tareas registradas" else "No tienes tareas asignadas", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        ProductionTaskCard(
                            task = task,
                            showAssign = isAdminOrSuper,
                            canRecord = Session.isOperario,
                            onRecordClick = { selectedTaskForRecord = task },
                            onAssignClick = { selectedTaskForAssign = task }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionTaskCard(
    task: ProductionTask, 
    showAssign: Boolean, 
    canRecord: Boolean,
    onRecordClick: () -> Unit,
    onAssignClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PrecisionManufacturing,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = task.Description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Badge(
                    containerColor = when (task.Status) {
                        "Terminada" -> Color(0xFF4CAF50)
                        "En Proceso" -> Color(0xFFFF9800)
                        "Pendiente" -> Color.Gray
                        else -> Color.DarkGray
                    }
                ) {
                    Text(task.Status, color = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val progressValue = if (task.TargetQuantity > 0) task.ProducedQuantity.toFloat() / task.TargetQuantity else 0f
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Progreso: ${task.ProducedQuantity} / ${task.TargetQuantity}", fontSize = 13.sp)
                    if (showAssign) {
                        Text(
                            text = if (task.AssignedToName != null) "Asignado a: ${task.AssignedToName}" else "Sin asignar",
                            fontSize = 11.sp,
                            color = if (task.AssignedToName != null) Color.Gray else Color.Red,
                            fontWeight = if (task.AssignedToName != null) FontWeight.Normal else FontWeight.Bold
                        )
                    }
                }
                
                Row {
                    if (showAssign && task.Status == "Pendiente") {
                        IconButton(onClick = onAssignClick) {
                            Icon(Icons.Default.AssignmentInd, contentDescription = "Asignar", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    if (canRecord && task.Status != "Terminada") {
                        TextButton(onClick = onRecordClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Avance")
                        }
                    }
                }
            }
        }
    }
}

