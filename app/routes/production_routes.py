from flask import Blueprint, request, jsonify
from app import db
from app.models import ProductionTask, Users, RoleS
import jwt
import os

production_bp = Blueprint('production', __name__)

def get_user_from_token():
    """Obtener usuario del token JWT"""
    token = request.headers.get('Authorization')
    if not token:
        return None

    if token.startswith('Bearer '):
        token = token[7:]

    try:
        payload = jwt.decode(token, os.getenv('SECRET_KEY', 'HolaMundo'), algorithms=['HS256'])
        return Users.query.get(payload['user_id'])
    except:
        return None

@production_bp.route('/tasks', methods=['GET'])
def get_tasks():
    """Listar tareas de producción (Filtrado por operario si aplica)"""
    try:
        user = get_user_from_token()
        if not user:
            return jsonify({'error': 'Token inválido'}), 401

        # Verificar si es operario puro
        is_operario = any(r.TypeRole == 'Operario' for r in user.roles)
        is_admin_or_super = any(r.TypeRole in ['Administrador', 'Supervisor'] for r in user.roles)

        if is_operario and not is_admin_or_super:
            # Operario solo ve sus tareas
            tasks = ProductionTask.query.filter_by(AssignedTo=user.iD_User).all()
        else:
            # Admin y Supervisor ven todo
            tasks = ProductionTask.query.all()

        return jsonify([t.to_dict() for t in tasks]), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@production_bp.route('/tasks', methods=['POST'])
def create_task():
    """Crear nueva tarea de producción (Solo Administrador)"""
    try:
        user = get_user_from_token()
        is_admin = any(r.TypeRole == 'Administrador' for r in user.roles)
        if not is_admin:
            return jsonify({'error': 'Solo el administrador puede crear tareas'}), 403

        data = request.get_json()
        description = data.get('Description')
        target_qty = data.get('TargetQuantity')

        if not description or not target_qty:
            return jsonify({'error': 'Description y TargetQuantity son requeridos'}), 400

        new_task = ProductionTask(
            Description=description,
            TargetQuantity=target_qty,
            Status='Pendiente'
        )

        db.session.add(new_task)
        db.session.commit()

        return jsonify({'message': 'Tarea creada exitosamente', 'task': new_task.to_dict()}), 201

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@production_bp.route('/assign', methods=['PUT'])
def assign_task():
    """Asignar tarea a un operario (Solo Admin/Supervisor)"""
    try:
        user = get_user_from_token()
        is_admin_or_super = any(r.TypeRole in ['Administrador', 'Supervisor'] for r in user.roles)
        if not is_admin_or_super:
            return jsonify({'error': 'No tienes permisos para asignar tareas'}), 403

        data = request.get_json()
        task_id = data.get('iD_Task')
        operario_id = data.get('iD_User')

        if not task_id or not operario_id:
            return jsonify({'error': 'iD_Task e iD_User son requeridos'}), 400

        task = ProductionTask.query.get(task_id)
        if not task:
            return jsonify({'error': 'Tarea no encontrada'}), 404

        # Verificar que el usuario asignado existe y es operario
        target_user = Users.query.get(operario_id)
        if not target_user:
            return jsonify({'error': 'Usuario no encontrado'}), 404

        task.AssignedTo = operario_id
        db.session.commit()

        return jsonify({'message': f'Tarea asignada a {target_user.UserName}'}), 200

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@production_bp.route('/record', methods=['POST'])
def record_production():
    """Registrar avance en una tarea"""
    try:
        data = request.get_json()
        task_id = data.get('iD_Task')
        quantity = data.get('Quantity')

        if not task_id or not quantity:
            return jsonify({'error': 'iD_Task y Quantity son requeridos'}), 400

        task = ProductionTask.query.get(task_id)
        if not task:
            return jsonify({'error': 'Tarea no encontrada'}), 404

        task.ProducedQuantity += quantity

        # Si se alcanza el objetivo, marcar como terminada o en proceso
        if task.ProducedQuantity >= task.TargetQuantity:
            task.Status = 'Terminada'
        elif task.ProducedQuantity > 0:
            task.Status = 'En Proceso'

        db.session.commit()
        return jsonify({'message': 'Avance registrado exitosamente'}), 200

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500
