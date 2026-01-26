# 🔧 Guide de Résolution: Tests d'Intégration TestContainers

## Problème identifié
```
[ERROR] AnswerControllerIntegrationTest.shouldCreateAnswers » IllegalState Failed to load...
[ERROR] Tests run: 26, Failures: 0, Errors: 8, Skipped: 0
```

**Cause:** TestContainers ne peut pas accéder à Docker en CI/CD, causant l'échec des tests d'intégration.

---

## ✅ Solutions implémentées

### 1. **TestContainersConfig.java - Try-Catch Fallback**
```java
// ✅ NEW: Try-catch pour gérer l'absence de Docker
static {
    try {
        mongoDBContainer = new MongoDBContainer(...)
        mongoDBContainer.start();
    } catch (Exception e) {
        // Fallback vers localhost si Docker n'est pas disponible
        System.setProperty("spring.data.mongodb.uri", 
            "mongodb://localhost:27017/answer-service");
    }
}
```

**Services affectés:**
- ✅ `answer-service` - MongoDB
- ✅ `course-service` - MySQL

### 2. **Profils Spring par environnement**

**application-test.properties** (Développement local + TestContainers)
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/answer-service
eureka.client.enabled=false
```

**application-test-ci.properties** (CI/CD - sans Docker)
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/answer-service
eureka.client.enabled=false
```

**Services avec profils configurés:**
- ✅ `answer-service` 
- ✅ `course-service`
- ✅ `user-service` (H2 in-memory)
- ✅ `exam-service` (H2 in-memory)

### 3. **Docker Compose pour développement local**

**Answer Service:**
```yaml
services:
  mongodb:
    image: mongo:4.4
    ports:
      - "27017:27017"
```

**Course Service:**
```yaml
services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
```

### 4. **.gitlab-ci.yml - Configuration des services**

```yaml
backend-tests:
  services:
    - mongo:4.4
    - mysql:8.0
  environment:
    SPRING_DATA_MONGODB_URI: "mongodb://mongo:27017/answer-service"
    SPRING_DATASOURCE_URL: "jdbc:mysql://mysql:3306/test_db"
```

---

## 🚀 Comment exécuter les tests

### En développement local (avec Docker Desktop)

```bash
# Terminal 1: Démarrer les bases de données
cd backend/answer-service
docker-compose up -d

# Terminal 2: Exécuter les tests
mvn clean test

# Arrêter les services
docker-compose down
```

### En CI/CD (GitLab)

```yaml
# Les services sont démarrés automatiquement par GitLab CI
# Les variables d'environnement sont injectées automatiquement
```

### Tests unitaires uniquement (fast)

```bash
mvn clean test -DskipIntegrationTests -Dspring.profiles.active=test-unit
```

---

## 📊 Résultats attendus

### Avant (Erreurs d'intégration)
```
[ERROR] AnswerServiceIntegrationTest » IllegalState Failed to load...
Tests run: 26, Failures: 0, Errors: 8, Skipped: 0
BUILD FAILURE
```

### Après (✅ Tous les tests passent)
```
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🔍 Dépannage

### Erreur: "MongoDB connection refused"
```bash
# Vérifier que MongoDB est en cours d'exécution
docker ps | grep mongo

# Si absent, redémarrer
docker-compose up -d
```

### Erreur: "Docker daemon is not running"
```bash
# Sur Windows/Mac: Démarrer Docker Desktop
# Sur Linux: sudo systemctl start docker
```

### Tests échouent localement mais passent en CI/CD
- Vérifier la version MongoDB/MySQL locale vs. CI/CD
- S'assurer que les ports ne sont pas en conflit

---

## 📝 Fichiers créés/modifiés

| Fichier | Statut | Description |
|---------|--------|-------------|
| `answer-service/src/test/java/config/TestContainersConfig.java` | ✏️ Modifié | Try-catch fallback ajouté |
| `answer-service/src/test/resources/application-test-ci.properties` | ✨ Créé | Profil CI/CD |
| `answer-service/docker-compose.yml` | ✨ Créé | Services de test |
| `answer-service/TESTING.md` | ✨ Créé | Documentation tests |
| `course-service/src/test/java/config/TestContainersConfig.java` | ✏️ Modifié | Try-catch fallback |
| `course-service/src/test/resources/application-test-ci.properties` | ✨ Créé | Profil CI/CD |
| `course-service/docker-compose.yml` | ✨ Créé | Services MySQL |
| `user-service/src/test/resources/application-test-ci.properties` | ✨ Créé | Profil CI/CD |
| `exam-service/src/test/resources/application-test-ci.properties` | ✨ Créé | Profil CI/CD |
| `.gitlab-ci.yml` | ✏️ Modifié | Services MongoDB/MySQL ajoutés |

---

## 💡 Bonnes pratiques

1. ✅ **Tests unitaires rapides** (mocks, pas de base de données)
2. ✅ **Tests d'intégration locaux** (TestContainers + Docker)
3. ✅ **Tests d'intégration CI/CD** (Services GitLab + profils)
4. ✅ **Fallback gracieux** (continue sans Docker)
5. ✅ **Isolation par profil** (test, test-ci, test-unit)

---

## 📚 Ressources

- [TestContainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [GitLab CI/CD Services](https://docs.gitlab.com/ee/ci/services/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
