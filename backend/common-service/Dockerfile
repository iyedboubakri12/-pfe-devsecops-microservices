# Étape 1 : Construire le projet avec Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copie tous les fichiers du projet common-service
COPY . .

# Construis le projet et génère le JAR
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copie le fichier JAR généré depuis l'étape précédente
COPY --from=builder /app/target/common-service-0.0.1-SNAPSHOT.jar /app/common-service.jar

# Expose le port (8080 par défaut pour Spring Boot, à ajuster si besoin)
EXPOSE 8080

# Lance le service
CMD ["java", "-jar", "/app/common-service.jar"]
