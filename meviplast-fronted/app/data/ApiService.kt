package com.meviplast.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Autenticación ---
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @GET("api/users/profile")
    suspend fun getProfile(@Header("Authorization") auth: String): Response<ProfileResponse>

    @PUT("api/users/profile")
    suspend fun updateProfile(
        @Header("Authorization") auth: String,
        @Body body: UpdateProfileRequest
    ): Response<ProfileResponse>

    @PUT("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") auth: String,
        @Body body: ChangePasswordRequest
    ): Response<MessageResponse>

    // --- Gestión de Usuarios (Admin) ---
    @GET("api/users/")
    suspend fun getUsers(
        @Header("Authorization") auth: String,
        @Query("per_page") perPage: Int = 50
    ): Response<UsersListResponse>

    @GET("api/users/search")
    suspend fun searchUsers(
        @Header("Authorization") auth: String,
        @Query("q") query: String
    ): Response<UsersListResponse>

    @PUT("api/users/{id}/roles")
    suspend fun updateRoles(
        @Header("Authorization") auth: String,
        @Path("id") id: Int,
        @Body body: UpdateRolesRequest
    ): Response<MessageResponse>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    @GET("api/users/stats")
    suspend fun getStats(
        @Header("Authorization") auth: String
    ): Response<UserStatsResponse>

    // --- Inventario ---
    @GET("api/materials")
    suspend fun getMaterials(@Header("Authorization") auth: String): Response<List<Material>>

    @GET("api/products")
    suspend fun getProducts(@Header("Authorization") auth: String): Response<ProductsListResponse>

    // --- Producción ---
    @GET("api/production/tasks")
    suspend fun getTasks(@Header("Authorization") auth: String): Response<List<ProductionTask>>

    @POST("api/production/record")
    suspend fun recordProduction(
        @Header("Authorization") auth: String,
        @Body body: ProductionRecordRequest
    ): Response<MessageResponse>

    @PUT("api/production/assign")
    suspend fun assignTask(
        @Header("Authorization") auth: String,
        @Body body: AssignTaskRequest
    ): Response<MessageResponse>

    @POST("api/production/tasks")
    suspend fun createProductionTask(
        @Header("Authorization") auth: String,
        @Body body: CreateTaskRequest
    ): Response<MessageResponse>

    // --- Ventas ---
    @GET("api/sales/cart")
    suspend fun getCart(@Header("Authorization") auth: String): Response<CartResponse>

    @POST("api/sales/cart/add")
    suspend fun addToCart(
        @Header("Authorization") auth: String,
        @Body body: AddToCartRequest
    ): Response<MessageResponse>

    @POST("api/sales/checkout")
    suspend fun checkout(
        @Header("Authorization") auth: String,
        @Body body: CheckoutRequest
    ): Response<MessageResponse>

    @GET("api/sales/")
    suspend fun getSales(@Header("Authorization") auth: String): Response<SalesListResponse>

    // --- Reportes ---
    @GET("api/reports/stats")
    suspend fun getReportStats(@Header("Authorization") auth: String): Response<ReportResponse>
}
