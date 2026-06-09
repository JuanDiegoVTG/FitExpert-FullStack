from flask import Flask, request, jsonify
from flask_cors import CORS
from services.ia_services import calcular_composicion_corporal
from services.cv_services import analizar_cv_entrenador

# IMPORTS ADICIONALES PARA SELENIUM
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
import time

app = Flask(__name__)
CORS(app)

# --- BASE DE DATOS TEMPORAL ---
# Aquí se guardarán los mensajitos de verdad
messages_db = {}


# --- RUTA PARA EL DIAGNÓSTICO DE IA (Lo que pide Java) ---
@app.route("/api/diagnostico", methods=["POST"])
def route_generar_diagnostico():
    try:
        datos = request.get_json()
        
        # Aquí es donde llamas a tu lógica de IA que calcula IMC y Grasa
        resultado = calcular_composicion_corporal(datos)
        
        # El JSON debe tener 'imc' y 'grasa_corporal' porque Java los busca así
        return jsonify(resultado), 200
        
    except Exception as e:
        print(f"Error en diagnóstico: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500
    

# --- RUTAS CV ---
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


# NUEVA RUTA: ENDPOINT PARA LA PRUEBA FUNCIONAL DE SELENIUM
#
@app.route('/test/selenium', methods=['GET'])
def ejecutar_prueba_selenium():
    # Configuración headless obligatoria para que corra invisible en la terminal de WSL
    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")

    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--remote-debugging-port=9222")
    chrome_options.binary_location = "/usr/bin/chromium-browser"
    
    driver = None
    try:
        # 1. Levantar el navegador virtual
        driver = webdriver.Chrome(options=chrome_options)
        
        # 2. Navegar a la URL del taller
        driver.get("https://www.google.com")
        
        # 3. Localizar la caja del buscador usando el selector por nombre ('q')
        buscador = driver.find_element(By.NAME, "q")
        
        # 4. Simular la escritura automatizada del texto solicitado
        buscador.send_keys("Testing de software")
        time.sleep(1) # Pausa de control para asegurar que el buffer procese la cadena
        
        # Cierramos el navegador de forma exitosa
        driver.quit()
        
        return jsonify({
            "status": "SUCCESS",
            "message": "Prueba funcional con Selenium ejecutada con éxito.",
            "valida": "Navegación correcta a Google e interacción con el input semántico name='q'."
        }), 200

    except Exception as e:
        # Si la prueba falla en algún punto, cerramos el proceso para no dejar hilos colgados
        if driver:
            driver.quit()
            
        return jsonify({
            "status": "FAILED",
            "message": "La prueba funcional automatizada ha fallado.",
            "error_tecnico": str(e)
        }), 500


if __name__ == "__main__":
    # Mantener corriendo en el puerto 5000 con autoreload activo
    app.run(debug=True, port=5000)