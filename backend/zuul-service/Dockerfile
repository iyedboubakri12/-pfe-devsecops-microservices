# Étape 1 : Construire l'application avec Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copie le projet zuul-service
COPY . .

# Construis le JAR (sans tests)
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copie le JAR construit
COPY --from=builder /app/target/zuul-service-0.0.1-SNAPSHOT.jar /app/zuul-service.jar

# Expose le port 8090
EXPOSE 8090

# Lance le service
CMD ["java", "-jar", "/app/zuul-service.jar"]
