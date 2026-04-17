from flask import Flask, request, jsonify
from flask_cors import CORS
from services.ia_services import IAService
from services.cv_services import analizar_cv_entrenador

app = Flask(__name__)
CORS(app)

# --- BASE DE DATOS TEMPORAL ---
# Aquí se guardarán los mensajitos de verdad
messages_db = {}


@app.route("/validar-cv", methods=["POST"])
def route_validar_cv():
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No hay archivo"}), 400
    file = request.files['file']
    return jsonify(analizar_cv_entrenador(file))

# --- RUTAS DEL CHAT HUMANO ---

@app.route('/send_message', methods=['POST'])
def send_message():
    data = request.json
    chat_id = str(data.get('chat_id')) # Lo pasamos a string para que no falle
    
    # ¡ESTO ERA LO QUE FALTABA! Armar el paquete del mensaje
    nuevo_mensaje = {
        "sender_id": data.get('sender_id'),
        "content": data.get('content')
    }
    
    # Si el cuarto de chat no existe, lo creamos vacío
    if chat_id not in messages_db:
        messages_db[chat_id] = []
        
    # Guardamos el mensaje
    messages_db[chat_id].append(nuevo_mensaje)
    
    return jsonify({"status": "success", "message": "Mensaje guardado"}), 201

@app.route('/get_messages/<chat_id>', methods=['GET'])
def get_messages(chat_id):
    # Buscamos los mensajes de este chat_id específico
    # Si no hay mensajes todavía, devolvemos una lista vacía []
    mensajes_del_chat = messages_db.get(str(chat_id), [])
    
    return jsonify(mensajes_del_chat), 200

@app.route('/api/diagnostico', methods=['POST'])
def diagnostico_endpoint():
    try:
        data = request.get_json(force=True)
        # Llamamos al service para que haga las matemáticas
        resultado = IAService.calcular_diagnostico_completo(data)
        
        print(f"✅ Diagnóstico generado para {data.get('sexo')}")
        return jsonify(resultado), 200

    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return jsonify({"error": "No se pudo procesar la analítica"}), 500

if __name__ == "__main__":
    app.run(debug=True, port=5000)