#!/bin/sh

# 1. Analyse du Backend (Java)
# On attend que le build Maven soit terminé pour analyser le JAR
echo "🔍 Analyse Qwiet.ai sur le Backend..."
sl analyze \
  --app "$CI_PROJECT_NAME-backend" \
  --tag branch="$CI_COMMIT_REF_NAME" \
  --wait \
  backend/user-service/target/*.jar # Remplacez par le chemin de l'un de vos JAR

# 2. Si c'est une Merge Request, on poste le rapport en commentaire
if [ -n "$CI_MERGE_REQUEST_IID" ]; then
  echo "📊 Génération du rapport pour la MR $CI_MERGE_REQUEST_IID"

  sl check-analysis \
    --app "$CI_PROJECT_NAME-backend" \
    --report \
    --report-file check-analysis.md \
    --source "tag.branch=main" \
    --target "tag.branch=$CI_COMMIT_REF_NAME"

  # On transforme le rapport en JSON pour l'API GitLab
  COMMENT_BODY=$(jq -n --arg body "$(cat check-analysis.md)" '{body: $body}')

  # On envoie le commentaire sur GitLab
  curl -X POST "https://gitlab.com/api/v4/projects/$CI_PROJECT_ID/merge_requests/$CI_MERGE_REQUEST_IID/notes" \
    -H "PRIVATE-TOKEN: $MR_TOKEN" \
    -H "Content-Type: application/json" \
    -d "$COMMENT_BODY"
fi