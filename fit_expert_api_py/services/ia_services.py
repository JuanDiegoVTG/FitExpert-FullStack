import os
import json
import random
from groq import Groq
from dotenv import load_dotenv

# 1. Cargar configuración
load_dotenv()

# 2. Inicializar cliente de Groq
try:
    # Intentamos leer la llave del .env
    api_key = os.getenv("GROQ_API_KEY")
    client = Groq(api_key=api_key)
except Exception as e:
    print(f" No se pudo inicializar Groq: {e}")
    client = None

def generar_rutina_ia(datos_usuario):
    """
    Función principal que decide qué 'motor' usar.
    """
    objetivo = datos_usuario.get('objetivo', 'acondicionamiento').lower()
    peso = datos_usuario.get('peso', 70)

    # --- INTENTO 1: IA GENERATIVA (GROQ) ---
    if client:
        try:
            print(f" Generando con Groq para: {objetivo} ---")
            
            instrucciones = f"""
            Eres el experto fitness de FitExpert. 
            Genera una rutina para un usuario de {peso}kg con objetivo: {objetivo}.
            RESPONDE ÚNICAMENTE UN JSON con estas llaves: 
            "nombre_rutina", "dias", "ejercicios" (lista de 5 strings).
            """

            completion = client.chat.completions.create(
                model="llama-3.3-70b-versatile",
                messages=[{"role": "user", "content": instrucciones}],
                response_format={"type": "json_object"} # Obliga a la IA a dar JSON puro
            )

            # Convertimos la respuesta de texto a diccionario Python
            resultado = json.loads(completion.choices[0].message.content)
            print(" IA generó la rutina exitosamente.")
            return resultado

        except Exception as e:
            print(f" Falló Groq (Cuota o Conexión): {e}")
            print("--- Activando Motor Local de Emergencia ---")

    # MOTOR LOCAL (Lógica de Respaldo)
    return generar_logica_local(objetivo, peso)

def generar_logica_local(objetivo, peso):
    """
    Sistema experto basado en reglas (Funciona sin internet).
    """
    ejercicios_db = {
        "masa": ["Press Banca", "Sentadillas", "Remo con barra", "Peso Muerto"],
        "peso": ["Burpees", "Saltar Cuerda", "Zancadas", "Escaladores"],
        "tonificar": ["Flexiones", "Fondos de tríceps", "Plancha", "Dominadas"]
    }

    # Seleccionamos la categoría por palabras clave
    categoria = "masa" if "masa" in objetivo else "peso" if "perder" in objetivo or "adelgazar" in objetivo else "tonificar"
    
    # Personalizamos un poco las repeticiones según el peso
    intensa = "Alta" if peso > 85 else "Media"

    return {
        "nombre_rutina": f"Plan FitExpert Local - Nivel {intensa}",
        "dias": 3,
        "ejercicios": [f"{e} (3 series)" for e in random.sample(ejercicios_db[cat], 3)]
    }