# 1. Etapa de compilación: Usa Maven con OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el archivo de configuración de dependencias y el código fuente
COPY pom.xml .
COPY src ./src

# Compilar el proyecto y empaquetar el archivo .jar (saltando los tests para agilizar)
RUN mvn clean package -DskipTests

# 2. Etapa de ejecución: Imagen oficial y ligera de Eclipse Temurin para Java 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el archivo compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para ejecutar el backend en producción
ENTRYPOINT ["java", "-jar", "app.jar"]