package com.meviplast.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meviplast.app.data.Material
import com.meviplast.app.data.Product
import com.meviplast.app.data.RetrofitClient
import com.meviplast.app.data.Session
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(onBack: () -> Unit) {
    var itemsList by remember { mutableStateOf<List<Any>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val showProducts = Session.isVendedor || Session.isAdmin || Session.isSupervisor

    LaunchedEffect(Unit) {
        try {
            // Si es Vendedor, Admin o Supervisor, puede ver productos. 
            // Para simplificar, si es Admin/Supervisor le mostramos Materias Primas por ahora, 
            // a menos que sea Vendedor puro.
            if (Session.isVendedor && !Session.isAdmin) {
                val response = RetrofitClient.api.getProducts(Session.bearer())
                if (response.isSuccessful) {
                    itemsList = response.body()?.products ?: emptyList()
                } else {
                    errorMessage = "Error al cargar productos"
                }
            } else {
                val response = RetrofitClient.api.getMaterials(Session.bearer())
                if (response.isSuccessful) {
                    itemsList = response.body() ?: emptyList()
                } else {
                    errorMessage = "Error al cargar materias primas"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error de conexión: ${e.message}"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (Session.isVendedor && !Session.isAdmin) "Inventario de Productos" else "Inventario Materias Primas") 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (itemsList.isEmpty()) {
                Text(
                    text = "No hay registros disponibles",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itemsList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    if (item is Material) {
                                        Text(text = item.MaterialName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text(text = "Disponible: ${item.Quantity} ${item.Unit}", color = Color.Gray)
                                    } else if (item is Product) {
                                        Text(text = item.ProductName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text(text = "Stock: ${item.Stock} unidades | $${item.Price}", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
