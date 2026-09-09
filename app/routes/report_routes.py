from flask import Blueprint, jsonify
from app import db
from app.models import Sales, SalesDetail, ProductionTask, Material, Product
from sqlalchemy import func

report_bp = Blueprint('reports', __name__)

@report_bp.route('/stats', methods=['GET'])
def get_report_stats():
    """Estadísticas consolidadas para el reporte global"""
    try:
        # 1. Producción
        total_tasks = ProductionTask.query.count()
        finished_tasks = ProductionTask.query.filter_by(Status='Terminada').count()
        in_progress_tasks = ProductionTask.query.filter_by(Status='En Proceso').count()
        pending_tasks = ProductionTask.query.filter_by(Status='Pendiente').count()

        # 2. Ventas
        total_revenue = db.session.query(func.sum(SalesDetail.ValueSale)).scalar() or 0
        total_sales_count = Sales.query.count()

        # 3. Inventario Crítico (Stock < 50)
        low_stock_materials = Material.query.filter(Material.Quantity < 50).count()
        low_stock_products = Product.query.filter(Product.Stock < 50).count()

        return jsonify({
            'production': {
                'total': total_tasks,
                'finished': finished_tasks,
                'in_progress': in_progress_tasks,
                'pending': pending_tasks,
                'completion_rate': (finished_tasks / total_tasks * 100) if total_tasks > 0 else 0
            },
            'sales': {
                'total_revenue': float(total_revenue),
                'count': total_sales_count
            },
            'inventory': {
                'critical_items': low_stock_materials + low_stock_products,
                'low_stock_materials': low_stock_materials,
                'low_stock_products': low_stock_products
            }
        }), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500
