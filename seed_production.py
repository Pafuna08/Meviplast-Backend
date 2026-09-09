from app import create_app, db
from app.models import Material, ProductionTask

app = create_app()

def seed():
    with app.app_context():
        # Materias Primas
        m1 = Material(MaterialName='Polietileno de Alta Densidad (HDPE)', Quantity=500.5, Unit='kg')
        m2 = Material(MaterialName='Polipropileno (PP)', Quantity=250.0, Unit='kg')
        m3 = Material(MaterialName='Colorante Rojo Maestro', Quantity=15.0, Unit='kg')

        # Tareas de Producción
        t1 = ProductionTask(
            Description='Fabricación de 1000 envases de 1L (HDPE)',
            Status='Pendiente',
            TargetQuantity=1000,
            ProducedQuantity=0
        )
        t2 = ProductionTask(
            Description='Producción de tapas para envases 1L (PP)',
            Status='En Proceso',
            TargetQuantity=5000,
            ProducedQuantity=1200
        )
        t3 = ProductionTask(
            Description='Mezclado de resina virgen con recuperado',
            Status='Terminada',
            TargetQuantity=500,
            ProducedQuantity=500
        )

        db.session.add_all([m1, m2, m3, t1, t2, t3])
        db.session.commit()
        print("Datos de producción insertados con éxito.")

if __name__ == '__main__':
    seed()
