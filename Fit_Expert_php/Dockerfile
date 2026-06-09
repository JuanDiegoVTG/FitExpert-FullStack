# 1. Usar la imagen oficial de PHP con Apache preconfigurado
FROM php:8.2-apache

# 2. Instalar extensiones del sistema necesarias para PHP (por si manejas imágenes o strings complejos)
RUN apt-get update && apt-get install -y \
    libpng-dev \
    libjpeg-dev \
    libfreetype6-dev \
    && docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install -y gd mysqli pdo pdo_mysql

# Si usas la librería oficial de MongoDB en PHP, des comenta las siguientes dos líneas:
RUN apt-get install -y libssl-dev && pecl install mongodb && docker-php-ext-enable mongodb

# 3. Habilitar el módulo de reescritura de Apache (mod_rewrite) por si usas rutas amigables
RUN a2enmod rewrite

# 4. Copiar todo el código de tu proyecto PHP a la carpeta pública de Apache dentro del contenedor
COPY . /var/www/html/

# 5. Darle permisos correctos a Apache para que pueda leer tus archivos y escribir en las carpetas de subidas
RUN chown -R www-data:www-data /var/www/html

# 6. Exponer el puerto 80 (el puerto estándar que usa Apache y que Render lee por defecto)
EXPOSE 80