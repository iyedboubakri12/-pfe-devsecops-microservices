# Étape 1 : Construire avec Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/common-exam-0.0.1-SNAPSHOT.jar /app/common-exam.jar

EXPOSE 8084

CMD ["java", "-jar", "/app/common-exam.jar"]

