package com.meviplast.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meviplast.app.data.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit) {
    var reportData by remember { mutableStateOf<ReportResponse?>(null) }
    var salesList by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showSalesDetail by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            loading = true
            try {
                val respReport = RetrofitClient.api.getReportStats(Session.bearer())
                val respSales = RetrofitClient.api.getSales(Session.bearer())
                
                if (respReport.isSuccessful) reportData = respReport.body()
                if (respSales.isSuccessful) salesList = respSales.body()?.sales ?: emptyList()
                
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes Globales") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage.isNotEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = errorMessage, color = Color.Red)
                    Button(onClick = { loadData() }) { Text("Reintentar") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ReportSection(
                            title = "Rendimiento de Producción",
                            icon = Icons.Default.PrecisionManufacturing,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            reportData?.production?.let { p ->
                                ReportItem("Total Tareas", p.total.toString())
                                ReportItem("Terminadas", p.finished.toString())
                                ReportItem("En Proceso", p.in_progress.toString())
                                ReportItem("Pendientes", p.pending.toString())
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tasa de Cumplimiento: ${String.format("%.1f", p.completion_rate)}%", fontWeight = FontWeight.Bold)
                                LinearProgressIndicator(
                                    progress = { (p.completion_rate / 100).toFloat() },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    item {
                        ReportSection(
                            title = "Resumen de Ventas",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            color = Color(0xFF4CAF50)
                        ) {
                            reportData?.sales?.let { s ->
                                ReportItem("Total Recaudado", "$${String.format("%.2f", s.total_revenue)}")
                                
                                Surface(
                                    onClick = { showSalesDetail = true },
                                    color = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Número de Ventas", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = s.count.toString(), fontWeight = FontWeight.Bold)
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ReportSection(
                            title = "Estado de Inventario",
                            icon = Icons.Default.Inventory,
                            color = Color(0xFFFF9800)
                        ) {
                            reportData?.inventory?.let { i ->
                                ReportItem("Alertas Bajo Stock", i.critical_items.toString(), isAlert = i.critical_items > 0)
                                if (i.critical_items > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Se requiere reabastecimiento urgente", color = Color.Red, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSalesDetail) {
        ModalBottomSheet(
            onDismissRequest = { showSalesDetail = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Detalle de Ventas Históricas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (salesList.isEmpty()) {
                    Text(text = "No hay registros de ventas", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(salesList) { sale ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "ID: #${sale.id_Sale}", fontWeight = FontWeight.Bold)
                                        Text(text = "$${String.format(Locale.getDefault(), "%.2f", sale.total)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = "Fecha: ${sale.DateCreated?.take(10) ?: "N/A"}", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "Nota: ${sale.DescripcionSale ?: "Sin descripción"}", fontSize = 12.sp)
                                    
                                    // Detalle de productos vendidos
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Productos:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    sale.details?.forEach { detail ->
                                        Text(
                                            text = "• ${detail.amount}x ${detail.product?.ProductName ?: "Producto"}",
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
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

@Composable
fun ReportSection(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}

@Composable
fun ReportItem(label: String, value: String, isAlert: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) Color.Red else Color.Unspecified
        )
    }
}
