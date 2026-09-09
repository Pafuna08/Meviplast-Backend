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
    
    # Crear tablas si no existen
    with app.app_context():
        db.create_all()
    
    return app