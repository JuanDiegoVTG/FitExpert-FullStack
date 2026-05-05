import math
import os
import json
from groq import Groq
from dotenv import load_dotenv

# 1. Cargar configuración
load_dotenv()

# 2. Inicializar cliente de Groq
try:
    api_key = os.getenv("GROQ_API_KEY")
    client = Groq(api_key=api_key) if api_key else None
except Exception as e:
    print(f"No se pudo inicializar Groq: {e}")
    client = None

def calcular_composicion_corporal(datos):
    """
    Calcula IMC, porcentaje de grasa y mantenimiento calórico.
    """
    try:
        # Extraemos datos con valores por defecto para evitar errores
        peso = float(datos.get('peso', 0))
        altura_cm = float(datos.get('altura', 0))
        cintura = float(datos.get('cintura', 0))
        cuello = float(datos.get('cuello', 0))
        cadera = float(datos.get('cadera', 0))
        sexo = str(datos.get('sexo', 'masculino')).lower()
        edad = int(datos.get('edad', 20))
        nivel_actividad = datos.get('nivel_actividad', 'sedentario')

        # --- A. CÁLCULO DE IMC ---
        altura_m = altura_cm / 100
        imc = peso / (altura_m ** 2) if altura_m > 0 else 0

        if imc < 18.5: estado = "Bajo peso"
        elif 18.5 <= imc < 24.9: estado = "Normal"
        elif 25 <= imc < 29.9: estado = "Sobrepeso"
        else: estado = "Obesidad"

        # --- B. PORCENTAJE DE GRASA (Navy Method) ---
        grasa = 0
        try:
            if sexo == 'masculino':
                grasa = 495 / (1.0324 - 0.19077 * math.log10(cintura - cuello) + 0.15456 * math.log10(altura_cm)) - 450
            else:
                grasa = 495 / (1.29579 - 0.35004 * math.log10(cintura + cadera - cuello) + 0.22100 * math.log10(altura_cm)) - 450
        except:
            grasa = 0

        # --- C. CALORÍAS (Mifflin-St Jeor) ---
        factores = {"sedentario": 1.2, "ligero": 1.375, "moderado": 1.55, "activo": 1.725, "muy_activo": 1.9}
        factor = factores.get(nivel_actividad.lower(), 1.2)
        
        if sexo == 'masculino':
            tmb = (10 * peso) + (6.25 * altura_cm) - (5 * edad) + 5
        else:
            tmb = (10 * peso) + (6.25 * altura_cm) - (5 * edad) - 161
        
        mantenimiento = int(tmb * factor)

        # --- D. RESPUESTA ---
        return {
            "imc": round(imc, 2),
            "grasa_corporal": round(max(0, grasa), 2),
            "estado": estado,
            "mantenimiento": mantenimiento,
            "recomendacion": generar_consejo_ia(estado, nivel_actividad)
        }

    except Exception as e:
        print(f"Error en cálculos: {e}")
        return {"error": str(e), "imc": 0, "grasa_corporal": 0, "estado": "Error", "mantenimiento": 0, "recomendacion": "Error en el proceso"}

def generar_consejo_ia(estado, actividad):
    """Genera consejo breve con IA"""
    if not client:
        return "Continúa con tu entrenamiento y mantén una dieta balanceada."
    try:
        prompt = f"Como experto fitness, dame un consejo de 2 líneas para alguien con estado '{estado}' y actividad '{actividad}'."
        completion = client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[{"role": "user", "content": prompt}]
        )
        return completion.choices[0].message.content
    except:
        return "Mantén el enfoque en tus objetivos y sé constante."