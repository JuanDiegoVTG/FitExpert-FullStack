from flask import Flask, request, jsonify
from services.chat_service import create_chat, send_message, get_messages
from services.ia_services import generar_rutina_ia # Importamos la función
from services.cv_services import analizar_cv_entrenador
app = Flask(__name__)

@app.route("/generar-rutina", methods=["POST"])
def route_generar_rutina():
    # El Controller recibe los datos
    datos = request.get_json()
    
    # El Service procesa la lógica
    resultado = generar_rutina_ia(datos)
    
    # El Controller responde al cliente (Java)
    return jsonify(resultado)

@app.route("/validar-cv", methods=["POST"])
def route_validar_cv():
    # Verificamos que venga un archivo en la petición
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No se encontro el archivo"}), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify({"status": "error", "message": "Nombre de archivo vacio"}), 400

    # Llamamos al servicio de análisis
    resultado = analizar_cv_entrenador(file)
    return jsonify(resultado)

if __name__ == "__main__":
    app.run(debug=True, port=8000)
    
    #crear chat cuando contratan 
@app.route("/contratar", methods=["POST"])
def contratar():
    data = request.json

    trainer_id = data["trainer_id"]
    client_id = data["client_id"]

    #  el cliente contrata al entrenador


    chat = create_chat(trainer_id, client_id)

    return jsonify({
        "message": "Contratación exitosa",
        "chat": chat
    })

    # enviar mensaje 
@app.route("/send_message", methods=["POST"])
def api_send_message():
    data = request.json
    msg = send_message(
        data["chat_id"],
        data["sender_id"],
        data["content"]
    )
    return jsonify(msg)

   # obtener mensajes 
@app.route("/messages/<int:chat_id>", methods=["GET"])
def api_get_messages(chat_id):
    return jsonify(get_messages(chat_id))
   # validar que pertenece al chat
def api_get_messages(chat_id):
    user_id = request.args.get("user_id")

    
    # devolver menasjes 
@app.route("/messages/<int:chat_id>", methods=["GET"])
def api_get_messages(chat_id):
    return jsonify(get_messages(chat_id))
