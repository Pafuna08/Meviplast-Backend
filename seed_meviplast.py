from app import create_app, db
from app.models import Material, ProductionTask, RoleS, Users, Product, Sales, SalesDetail
from datetime import datetime, timedelta

app = create_app()

def seed():
    with app.app_context():
        # 1. ROLES
        roles_names = ['Administrador', 'Supervisor', 'Operario', 'Almacenista', 'Vendedor']
        for role_name in roles_names:
            if not RoleS.query.filter_by(TypeRole=role_name).first():
                db.session.add(RoleS(TypeRole=role_name))
        db.session.commit()
        print("Roles inicializados.")

        # 2. USUARIOS DE PRUEBA
        users_data = [
            ('Pablo Admin', 'Pablo@gmail.com', 'Administrador'),
            ('Carlos Supervisor', 'carlos@meviplast.com', 'Supervisor'),
            ('Juan Operario', 'juan@meviplast.com', 'Operario'),
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
        print("Usuarios de prueba creados.")

        # 3. MATERIAS PRIMAS
        if not Material.query.first():
            db.session.add_all([
                Material(MaterialName='Polietileno (HDPE)', Quantity=500.5, Unit='kg'),
                Material(MaterialName='Polipropileno (PP)', Quantity=250.0, Unit='kg'),
                Material(MaterialName='Pigmento Azul', Quantity=10.0, Unit='kg'),
                Material(MaterialName='Recuperado Molido', Quantity=800.0, Unit='kg')
            ])

        # 4. PRODUCTOS TERMINADOS (PARA VENTAS)
        if not Product.query.first():
            db.session.add_all([
                Product(ProductName='Caneca Plástica 20L', Price=15000.0, Stock=150),
                Product(ProductName='Envase Industrial 1L', Price=2500.0, Stock=2000),
                Product(ProductName='Tapa de Seguridad 38mm', Price=150.0, Stock=10000),
                Product(ProductName='Caja Agrícola Calada', Price=12000.0, Stock=80)
            ])
        db.session.commit()
        print("Productos terminados creados.")

        # 5. TAREAS DE PRODUCCIÓN
        if not ProductionTask.query.first():
            db.session.add_all([
                ProductionTask(Description='Fabricación 1000 envases 1L', Status='Pendiente', TargetQuantity=1000),
                ProductionTask(Description='Inyección de 5000 tapas', Status='En Proceso', TargetQuantity=5000, ProducedQuantity=2500),
                ProductionTask(Description='Molienda de purga (HDPE)', Status='Terminada', TargetQuantity=200, ProducedQuantity=200)
            ])

        # 6. VENTAS HISTÓRICAS (PARA REPORTES)
        if not Sales.query.first():
            vendedor = Users.query.join(Users.roles).filter(RoleS.TypeRole == 'Vendedor').first()
            prod1 = Product.query.filter_by(ProductName='Caneca Plástica 20L').first()
            prod2 = Product.query.filter_by(ProductName='Envase Industrial 1L').first()

            if vendedor and prod1 and prod2:
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

                print("Ventas históricas creadas.")

        db.session.commit()
        print("Datos de Meviplast sembrados con éxito.")

if __name__ == '__main__':
    seed()
