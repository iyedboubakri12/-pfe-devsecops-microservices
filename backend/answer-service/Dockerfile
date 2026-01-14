# Étape 1 : Utiliser common-exam depuis Docker Hub
FROM oumaimakhelifi/common-exam:latest AS common-exam-builder

# Étape 2 : Utiliser common-service depuis Docker Hub
FROM oumaimakhelifi/common-service:latest AS common-service-builder

# Étape 3 : Utiliser common-student depuis Docker Hub
FROM oumaimakhelifi/common-student:latest AS common-student-builder

# Étape 4 : Construire answer-service avec les dépendances
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copier le projet answer-service
COPY . .

# Copier les JAR des autres projets depuis leurs images
COPY --from=common-exam-builder /app/common-exam.jar /root/.m2/repository/com/microservices/common-exam/0.0.1-SNAPSHOT/common-exam-0.0.1-SNAPSHOT.jar
COPY --from=common-service-builder /app/common-service.jar /root/.m2/repository/com/microservices/common-service/0.0.1-SNAPSHOT/common-service-0.0.1-SNAPSHOT.jar
COPY --from=common-student-builder /app/common-student.jar /root/.m2/repository/com/microservices/common-student/0.0.1-SNAPSHOT/common-student-0.0.1-SNAPSHOT.jar

# Construire answer-service
RUN mvn clean package -DskipTests

# Étape 5 : Image finale avec OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/answer-service-0.0.1-SNAPSHOT.jar /app/answer-service.jar

EXPOSE 8083

CMD ["java", "-jar", "/app/answer-service.jar"]
