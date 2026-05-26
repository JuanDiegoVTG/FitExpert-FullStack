# 1. Usar una imagen base oficial de Python
FROM python:3.10-slim

# 2. Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiar los archivos de requerimientos e instalarlos
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. Copiar todo el código del proyecto al contenedor
COPY . .

# 5. Exponer el puerto predeterminado en el que corre Flask
EXPOSE 5000

# 6. Comando para arrancar la aplicación apuntando al host de producción
CMD ["python", "app.py"]
