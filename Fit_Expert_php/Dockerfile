# 1. Usar la imagen oficial de PHP con Apache preconfigurado
FROM php:8.2-apache

# 2. Instalar extensiones del sistema (Agregamos dependencias para Mongo y Composer)
RUN apt-get update && apt-get install -y \
    libpng-dev \
    libjpeg-dev \
    libfreetype6-dev \
    libssl-dev \
    pkg-config \
    git \
    unzip \
    && docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install gd mysqli pdo pdo_mysql

# 3. Instalar y habilitar la extensión nativa de MongoDB para PHP
RUN pecl install mongodb && docker-php-ext-enable mongodb

# 4. Traer Composer desde su imagen oficial
COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

# 5. Habilitar el módulo de reescritura de Apache
RUN a2enmod rewrite

# 6. Copiar todo el código de tu proyecto PHP a la carpeta de Apache
COPY . /var/www/html/

# 7. Ejecutar Composer ACTUALIZANDO dependencias para evitar el error de "wrong version"
WORKDIR /var/www/html/
RUN composer update --no-dev --optimize-autoloader

# 8. Darle permisos correctos a Apache (después de crear la carpeta vendor)
RUN chown -R www-data:www-data /var/www/html

# 9. Exponer el puerto 80
EXPOSE 80