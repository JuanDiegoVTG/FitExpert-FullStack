# 1. Etapa de compilación: Usa Maven con OpenJDK 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiar el archivo de configuración de dependencias y el código fuente
COPY pom.xml .
COPY src ./src

# Compilar el proyecto y empaquetar el archivo .jar (saltando los tests para agilizar)
RUN mvn clean package -DskipTests

# 2. Etapa de ejecución: Imagen ligera con solo el JRE para correr el sistema
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiar el archivo compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para ejecutar el backend en producción
ENTRYPOINT ["java", "-jar", "app.jar"]