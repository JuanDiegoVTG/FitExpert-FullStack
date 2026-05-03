import math

class IAService:
    @staticmethod
    def calcular_diagnostico_completo(data):
        """
        Procesa los datos del cliente y genera un reporte de composición corporal.
        """
        # Extraer datos (vienen de Java)
        sexo = data.get('sexo', 'M')
        peso = float(data.get('peso', 0))
        altura = float(data.get('altura', 0)) # en cm
        cuello = float(data.get('cuello', 0))
        cintura = float(data.get('cintura', 0))
        cadera = float(data.get('cadera', 0))
        edad = int(data.get('edad', 25))
        nivel_actividad = float(data.get('nivelActividad', 1.2))

        # 1. IMC (Índice de Masa Corporal)
        # 
        altura_m = altura / 100
        imc = peso / (altura_m ** 2)

        # 2. % Grasa Corporal (Fórmula de la Marina de EE.UU.)
        try:
            if sexo == 'M':
                # Fórmula para hombres
                grasa = 495 / (1.0324 - 0.19077 * math.log10(cintura - cuello) + 0.15456 * math.log10(altura)) - 450
            else:
                # Fórmula para mujeres (incluye la cadera)
                grasa = 495 / (1.29579 - 0.35004 * math.log10(cintura + cadera - cuello) + 0.22100 * math.log10(altura)) - 450
        except Exception:
            grasa = 0

        # 3. Tasa Metabólica Basal (Harris-Benedict) y Mantenimiento
        # 
        if sexo == 'M':
            tmb = (10 * peso) + (6.25 * altura) - (5 * edad) + 5
        else:
            tmb = (10 * peso) + (6.25 * altura) - (5 * edad) - 161
        
        mantenimiento = tmb * nivel_actividad

        # 4. Análisis de Estado
        estado = "Normal"
        if imc < 18.5: estado = "Bajo Peso"
        elif 25 <= imc < 30: estado = "Sobrepeso"
        elif imc >= 30: estado = "Obesidad"

        # 5. Recomendación Analítica para el Entrenador
        recomendacion = "Priorizar "
        if grasa > (25 if sexo == 'M' else 32):
            recomendacion += "déficit calórico y entrenamiento de fuerza para recomposición."
        else:
            recomendacion += "superávit controlado para ganancia de masa magra."

        return {
            "imc": round(imc, 2),
            "grasa_corporal": round(grasa, 1),
            "tmb": round(tmb, 0),
            "mantenimiento": round(mantenimiento, 0),
            "estado": estado,
            "recomendacion_ia": recomendacion
        }