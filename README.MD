# MEVIPLAST - Backend (Sistema de Gestión Integral)

Este es el servidor oficial de **MEVIPLAST**, desarrollado como proyecto integrador para el **SENA CTMA** (Ficha 3223873). El sistema centraliza la gestión de una planta de plásticos, abarcando desde la recepción de materias primas hasta la venta del producto terminado.

## 🚀 Despliegue en Vivo
La API está actualmente desplegada y operativa en:
**URL:** [https://meviplast-backend.onrender.com](https://meviplast-backend.onrender.com)

---

## 🛠️ Tecnologías y Arquitectura
- **Lenguaje:** Python 3.12+
- **Framework:** Flask (Estructura modular por Blueprints)
- **ORM:** SQLAlchemy (Gestión de base de datos relacional)
- **Seguridad:** Autenticación basada en **JWT** (JSON Web Tokens) y cifrado de contraseñas.
- **Base de Datos:** SQLite (Migrable a PostgreSQL/MySQL)
- **Despliegue:** Optimizado para Render con Gunicorn.

---

## 👥 Jerarquía de Usuarios y Roles
El sistema implementa un control de acceso basado en roles (RBAC) con los siguientes usuarios de prueba (Contraseña: `123456`):

| Nombre | Correo | Rol | Funciones |
| :--- | :--- | :--- | :--- |
| **Pablo Admin** | `Pablo@gmail.com` | Administrador | Gestión total, creación de tareas y usuarios. |
| **Carlos Supervisor** | `carlos@meviplast.com` | Supervisor | Asignación de tareas, monitoreo e inventario. |
| **Juan / Ana** | `juan@meviplast.com` / `Ana@gmail.com` | Operario | Registro de avances de producción. |
| **Maria Almacenista** | `maria@meviplast.com` | Almacenista | Control de materias primas e insumos. |
| **Luis Vendedor** | `luis@meviplast.com` | Vendedor | Catálogo de productos y registro de ventas. |

---

## 📦 Módulos del Sistema
- **⚙️ Producción:** Flujo completo desde la creación de la orden (Admin) hasta la ejecución (Operario) y supervisión (Supervisor).
- **🔋 Inventario:** Separación lógica entre Materias Primas (Insumos) y Productos Terminados (Venta).
- **💰 Ventas:** Sistema de carrito y checkout con actualización automática de stock en tiempo real.
- **📊 Reportes:** Tableros de control con indicadores de cumplimiento de producción, recaudación de ventas y alertas de bajo stock.
- **📍 Ubicaciones:** Gestión de geografía (Países, Estados, Ciudades) para la logística.

---

## 💾 Instalación y Desarrollo Local
1. **Clonar Repositorio:**
   ```bash
   git clone https://github.com/Pafuna08/Meviplast-Backend.git
   cd Meviplast-Backend
   ```
2. **Entorno Virtual:**
   ```bash
   python -m venv venv
   source venv/bin/activate  # o venv\Scripts\activate en Windows
   ```
3. **Dependencias:**
   ```bash
   pip install -r requirement.txt
   ```
4. **Ejecución:**
   ```bash
   python app.py
   ```

---
**Proyecto Integrador - SENA CTMA 2026**
*Integrantes:* Pablo Fuenmayor Osuna y Carlos Herrera Sibaja.
