# MEVIPLAST Backend

API REST para la gestion de MEVIPLAST, una empresa dedicada a la fabricacion y comercializacion de productos plasticos. El backend centraliza usuarios, roles, ubicaciones, materias primas, productos terminados, produccion, ventas y reportes.

Construido con Flask, SQLAlchemy y SQLite. La API utiliza JWT para proteger los recursos autenticados y CORS para comunicarse con un frontend.

## 🚀 Despliegue en Render
La API está actualmente desplegada y disponible en:
**URL:** [https://meviplast-backend.onrender.com](https://meviplast-backend.onrender.com)

## Funcionalidades

- Autenticacion y autorizacion mediante JWT.
- Gestion de usuarios y roles: Administrador, Supervisor, Operario, Almacenista y Vendedor.
- Catalogo de productos terminados con stock, precios, imagenes y categorias.
- Gestion de materias primas.
- Registro y seguimiento de tareas de produccion.
- Carrito de compras, checkout y consulta de ventas.
- Catalogo geografico de paises, departamentos/estados y ciudades.
- Estadisticas para usuarios, categorias, ventas y reportes.

## Tecnologias

- Python 3
- Flask 2.3.3
- Flask-SQLAlchemy 3.0.5
- Flask-CORS 4.0.0
- PyJWT 2.8.0
- SQLite

## Estructura del proyecto

```text
Meviplast-Backend/
├── app.py                         # Punto de entrada de Flask
├── wsgi.py                        # Punto de entrada para Gunicorn/Render
├── requirement.txt                # Dependencias del proyecto
├── seed_meviplast.py              # Datos iniciales completos
├── ApiDocumentacion.MD            # Coleccion de ejemplos cURL
└── app/
    ├── __init__.py                # Configuracion y registro de blueprints
    ├── models.py                  # Modelos SQLAlchemy
    └── routes/
        ├── auth_routes.py         # Autenticacion y roles
        ├── material_routes.py     # Materias primas
        ├── product_routes.py      # Productos e inventario
        ├── production_routes.py   # Tareas de produccion
        ├── report_routes.py       # Reportes
        ├── sales_routes.py        # Carrito y ventas
        └── user_routes.py         # Usuarios
```

## Instalacion

### 1. Clonar el repositorio

```bash
git clone https://github.com/Pafuna08/Meviplast-Backend.git
cd Meviplast-Backend
```

### 2. Crear y activar un entorno virtual

En Windows PowerShell:

```powershell
python -m venv venv
venv\Scripts\Activate.ps1
```

### 3. Instalar dependencias

```bash
pip install -r requirement.txt
```

## Datos de prueba

Para cargar roles, usuarios, materias primas, productos, tareas de produccion y ventas de ejemplo:

```bash
python seed_meviplast.py
```

`seed_meviplast.py` crea estos usuarios de prueba. Todos usan la contrasena `123456`:

| Usuario           | Correo                 | Rol           |
| ----------------- | ---------------------- | ------------- |
| Pablo Admin       | `Pablo@gmail.com`      | Administrador |
| Carlos Supervisor | `carlos@meviplast.com` | Supervisor    |
| Juan Operario     | `juan@meviplast.com`   | Operario      |
| Ana Operario      | `Ana@gmail.com`        | Operario      |
| Maria Almacenista | `maria@meviplast.com`  | Almacenista   |
| Luis Vendedor     | `luis@meviplast.com`   | Vendedor      |

---
**Proyecto Integrador - SENA CTMA 2026**
*Integrantes:* Pablo Fuenmayor Osuna y Carlos Herrera Sibaja.
