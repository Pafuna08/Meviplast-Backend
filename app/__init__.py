from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
import os
from dotenv import load_dotenv

# Cargar variables de entorno
load_dotenv()

# Inicializar extensiones
db = SQLAlchemy()

def create_app():
    app = Flask(__name__)
    
    # Obtener la ruta absoluta para evitar errores de espacios en Windows
    basedir = os.path.abspath(os.path.dirname(__file__))
    db_path = os.path.join(os.path.dirname(basedir), 'database', 'meviplast.db')

    # Configuración
    app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', 'HolaMundo')
    app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + db_path
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['JSON_AS_ASCII'] = False  # Para caracteres especiales en JSON
    
    # Inicializar extensiones con la app
    db.init_app(app)
    CORS(app)  # Permitir CORS para frontend
    
    # Registrar blueprints
    from app.routes.auth_routes import auth_bp
    from app.routes.product_routes import product_bp
    from app.routes.user_routes import user_bp
    from app.routes.sales_routes import sales_bp
    from app.routes.category_routes import category_bp
    from app.routes.location_routes import location_bp
    from app.routes.material_routes import material_bp
    from app.routes.production_routes import production_bp
    from app.routes.report_routes import report_bp

    app.register_blueprint(auth_bp, url_prefix='/api/auth')
    app.register_blueprint(product_bp, url_prefix='/api/products')
    app.register_blueprint(user_bp, url_prefix='/api/users')
    app.register_blueprint(sales_bp, url_prefix='/api/sales')
    app.register_blueprint(category_bp, url_prefix='/api/categories')
    app.register_blueprint(location_bp, url_prefix='/api/locations')
    app.register_blueprint(material_bp, url_prefix='/api/materials')
    app.register_blueprint(production_bp, url_prefix='/api/production')
    app.register_blueprint(report_bp, url_prefix='/api/reports')

    # Ruta de prueba
    @app.route('/')
    def home():
        return {
            'message': 'API MEVIPLAST funcionando en puerto 5060',
            'version': '1.0',
            'endpoints': {
                'auth': '/api/auth',
                'products': '/api/products', 
                'users': '/api/users',
                'sales': '/api/sales',
                'categories': '/api/categories',
                'locations': '/api/locations'
            }
        }
    
    # Crear tablas y sembrar datos iniciales si es necesario
    with app.app_context():
        db.create_all()

        from app.models import RoleS, Users, Material, Product, ProductionTask, Sales, SalesDetail
        from datetime import datetime, timedelta

        if RoleS.query.count() == 0:
            # 1. Crear Roles
            roles_names = ['Administrador', 'Supervisor', 'Operario', 'Almacenista', 'Vendedor']
            for name in roles_names:
                db.session.add(RoleS(TypeRole=name))
            db.session.commit()

            # 2. Crear Usuarios de Prueba
            users_data = [
                ('Pablo Admin', 'Pablo@gmail.com', 'Administrador'),
                ('Carlos Supervisor', 'carlos@meviplast.com', 'Supervisor'),
                ('Juan Operario', 'juan@meviplast.com', 'Operario'),
                ('Ana Operario', 'Ana@gmail.com', 'Operario'),
                ('Maria Almacenista', 'maria@meviplast.com', 'Almacenista'),
                ('Luis Vendedor', 'luis@meviplast.com', 'Vendedor')
            ]

            for name, email, role_name in users_data:
                if not Users.query.filter_by(Email=email).first():
                    u = Users(UserName=name, Email=email)
                    u.set_password('123456')
                    role = RoleS.query.filter_by(TypeRole=role_name).first()
                    if role:
                        u.roles.append(role)
                    db.session.add(u)
            db.session.commit()

            # 3. Materias Primas
            if not Material.query.first():
                db.session.add_all([
                    Material(MaterialName='Polietileno (HDPE)', Quantity=500.5, Unit='kg'),
                    Material(MaterialName='Polipropileno (PP)', Quantity=250.0, Unit='kg'),
                    Material(MaterialName='Pigmento Azul', Quantity=10.0, Unit='kg'),
                    Material(MaterialName='Recuperado Molido', Quantity=800.0, Unit='kg')
                ])

            # 4. Productos Terminados
            if not Product.query.first():
                db.session.add_all([
                    Product(ProductName='Caneca Plástica 20L', Price=15000.0, Stock=150),
                    Product(ProductName='Envase Industrial 1L', Price=2500.0, Stock=2000),
                    Product(ProductName='Tapa de Seguridad 38mm', Price=150.0, Stock=10000),
                    Product(ProductName='Caja Agrícola Calada', Price=12000.0, Stock=80)
                ])
            db.session.commit()

            # 5. Tareas de Producción
            if not ProductionTask.query.first():
                db.session.add_all([
                    ProductionTask(Description='Fabricación 1000 envases 1L', Status='Pendiente', TargetQuantity=1000),
                    ProductionTask(Description='Inyección de 5000 tapas', Status='En Proceso', TargetQuantity=5000, ProducedQuantity=2500),
                    ProductionTask(Description='Molienda de purga (HDPE)', Status='Terminada', TargetQuantity=200, ProducedQuantity=200)
                ])

            # 6. Ventas Históricas
            if not Sales.query.first():
                vendedor = Users.query.join(Users.roles).filter(RoleS.TypeRole == 'Vendedor').first()
                prod1 = Product.query.filter_by(ProductName='Caneca Plástica 20L').first()

                if vendedor and prod1:
                    for i in range(5):
                        sale = Sales(
                            iD_User=vendedor.iD_User,
                            DescripcionSale=f"Venta de ejemplo #{i+1}",
                            DateCreated=datetime.utcnow() - timedelta(days=i)
                        )
                        db.session.add(sale)
                        db.session.flush()

                        detail = SalesDetail(
                            id_Product=prod1.id_Product,
                            id_Sale=sale.id_Sale,
                            amount=2,
                            ValueSale=prod1.Price * 2,
                            DateSales=sale.DateCreated
                        )
                        db.session.add(detail)

            db.session.commit()

    return app

    return app
