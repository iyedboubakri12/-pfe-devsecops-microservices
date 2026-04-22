#!/bin/sh

# Liste de tes microservices (doivent correspondre aux noms de tes dossiers dans /backend)
SERVICES="user-service exam-service answer-service course-service api-gateway-service eureka-service"

echo "🔍 Démarrage de l'analyse globale Qwiet.ai pour l'architecture microservices..."

for service in $SERVICES; do
    APP_NAME="pfe-$service"
    JAR_PATH="backend/$service/target/$service-0.0.1-SNAPSHOT.jar"

    echo "---------------------------------------------------------"
    echo "📦 Analyse du service : $service"
    echo "---------------------------------------------------------"

    # 1. Lancement de l'analyse Qwiet.ai
    # On utilise un nom d'app unique pour chaque service dans le dashboard
    sl analyze --app "$APP_NAME" \
               --tag branch="$CI_COMMIT_REF_NAME" \
               --wait \
               --java "$JAR_PATH"

    # 2. Gestion spécifique pour les Merge Requests (MR)
    if [ -n "$CI_MERGE_REQUEST_IID" ]; then
        echo "📊 Génération du rapport de sécurité pour la MR..."
        
        REPORT_FILE="report-$service.md"
        
        sl check-analysis --app "$APP_NAME" \
                          --report \
                          --report-file "$REPORT_FILE" \
                          --source "tag.branch=main" \
                          --target "tag.branch=$CI_COMMIT_REF_NAME"

        # On prépare le message pour GitLab
        if [ -f "$REPORT_FILE" ]; then
            COMMENT_HEADER="### 🛡️ Rapport de Sécurité Qwiet.ai : $service"
            { echo "$COMMENT_HEADER"; cat "$REPORT_FILE"; } > "full-$REPORT_FILE"
            
            COMMENT_BODY=$(jq -n --arg body "$(cat full-$REPORT_FILE)" '{body: $body}')

            # Publication du commentaire sur la MR
            curl -X POST "https://gitlab.com/api/v4/projects/$CI_PROJECT_ID/merge_requests/$CI_MERGE_REQUEST_IID/notes" \
              -H "PRIVATE-TOKEN: $MR_TOKEN" \
              -H "Content-Type: application/json" \
              -d "$COMMENT_BODY"
        fi
    fi
done

echo "✅ Toutes les analyses sont terminées."