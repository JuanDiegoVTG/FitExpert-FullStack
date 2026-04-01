import os
from dotenv import load_dotenv
import google.generativeai as genai
from flask import Flask, request, jsonify
import json

# Cargar las variables del archivo .env
load_dotenv()

app = Flask(__name__)

# Configurar la clave usando la variable de entorno
api_key = os.getenv("GEMINI_API_KEY")
genai.configure(api_key=api_key)

#for m in genai.list_models():
#  if 'generateContent' in m.supported_generation_methods:
#    print(f"Modelo disponible: {m.name}")

#Modelo gratituo 
model = genai.GenerativeModel('models/gemini-1.5-flash-latest')

# 2. Esta es la página principal (para saber si el servidor está encendido)
@app.route("/")
def home():
    # Solo manda un mensaje de confirmación
    return jsonify({"mensaje": "API Flask funcionando correctamente"})

# 3. Aquí es donde sucede la "magia" para generar la rutina
@app.route("/generar-rutina", methods=["POST"])
def generar_rutina():
    #Recibimos lo que java manda
    datos_usuario = request.get_json()

    objetivo = datos_usuario.get('objetivo' , 'acondicionamiento')
    peso = datos_usuario.get('peso', 70)
    altura = datos_usuario.get('altura', 170)

    #Las instrucciones para trabajar la IA
    instrucciones = f"""
    Actua como un entrenador personal de alto rendimiento.
    Crea una Rutina para un usuario con:
    - Peso: {peso}kg
    - Altura: {altura}m
    - Objetivo: {objetivo}
    Responde ÚNICAMENTE en formato JSON con estas llaves:
    "nombre_rutina", "dias", "ejercicios" (lista de strings).
    No escribas nada más, solo el JSON puro."""

    #Pedimos la respuesta de la IA gemini
    try:
        repuesta = model.generate_content(instrucciones)
    
        #Limpiamos para que Python entienda los datos 
        texto_sucio = repuesta.text
        texto_limpio = texto_sucio.replace('```json', '').replace('```', '').strip()

        #Convertimos el texto en un diccionario JSON
        rutina_final = json.loads(texto_limpio)

        return jsonify(rutina_final)

    except Exception as e:
        print(f'Error: {e}')
    
        print(f"LA IA ESTÁ CANSADA: {e}")
        # Devolvemos una rutina manual para que el usuario no vea un error 500
        return jsonify({
            "nombre_rutina": "Rutina de Entrenamiento Express (Modo Offline)",
            "dias": 3,
            "ejercicios": ["Sentadillas", "Flexiones", "Plancha"]
        
        })

# Arrancamos el servidor en el puerto 8000
if __name__ == "__main__":
    # El modo debug=True nos ayuda a ver errores mientras desarrollamos
    app.run(debug=True, port=8000)