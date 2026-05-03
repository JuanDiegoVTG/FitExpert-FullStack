# 🏋️ FitExpert - FullStack Project (SENA)

Este proyecto es una solución integral para la gestión de rutinas personalizadas, desarrollada 
como parte de la formación en Desarrollo de Software en el **SENA**. El sistema combina la potencia 
de **Spring Boot** para el backend principal y **Flask** para una API especializada.

---

## 🚀 Estructura del Proyecto

El repositorio está organizado en dos componentes principales:

### 1. 🟢 Backend Principal (Spring Boot)
Ubicado en la carpeta `EmiLite_5to-main`.
- **Tecnologías:** Java, Spring Boot, Maven.
- **Función:** Gestión de usuarios, persistencia de datos y lógica de negocio principal.

### 2. 🐍  API de Rutinas (Flask)
Ubicada en la carpeta `fit_expert_api_py`.
- **Tecnologías:** Python, Flask.
- **Función:** Generación y manejo de rutinas personalizadas a través de una API REST.

---

##  Requisitos Previos

Para ejecutar este proyecto localmente, necesitarás:
* **Java 17** o superior.
* **Maven** (para las dependencias de Java).
* **Python 3.x**.
* **Postman** (opcional, para probar los endpoints).

---

## 📦 Instalación y Configuración

### Ejecutar el API de Flask
1. Entra a la carpeta: `cd fit_expert_api_py`
2. Instala las dependencias: `pip install -r requirements.txt` (si tienes el archivo)
3. Inicia el servidor: `python app.py`
   * *La API correrá por defecto en el puerto 8000.*

### Ejecutar el Backend de Spring Boot
1. Entra a la carpeta: `cd EmiLite_5to-main`
2. Ejecuta el comando: `mvn spring-boot:run`
   * *El servidor correrá por defecto en el puerto 8082.*

---

##  Autor
* **Juan Diego Guasca** - *Desarrollador en Formación (SENA)* - [JuanDiegoVTG](https://github.com/JuanDiegoVTG)

---
*Proyecto desarrollado en Bogota, Colombia.*
