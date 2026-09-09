from app import create_app

app = create_app()

if __name__ == '__main__':
    print("Iniciando servidor Flask para MEVIPLAST en puerto 5060...")
    app.run(debug=True, host='0.0.0.0', port=5060)