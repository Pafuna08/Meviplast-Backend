from flask import Blueprint, jsonify
from app.models import Material

material_bp = Blueprint('materials', __name__)

@material_bp.route('/', methods=['GET'])
def get_materials():
    """Listar todas las materias primas"""
    try:
        materials = Material.query.all()
        return jsonify([m.to_dict() for m in materials]), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500
