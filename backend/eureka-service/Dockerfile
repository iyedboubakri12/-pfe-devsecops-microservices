# Étape 1 : Construire avec Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copie le projet eureka-service
COPY . .

# Construis le JAR
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copie le JAR construit
COPY --from=builder /app/target/eureka-service-0.0.1-SNAPSHOT.jar /app/eureka-service.jar

# Expose le port 8761
EXPOSE 8761

# Lance le service
CMD ["java", "-jar", "/app/eureka-service.jar"]
