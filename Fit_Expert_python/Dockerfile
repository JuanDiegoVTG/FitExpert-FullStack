# 1. Usar imagen oficial fijada a 'bullseye' para garantizar compatibilidad exacta entre Chromium y su Driver
FROM python:3.10-slim-bullseye

# 2. Actualizar el sistema e instalar Chromium y dependencias nativas
RUN apt-get update && apt-get install -y \
    chromium \
    chromium-driver \
    && rm -rf /var/lib/apt/lists/*

# 3. Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# 4. Copiar los archivos de requerimientos e instalarlos sin guardar caché para ahorrar espacio
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. Instalar Gunicorn explícitamente para un servidor de producción robusto
RUN pip install --no-cache-dir gunicorn

# 6. Copiar todo el código del proyecto al contenedor
COPY . .

# 7. Exponer el puerto predeterminado
EXPOSE 5000

# 8. Comando para arrancar la aplicación con Gunicorn escuchando en todas las interfaces
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "app:app"]