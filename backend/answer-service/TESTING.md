# Answer Service - Tests d'Intégration

## Problème résolu
Les tests d'intégration échouaient avec l'erreur `IllegalState: Failed to load` car TestContainers ne pouvait pas accéder à Docker en environnement CI/CD.

## Solutions implémentées

### 1. **En local (avec Docker Desktop)**
```bash
# Démarrer MongoDB
docker-compose up -d

# Exécuter les tests
mvn clean test

# Arrêter MongoDB
docker-compose down
```

Ou utiliser le script automatisé:
```bash
bash run-tests.sh
```

### 2. **En CI/CD (GitLab, GitHub, etc.)**

Ajouter à votre `.gitlab-ci.yml` ou `.github/workflows/`:

```yaml
# Pour GitLab CI
services:
  - mongo:4.4

variables:
  SPRING_DATA_MONGODB_URI: "mongodb://mongo:27017/answer-service"

test:
  script:
    - mvn clean test -Dspring.profiles.active=test-ci
```

Ou:

```yaml
# Pour GitHub Actions
services:
  mongo:
    image: mongo:4.4
    options: >-
      --health-cmd "mongosh --eval \"db.adminCommand('ping')\"" 
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
    ports:
      - 27017:27017

env:
  SPRING_DATA_MONGODB_URI: mongodb://localhost:27017/answer-service
```

### 3. **Profils de test disponibles**

- `test`: Tests d'intégration avec TestContainers (local)
- `test-unit`: Tests unitaires uniquement (mock)
- `test-ci`: Tests avec MongoDB localhost (CI/CD)

```bash
# Tests unitaires uniquement
mvn clean test -Dspring.profiles.active=test-unit -DskipIntegrationTests

# Tests d'intégration
mvn clean test -Dspring.profiles.active=test
```

### 4. **Configuration TestContainers**

La classe `TestContainersConfig.java` inclut maintenant un fallback:
- Si Docker est disponible: crée un conteneur MongoDB dynamique ✅
- Si Docker n'est pas disponible: utilise MongoDB sur `localhost:27017` ⚠️

## Vérification

```bash
# Vérifier que MongoDB est en cours d'exécution
mongosh

# Vérifier la connexion
db.adminCommand('ping')
```

## Points clés

✅ Tests locaux: Docker Desktop + TestContainers
✅ Tests CI/CD: MongoDB service intégré
✅ Fallback automatique si Docker indisponible
✅ Configuration par profil Spring
✅ Script d'automatisation bash
