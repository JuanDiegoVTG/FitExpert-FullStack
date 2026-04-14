import PyPDF2
import io

KEYWORDS_ENTRENADOR=[
    "certificacion", "fitness", "nutricion", "entrenamiento", 
    "deporte", "fisiologia", "gimnasio", "musculacion", "atleta"
]

def analizar_cv_entrenador(file_storage):
    """
    Lee un archivo PDF y busca palabras clave para puntuar el perfil.
    """
    texto_cv = ""
    puntos = 0
    encontradas = []

    try:
        # 1. Leer el contenido del PDF desde la memoria
        pdf_reader = PyPDF2.PdfReader(io.BytesIO(file_storage.read()))
        
        # 2. Extraer texto de todas las páginas
        for page in pdf_reader.pages:
            texto_cv += page.extract_text().lower()

        # 3. Validar palabras clave
        for word in KEYWORDS_ENTRENADOR:
            if word in texto_cv:
                puntos += 1
                encontradas.append(word)

        # 4. Calcular porcentaje de compatibilidad
        score = (puntos / len(KEYWORDS_ENTRENADOR)) * 100

        return {
            "status": "success",
            "score": round(score, 2),
            "palabras_clave": encontradas,
            "recomendacion": "Altamente Recomendado" if score >= 50 else "Revision Manual Requerida",
            "resumen": f"Se detectaron {puntos} competencias clave de entrenamiento."
        }

    except Exception as e:
        return {
            "status": "error",
            "message": f"No se pudo procesar el PDF: {str(e)}"
        }