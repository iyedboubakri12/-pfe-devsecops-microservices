# Référentiel de Sécurité DevSecOps - SFM Technologies

## 1. Sécurité du Code Applicatif (Java Spring Boot)
- **Injection SQL (CWE-89) :** Interdiction formelle de concaténer des chaînes dans les requêtes SQL. Utiliser impérativement `PreparedStatement` ou Spring Data JPA.
- **Exposition Actuator (CWE-284) :** Les endpoints `/actuator` ne doivent jamais être exposés entièrement (`*`). Autoriser uniquement `health` et `info`.
- **Désérialisation (CWE-502) :** Éviter l'utilisation de `ObjectInputStream` sur des données non fiables. Préférer JSON avec Jackson configuré de manière sécurisée.
- **Gestion des logs :** Ne jamais logger des données sensibles (PII), des mots de passe ou des tokens JWT. Utiliser un masque de données si nécessaire.
- **Validation des entrées :** Utiliser les annotations `@Valid` et `@NotNull` pour valider systématiquement les DTO.

## 2. Sécurité Frontend (Angular)
- **Cross-Site Scripting (XSS) (CWE-79) :** Ne jamais utiliser `innerHTML` ou `bypassSecurityTrustHtml` sans assainissement (sanitization).
- **Communication API :** Utiliser impérativement des Intercepteurs pour ajouter les headers de sécurité (Content-Security-Policy).
- **Stockage local :** Interdiction de stocker des mots de passe ou des clés privées dans le `localStorage` ou `sessionStorage`. Utiliser des cookies HttpOnly pour les sessions.

## 3. Sécurité de l'Infrastructure (Kubernetes & OPA)
- **Privilèges Conteneur :** Aucun conteneur ne doit s'exécuter en tant que `root`. Ajouter `runAsNonRoot: true` dans le `securityContext`.
- **Système de fichiers :** Forcer l'immutabilité avec `readOnlyRootFilesystem: true`. Utiliser des volumes `emptyDir` pour les dossiers temporaires (/tmp, /var/cache).
- **Escalade de privilèges :** Positionner `allowPrivilegeEscalation: false` pour empêcher les processus enfants d'obtenir plus de droits que le parent.
- **Ressources (DoS Protection) :** Chaque Pod doit définir des `limits` et `requests` pour la mémoire et le CPU. (Standards SFM : RAM max 512Mi pour microservices).
- **Network Policies :** Par défaut, appliquer une règle "Deny All" et autoriser uniquement les flux nécessaires entre microservices via Ingress.

## 4. Sécurité des Conteneurs (Docker & SCA)
- **Images de base :** Utiliser des images minimalistes comme `alpine` ou `distroless` pour réduire la surface d'attaque.
- **Vérification des vulnérabilités :** Tout composant présentant une faille Trivy de sévérité `CRITICAL` doit être bloqué au déploiement.
- **Multi-stage Build :** Utiliser le multi-stage pour ne pas inclure les outils de build (Maven, JDK) dans l'image finale de production.

## 5. Gestion des Secrets (Gitleaks & Vault)
- **Secrets en clair :** Interdiction de commiter des fichiers `.env`, `.pem`, ou des mots de passe dans Git.
- **Rotation :** Les clés d'API Cloud (DigitalOcean) doivent être renouvelées tous les 90 jours.
- **Injection de secrets :** Utiliser les variables d'environnement GitLab masquées ou l'injection dynamique via HashiCorp Vault.

## 6. Surveillance Runtime (Falco)
- **Détection d'intrusion :** Toute modification de fichier dans `/etc` ou l'exécution d'un shell (`bin/bash`) dans un pod de production doit déclencher une alerte Slack immédiate.
- **Anomalie réseau :** Signaler toute connexion sortante d'un conteneur vers une IP publique non identifiée (potentiel exfiltration de données).