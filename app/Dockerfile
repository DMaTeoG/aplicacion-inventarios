# --- Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar el pom y descargar dependencias (se aprovecha la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar las fuentes y compilar el JAR omitiendo los tests unitarios
COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto por defecto (Render inyectará dynamic PORT)
EXPOSE 8085

# Iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
