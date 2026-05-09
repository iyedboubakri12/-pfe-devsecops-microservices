import os
import json
import requests


def generate_remediation():
    # 1. Lecture des variables d'environnement
    api_key = os.getenv("GOOGLE_API_KEY", "").strip()
    slack_url = os.getenv("SLACK_WEBHOOK", "").strip()

    print("🤖 Agent IA SFM : Initialisation avec Gemini...")

    if not api_key or len(api_key) < 10:
        print(f"❌ Erreur : Clé API invalide. (Clé détectée : '{api_key}')")
        return

    # 2. URL corrigée : modèle gemini-2.0-flash (stable en 2025)
    MODEL = "gemini-2.0-flash"
    url = (
        f"https://generativelanguage.googleapis.com"
        f"/v1beta/models/{MODEL}:generateContent?key={api_key}"
    )

    # 3. Lecture des fichiers (RAG)
    try:
        with open("knowledge_base/sfm_security_standards.md", "r") as f:
            standards = f.read()
    except Exception as e:
        print(f"⚠️  Fichier standards introuvable : {e}")
        standards = "Standard : runAsNonRoot: true exigé."

    try:
        with open("opa-report.json", "r") as f:
            report = f.read()
    except Exception:
        report = "Alerte : Le déploiement 'frontend' tourne avec l'utilisateur ROOT."

    # 4. Préparation du prompt
    prompt_text = f"""Tu es l'Expert DevSecOps de SFM Technologies.
Basé sur nos STANDARDS :
{standards}

Analyse ce RAPPORT :
{report}

Donne le code CORRECT (YAML ou Java) et cite la règle exacte.
Réponds en Markdown compatible Slack."""

    payload = {
        "contents": [
            {
                "parts": [{"text": prompt_text}]
            }
        ]
    }

    # 5. Appel à l'API Gemini
    try:
        print(f"🔍 Analyse de sécurité via {MODEL}...")
        response = requests.post(url, json=payload, timeout=30)

        # Debug en cas d'erreur
        if not response.ok:
            print(f"❌ Erreur HTTP {response.status_code} : {response.text}")
            return

        data = response.json()

        # Extraction sécurisée de la réponse
        candidates = data.get("candidates", [])
        if not candidates:
            print("❌ Aucun candidat retourné par l'IA.")
            print(f"Réponse brute : {data}")
            return

        fix = candidates[0]["content"]["parts"][0]["text"]
        print("\n✅ Correction générée avec succès !\n")
        print(fix)

        # 6. Envoi vers Slack
        if slack_url:
            slack_payload = {"text": f"🤖 *IA REMEDIATION AGENT*\n\n{fix}"}
            r = requests.post(slack_url, json=slack_payload, timeout=10)
            if r.ok:
                print("\n🚀 Notification envoyée sur Slack !")
            else:
                print(f"⚠️  Slack a répondu : {r.status_code} - {r.text}")

    except requests.exceptions.Timeout:
        print("❌ Timeout : l'API Gemini ne répond pas.")
    except requests.exceptions.RequestException as e:
        print(f"❌ Erreur réseau : {e}")
    except (KeyError, IndexError) as e:
        print(f"❌ Structure de réponse inattendue : {e}")
        print(f"Réponse brute : {data}")


if __name__ == "__main__":
    generate_remediation()