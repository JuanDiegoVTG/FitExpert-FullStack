# 1. Usar la imagen oficial de PHP con Apache preconfigurado
FROM php:8.2-apache

# 2. Instalar extensiones del sistema necesarias para PHP
RUN apt-get update && apt-get install -y \
    libpng-dev \
    libjpeg-dev \
    libfreetype6-dev \
    && docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install gd mysqli pdo pdo_mysql

# 3. Habilitar el módulo de reescritura de Apache
RUN a2enmod rewrite

# 4. Copiar todo el código de tu proyecto PHP a la carpeta pública de Apache
COPY . /var/www/html/

# 5. Darle permisos correctos a Apache
RUN chown -R www-data:www-data /var/www/html

# 6. Exponer el puerto 80
EXPOSE 80