# Étape 1 : Récupérer les dépendances depuis les images existantes
FROM oumaimakhelifi/common-exam:latest AS common-exam-builder
FROM oumaimakhelifi/common-service:latest AS common-service-builder
FROM oumaimakhelifi/common-student:latest AS common-student-builder

# Étape 2 : Construire course-service avec Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copie le projet course-service
COPY . .

# Copie les JAR des dépendances depuis leurs images dans le dépôt Maven local
COPY --from=common-exam-builder /app/common-exam.jar /root/.m2/repository/com/microservices/common-exam/0.0.1-SNAPSHOT/common-exam-0.0.1-SNAPSHOT.jar
COPY --from=common-service-builder /app/common-service.jar /root/.m2/repository/com/microservices/common-service/0.0.1-SNAPSHOT/common-service-0.0.1-SNAPSHOT.jar
COPY --from=common-student-builder /app/common-student.jar /root/.m2/repository/com/microservices/common-student/0.0.1-SNAPSHOT/common-student-0.0.1-SNAPSHOT.jar

# Construis le JAR
RUN mvn clean package -DskipTests

# Étape 3 : Image finale avec JDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copie le JAR construit
COPY --from=builder /app/target/course-service-0.0.1-SNAPSHOT.jar /app/course-service.jar

# Expose le port 8081
EXPOSE 8081

# Lance le service
CMD ["java", "-jar", "/app/course-service.jar"]
