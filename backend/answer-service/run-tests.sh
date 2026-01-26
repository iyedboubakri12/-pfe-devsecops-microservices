#!/bin/bash
# Script pour exécuter les tests d'intégration de answer-service

echo "🚀 Démarrage des services de test..."

# Vérifier si docker-compose est disponible
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose n'est pas installé. Veuillez l'installer."
    exit 1
fi

# Démarrer les services
echo "📦 Démarrage de MongoDB..."
docker-compose up -d

# Attendre que MongoDB soit prêt
echo "⏳ Attente du démarrage de MongoDB (10 secondes)..."
sleep 10

# Exécuter les tests
echo "🧪 Exécution des tests..."
mvn clean test -DskipTests=false

# Arrêter les services
echo "🛑 Arrêt des services..."
docker-compose down

echo "✅ Tests terminés!"
