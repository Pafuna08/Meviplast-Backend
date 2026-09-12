package com.meviplast.app.data

import com.google.gson.annotations.SerializedName

// ==========================================
// AUTENTICACIÓN
// ==========================================

data class LoginRequest(
    val Email: String,
    val PasswoRDkey: String
)

data class LoginResponse(
    val message: String?,
    val token: String?,
    val user: User?
)

data class RegisterRequest(
    val UserName: String,
    val Email: String,
    val PasswoRDkey: String,
    val iD_City: Int = 1
)

data class RegisterResponse(
    val message: String?,
    val user: User?,
    val token: String?
)

data class User(
    @SerializedName("iD_User") val id: Int,
    val UserName: String,
    val Email: String,
    val roles: List<Role>? = null
)

data class Role(
    val iDRole: Int,
    val TypeRole: String
)

// ==========================================
// INVENTARIO (MATERIAS PRIMAS Y PRODUCTOS)
// ==========================================

data class Material(
    val iD_Material: Int,
    val MaterialName: String,
    val Quantity: Double,
    val Unit: String
)

data class Product(
    @SerializedName("id_Product") val iD_Product: Int,
    val ProductName: String,
    val Price: Double,
    val Stock: Int
)

// ==========================================
// PRODUCCIÓN (TAREAS)
// ==========================================

data class ProductionTask(
    val iD_Task: Int,
    val Description: String,
    val AssignedTo: Int?, // iD_User
    val AssignedToName: String?,
    val Status: String, // Pendiente, En Proceso, Terminada
    val TargetQuantity: Int,
    val ProducedQuantity: Int = 0
)

data class ProductionRecordRequest(
    val iD_Task: Int,
    val Quantity: Int
)

data class AssignTaskRequest(
    val iD_Task: Int,
    val iD_User: Int
)

data class CreateTaskRequest(
    val Description: String,
    val TargetQuantity: Int
)

// ==========================================
// GESTIÓN DE USUARIOS (ADMIN)
// ==========================================

data class ProductsListResponse(
    val products: List<Product>?
)

data class UsersListResponse(
    val users: List<User>?
)

data class UpdateRolesRequest(
    val role_ids: List<Int>
)

data class UserStatsResponse(
    @SerializedName(value = "total_users", alternate = ["total"])
    val total_users: Int?,
    @SerializedName(value = "roles_distribution", alternate = ["roles"])
    val roles_distribution: List<RoleStat>?
)

data class RoleStat(
    val role: Role?,
    @SerializedName(value = "user_count", alternate = ["count"])
    val user_count: Int?
)

// ==========================================
// VENTAS
// ==========================================

data class Sale(
    val id_Sale: Int,
    val DescripcionSale: String?,
    val iD_User: Int,
    val DateCreated: String?,
    val details: List<SaleDetail>? = null,
    val total: Double? = 0.0
)

data class SaleDetail(
    val id_SalesDetails: Int,
    val id_Product: Int,
    val amount: Int,
    val ValueSale: Double,
    val product: Product? = null
)

data class CheckoutRequest(
    val DescripcionSale: String = "Venta desde App"
)

data class CartItem(
    val id_TemporalSales: Int,
    val id_Product: Int,
    val quantity: Int,
    val product: Product?
)

data class CartResponse(
    val cart_items: List<CartItem>,
    val total: Double,
    val count: Int
)

data class AddToCartRequest(
    val id_Product: Int,
    val quantity: Int
)

data class SalesListResponse(
    val sales: List<Sale>
)

// ==========================================
// REPORTES
// ==========================================

data class ReportResponse(
    val production: ProductionReport,
    val sales: SalesReport,
    val inventory: InventoryReport
)

data class ProductionReport(
    val total: Int,
    val finished: Int,
    val in_progress: Int,
    val pending: Int,
    val completion_rate: Double
)

data class SalesReport(
    val total_revenue: Double,
    val count: Int
)

data class InventoryReport(
    val critical_items: Int,
    val low_stock_materials: Int,
    val low_stock_products: Int
)

// ==========================================
// RESPUESTAS GENERALES
// ==========================================

data class MessageResponse(
    val message: String?,
    val error: String?
)

data class ProfileResponse(
    val message: String?,
    val user: User?
)

data class UpdateProfileRequest(
    val UserName: String,
    val Email: String
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)
