# --- Etapa 1: Construcción ---
# Usa una imagen con Maven y JDK para compilar el código
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copia el archivo de configuración de Maven y el código fuente
COPY pom.xml .
# Si usas Gradle, copia build.gradle y settings.gradle
RUN mvn dependency:go-offline -B
COPY src ./src
# Ejecuta el empaquetado y omite los tests para acelerar el proceso
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución ---
# Usa una imagen de JRE más ligera para correr la aplicación
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copia el archivo JAR generado en la primera etapa
COPY --from=build /app/target/*.jar app.jar
# Expone el puerto donde correrá la app (Render usará la variable PORT)
EXPOSE 8080
# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]