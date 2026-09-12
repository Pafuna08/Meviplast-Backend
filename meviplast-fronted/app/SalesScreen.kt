package com.meviplast.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
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
fun SalesScreen(onBack: () -> Unit) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var cartTotal by remember { mutableDoubleStateOf(0.0) }
    var loading by remember { mutableStateOf(true) }
    var showCart by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun loadProducts() {
        scope.launch {
            try {
                val response = RetrofitClient.api.getProducts(Session.bearer())
                if (response.isSuccessful) {
                    products = response.body()?.products ?: emptyList()
                }
            } catch (_: Exception) {}
        }
    }

    fun loadCart() {
        scope.launch {
            try {
                val response = RetrofitClient.api.getCart(Session.bearer())
                if (response.isSuccessful) {
                    val data = response.body()
                    cartItems = data?.cart_items ?: emptyList()
                    cartTotal = data?.total ?: 0.0
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadProducts()
        loadCart()
        loading = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registro de Ventas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge { Text(cartItems.size.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { showCart = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (products.isEmpty()) {
                Text(text = "No hay productos disponibles para la venta", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductSaleCard(
                            product = product,
                            onAddToCart = {
                                scope.launch {
                                    val response = RetrofitClient.api.addToCart(
                                        Session.bearer(),
                                        AddToCartRequest(product.iD_Product, 1)
                                    )
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar("Agregado al carrito")
                                        loadCart()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCart) {
        ModalBottomSheet(
            onDismissRequest = { showCart = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(text = "Tu Carrito", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (cartItems.isEmpty()) {
                    Text(text = "El carrito está vacío", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(cartItems) { item ->
                            ListItem(
                                headlineContent = { Text(item.product?.ProductName ?: "Producto") },
                                supportingContent = { Text("Cantidad: ${item.quantity}") },
                                trailingContent = { Text("$${(item.product?.Price ?: 0.0) * item.quantity}") }
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total:", fontWeight = FontWeight.Bold)
                        Text(text = "$${cartTotal}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                val response = RetrofitClient.api.checkout(
                                    Session.bearer(),
                                    CheckoutRequest("Venta realizada por ${Session.userName}")
                                )
                                if (response.isSuccessful) {
                                    snackbarHostState.showSnackbar("Venta registrada con éxito")
                                    showCart = false
                                    loadCart()
                                    loadProducts() // Para actualizar stock
                                } else {
                                    snackbarHostState.showSnackbar("Error al procesar la venta")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finalizar Venta")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductSaleCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.ProductName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "$${product.Price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(text = "Stock: ${product.Stock}", fontSize = 12.sp, color = if (product.Stock > 0) Color.Gray else Color.Red)
            }
            IconButton(
                onClick = onAddToCart,
                enabled = product.Stock > 0
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
            }
        }
    }
}
