package com.meviplast.app.data

object Session {
    var token: String? = null
    var userId: Int? = null
    var userName: String? = null
    var roles: List<String> = emptyList()

    val isAdmin: Boolean get() = roles.contains("Administrador")
    val isSupervisor: Boolean get() = roles.contains("Supervisor")
    val isOperario: Boolean get() = roles.contains("Operario")
    val isAlmacenista: Boolean get() = roles.contains("Almacenista")
    val isVendedor: Boolean get() = roles.contains("Vendedor")

    fun startSession(token: String?, user: User?) {
        this.token = token
        this.userId = user?.id
        this.userName = user?.UserName
        this.roles = user?.roles?.map { it.TypeRole } ?: emptyList()
    }

    fun bearer(): String = "Bearer ${token ?: ""}"

    fun clear() {
        token = null
        userId = null
        userName = null
        roles = emptyList()
    }
}
