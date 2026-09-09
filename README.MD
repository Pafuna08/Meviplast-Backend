# MEVIPLAST Backend

API REST para la gestion de MEVIPLAST, una empresa dedicada a la fabricacion y comercializacion de productos plasticos. El backend centraliza usuarios, roles, ubicaciones, materias primas, productos terminados, produccion, ventas y reportes.

Construido con Flask, SQLAlchemy y SQLite. La API utiliza JWT para proteger los recursos autenticados y CORS para comunicarse con un frontend.

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
├── change.py                      # Utilidad de cambios o migraciones
├── requirement.txt                # Dependencias del proyecto
├── seed_meviplast.py              # Datos iniciales completos
├── seed_production.py             # Datos de materias primas y produccion
├── ApiDocumentacion.MD            # Coleccion de ejemplos cURL
├── database/
│   └── meviplast_init.sql         # Script SQL de inicializacion
└── app/
    ├── __init__.py                # Configuracion y registro de blueprints
    ├── models.py                  # Modelos SQLAlchemy
    └── routes/
        ├── auth_routes.py         # Autenticacion y roles
        ├── category_routes.py     # Categorias
        ├── location_routes.py     # Ubicaciones
        ├── material_routes.py     # Materias primas
        ├── product_routes.py      # Productos e inventario
        ├── production_routes.py   # Tareas de produccion
        ├── report_routes.py       # Reportes
        ├── sales_routes.py        # Carrito y ventas
        └── user_routes.py         # Usuarios
```

> El archivo SQLite `database/meviplast.db` se crea localmente al iniciar la aplicacion y no se versiona en Git.

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

En Linux o macOS:

```bash
python3 -m venv venv
source venv/bin/activate
```

### 3. Instalar dependencias

```bash
pip install -r requirement.txt
```

### 4. Configurar variables de entorno

El proyecto funciona con valores predeterminados, pero se recomienda crear un archivo `.env`:

```env
SECRET_KEY=una-clave-segura-para-desarrollo
```

No subas el archivo `.env` al repositorio. La aplicacion usa SQLite en `database/meviplast.db` y crea las tablas automaticamente al iniciar.

## Ejecucion

```bash
python app.py
```

La API queda disponible en `http://localhost:5060`.

Puedes comprobar que esta funcionando con:

```bash
curl http://localhost:5060/
```

## Datos de prueba

Para cargar roles, usuarios, materias primas, productos, tareas de produccion y ventas de ejemplo:

```bash
python seed_meviplast.py
```

Para cargar un conjunto adicional de materias primas y tareas de produccion:

```bash
python seed_production.py
```

`seed_meviplast.py` crea estos usuarios de prueba. Todos usan la contrasena `123456`:

| Usuario | Correo | Rol |
| --- | --- | --- |
| Pablo Admin | `Pablo@gmail.com` | Administrador |
| Carlos Supervisor | `carlos@meviplast.com` | Supervisor |
| Juan Operario | `juan@meviplast.com` | Operario |
| Maria Almacenista | `maria@meviplast.com` | Almacenista |
| Luis Vendedor | `luis@meviplast.com` | Vendedor |

Estos datos son solo para desarrollo. Cambia las contrasenas antes de usar la API en un entorno real.

## Autenticacion

Primero inicia sesion para obtener un token JWT:

```bash
curl -X POST http://localhost:5060/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "Email": "Pablo@gmail.com",
    "PasswoRDkey": "123456"
  }'
```

En los endpoints protegidos envia el token en el encabezado:

```text
Authorization: Bearer TU_TOKEN
```

## Endpoints

Todos los endpoints usan el prefijo `http://localhost:5060`.

### Autenticacion: `/api/auth`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| POST | `/register` | Registrar usuario |
| POST | `/login` | Iniciar sesion |
| POST | `/verify-token` | Verificar token |
| GET | `/roles` | Listar roles |
| PUT | `/change-password` | Cambiar contrasena |

