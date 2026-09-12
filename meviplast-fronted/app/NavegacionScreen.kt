package com.meviplast.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meviplast.app.data.DataStoreManager
import com.meviplast.app.data.RetrofitClient
import com.meviplast.app.data.Session

@Composable
fun NavegacionScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val savedToken by dataStoreManager.getToken.collectAsState(initial = null)

    // Auto-login (Reto de la Guía 4 integrado desde el inicio)
    LaunchedEffect(savedToken) {
        if (!savedToken.isNullOrEmpty() && Session.token == null) {
            try {
                val response = RetrofitClient.api.getProfile("Bearer $savedToken")
                if (response.isSuccessful) {
                    Session.startSession(savedToken, response.body()?.user)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { 
            LoginScreen(navController) 
        }
        composable("register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") { 
            HomeScreen(navController) 
        }
        composable("inventory") { 
            InventoryScreen(onBack = { navController.popBackStack() }) 
        }
        composable("production") { 
            ProductionScreen(onBack = { navController.popBackStack() }) 
        }-
        composable("sales") {
            SalesScreen(onBack = { navController.popBackStack() })
        }
        composable("reports") {
            ReportsScreen(onBack = { navController.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable("users") {
            UsersScreen(onBack = { navController.popBackStack() })
        }
    }
}

