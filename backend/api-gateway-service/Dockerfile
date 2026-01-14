# Étape 1 : Construire avec Maven et JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copier le projet api-gateway-service
COPY . .

# Construire le JAR
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copier le JAR construit
COPY --from=builder /app/target/api-gateway-service-0.0.1-SNAPSHOT.jar /app/api-gateway-service.jar

# Exposer le port 8090 (défini dans application.properties)
EXPOSE 8090

# Lancer le service
CMD ["java", "-jar", "/app/api-gateway-service.jar"]