### Usuarios: `/api/users`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET, PUT | `/profile` | Consultar o actualizar el perfil propio |
| GET | `/` | Listar usuarios |
| GET | `/<user_id>` | Consultar usuario |
| PUT | `/<user_id>/roles` | Actualizar roles |
| DELETE | `/<user_id>` | Eliminar usuario |
| GET | `/<user_id>/sales` | Consultar ventas del usuario |
| GET | `/stats` | Estadisticas de usuarios |
| GET | `/search` | Buscar usuarios |

### Productos: `/api/products`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/` | Listar productos |
| GET | `/<product_id>` | Consultar producto |
| POST | `/` | Crear producto |
| PUT | `/<product_id>` | Actualizar producto |
| DELETE | `/<product_id>` | Eliminar producto |
| POST | `/<product_id>/images` | Agregar imagen |
| DELETE | `/<product_id>/images/<image_id>` | Eliminar imagen |
| PUT | `/<product_id>/stock` | Actualizar stock |
| GET | `/search` | Buscar productos |
| GET | `/featured` | Listar productos destacados |

### Categorias: `/api/categories`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/` | Listar categorias |
| GET | `/<category_id>` | Consultar categoria |
| POST | `/` | Crear categoria |
| PUT | `/<category_id>` | Actualizar categoria |
| DELETE | `/<category_id>` | Eliminar categoria |
| GET | `/<category_id>/products` | Productos de una categoria |
| GET | `/stats` | Estadisticas de categorias |

### Materias primas: `/api/materials`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/` | Listar materias primas |

### Produccion: `/api/production`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/tasks` | Listar tareas de produccion |
| POST | `/tasks` | Crear tarea |
| PUT | `/assign` | Asignar tarea |
| POST | `/record` | Registrar produccion realizada |

### Ventas y carrito: `/api/sales`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/cart` | Consultar carrito |
| POST | `/cart/add` | Agregar producto al carrito |
| PUT | `/cart/update/<item_id>` | Actualizar cantidad |
| DELETE | `/cart/remove/<item_id>` | Retirar producto |
| DELETE | `/cart/clear` | Vaciar carrito |
| POST | `/checkout` | Crear venta desde el carrito |
| GET | `/` | Listar ventas |
| GET | `/<sale_id>` | Consultar venta |
| GET | `/stats` | Estadisticas de ventas |

### Ubicaciones: `/api/locations`

Incluye operaciones de consulta y creacion para paises, estados/departamentos y ciudades:

- `GET, POST /countries`
- `GET /countries/<country_id>`
- `GET, POST /states`
- `GET /states/<state_id>`
- `GET /countries/<country_id>/states`
- `GET, POST /cities`
- `GET /cities/<city_id>`
- `GET /states/<state_id>/cities`
- `GET /countries/<country_id>/cities`
- `GET /hierarchy`
- `GET /search`

### Reportes: `/api/reports`

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/stats` | Consultar estadisticas generales |

## Paginacion y filtros

Los endpoints de listado que lo soportan aceptan parametros como:

- `page`: numero de pagina.
- `per_page`: cantidad de resultados por pagina.
- `search`: texto de busqueda.
- Filtros propios de cada recurso.

Consulta `ApiDocumentacion.MD` para ver ejemplos de peticiones cURL y cuerpos JSON.

## Codigos de respuesta comunes

| Codigo | Significado |
| --- | --- |
| 200 | Peticion exitosa |
| 201 | Recurso creado |
| 400 | Datos invalidos |
| 401 | Falta autenticacion o el token no es valido |
| 403 | El usuario no tiene permisos |
| 404 | Recurso no encontrado |
| 409 | Conflicto o registro duplicado |
| 500 | Error interno del servidor |

## Desarrollo

Antes de subir cambios:

```bash
git status
git add .
git commit -m "Describe el cambio"
git push origin main
```

El proyecto ignora el entorno virtual, archivos `.env`, caches de Python, logs y bases de datos locales.
