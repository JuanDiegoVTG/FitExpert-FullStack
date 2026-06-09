# 1. Usar una imagen base oficial de Python
FROM python:3.10-slim

# 🔥 NUEVO: Actualizar el sistema e instalar Chromium y sus dependencias de Linux
RUN apt-get update && apt-get install -y \
    chromium \
    chromium-driver \
    && rm -rf /var/lib/apt/lists/*

# 2. Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiar los archivos de requerimientos e instalarlos
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 🔥 NUEVO: Asegurarnos de tener gunicorn instalado para producción en Render
RUN pip install gunicorn

# 4. Copiar todo el código del proyecto al contenedor
COPY . .

# 5. Exponer el puerto predeterminado en el que corre Flask
EXPOSE 5000

# 6. Comando para arrancar la aplicación con Gunicorn (Recomendado para Render)
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "app:app"]