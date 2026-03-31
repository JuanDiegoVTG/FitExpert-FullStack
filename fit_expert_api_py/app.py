from flask import Flask, request, jsonify

# 1. Creamos la aplicación Flask
app = Flask(__name__)

# 2. Esta es la página principal (para saber si el servidor está encendido)
@app.route("/")
def home():
    # Solo manda un mensaje de confirmación
    return jsonify({"mensaje": "API Flask funcionando correctamente"})

# 3. Aquí es donde sucede la "magia" para generar la rutina
@app.route("/generar-rutina", methods=["POST"])
def generar_rutina():
    # Recibimos la información que nos envía Spring Boot (en formato JSON)
    data = request.get_json()

    # Sacamos el objetivo que el usuario eligió
    objetivo = data.get("objetivo")

    # Lógica sencilla para decidir qué rutina entregar
    if objetivo == "bajar grasa":
        # Si quiere bajar grasa, le devolvemos una rutina de cardio
        return jsonify({
            "rutina": "HIIT + Cardio",
            "dias": 4,
            "ejercicios": ["Burpees", "Correr", "Sentadillas"],
            "es_favorita": False
        })
    else:
        # Para cualquier otro objetivo (como ganar músculo), le damos una de fuerza
        return jsonify({
            "rutina": "Fuerza",
            "dias": 3,
            "ejercicios": ["Pesas", "Press banca", "Dominadas"],
            "es_favorita": False
        })

# Arrancamos el servidor en el puerto 8000
if __name__ == "__main__":
    # El modo debug=True nos ayuda a ver errores mientras desarrollamos
    app.run(debug=True, port=8000)