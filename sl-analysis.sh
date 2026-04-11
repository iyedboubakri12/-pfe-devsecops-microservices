#!/bin/sh

echo "🔍 Démarrage de l'analyse Qwiet.ai..."

# On lance l'analyse sur le JAR spécifique
# Assurez-vous que le chemin vers le JAR est exactement celui-ci
sl analyze --app "$CI_PROJECT_NAME-backend" --tag branch="$CI_COMMIT_REF_NAME" --wait --java backend/user-service/target/user-service-0.0.1-SNAPSHOT.jar

# Si nous sommes dans une Merge Request (MR), on génère et poste le rapport
if [ -n "$CI_MERGE_REQUEST_IID" ]; then
    echo "📊 Merge Request détectée (#$CI_MERGE_REQUEST_IID). Comparaison avec main..."
    
    sl check-analysis --app "$CI_PROJECT_NAME-backend" --report --report-file check-analysis.md --source "tag.branch=main" --target "tag.branch=$CI_COMMIT_REF_NAME"

    # Transformation du rapport Markdown en format JSON pour GitLab
    COMMENT_BODY=$(jq -n --arg body "$(cat check-analysis.md)" '{body: $body}')

    # Publication du commentaire sur la MR
    curl -X POST "https://gitlab.com/api/v4/projects/$CI_PROJECT_ID/merge_requests/$CI_MERGE_REQUEST_IID/notes" \
      -H "PRIVATE-TOKEN: $MR_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$COMMENT_BODY"
fi