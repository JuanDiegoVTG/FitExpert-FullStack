from flask import Flask, request, jsonify
from flask_cors import CORS
from services.ia_services import generar_rutina_ia 
from services.cv_services import analizar_cv_entrenador

app = Flask(__name__)
CORS(app)

# --- BASE DE DATOS TEMPORAL ---
# Aquí se guardarán los mensajitos de verdad
messages_db = {}

# --- RUTAS DE IA Y CV ---
@app.route("/generar-rutina", methods=["POST"])
def route_generar_rutina():
    datos = request.get_json()
    return jsonify(generar_rutina_ia(datos))

@app.route("/validar-cv", methods=["POST"])
def route_validar_cv():
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No hay archivo"}), 400
    file = request.files['file']
    return jsonify(analizar_cv_entrenador(file))

# --- RUTAS DEL CHAT HUMANO ---

@app.route('/messages/<int:chat_id>', methods=['GET'])
def get_messages(chat_id):
    # Retorna la lista de mensajes de esa conversación
    return jsonify(messages_db.get(chat_id, []))

@app.route('/send_message', methods=['POST'])
def send_message():
    data = request.json
    chat_id = data.get('chat_id')
    
    if chat_id not in messages_db:
        messages_db[chat_id] = []
    
    # Simplemente guardamos lo que llegue (del cliente o del entrenador)
    new_message = {
        "chat_id": chat_id,
        "sender_id": data.get('sender_id'), # 1 para cliente, 3 para entrenador (ejemplo)
        "content": data.get('content')
    }
    
    messages_db[chat_id].append(new_message)
    return jsonify({"status": "success", "message": new_message}), 201

if __name__ == "__main__":
    app.run(debug=True, port=5000)