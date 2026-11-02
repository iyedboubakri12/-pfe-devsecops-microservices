"""AI remediation agent - Groq / Llama 3.3 70B variant.

This is the version of the agent documented in the PFE report (annex
"Agent de remediation IA - remediate.py"). It reads the OPA/Conftest report
produced by the pipeline, grounds the prompt in the internal security
standards (knowledge_base/), asks Llama 3.3 70B through the Groq API for a
fix, and posts the result to Slack.

Required environment variables (set as masked CI/CD variables):
  GROQ_API_KEY       Groq API key
  SLACK_WEBHOOK_URL  Slack incoming webhook (optional)
"""
import os
import json
import requests

GROQ_API_KEY  = os.environ.get("GROQ_API_KEY", "")
SLACK_WEBHOOK = os.environ.get("SLACK_WEBHOOK_URL", "")
GROQ_URL      = "https://api.groq.com/openai/v1/chat/completions"
GROQ_MODEL    = "llama-3.3-70b-versatile"

KNOWLEDGE_BASE  = "ai-remediator/knowledge_base/sfm_security_standards.md"
OPA_REPORT      = "opa-report.json"

def load_context():
    with open(KNOWLEDGE_BASE, "r", encoding="utf-8") as f:
        standards = f.read()
    try:
        with open(OPA_REPORT, "r", encoding="utf-8") as f:
            opa = json.load(f)
    except FileNotFoundError:
        opa = {"violations": []}
    return standards, json.dumps(opa, indent=2)

def ask_llm(standards, opa_violations):
    prompt = f"""Tu es un expert DevSecOps.
Voici les standards de sécurité SFM :
{standards}

Voici les violations OPA détectées dans le pipeline CI/CD :
{opa_violations}

Pour chaque violation, propose un correctif YAML concis et explique
pourquoi il est nécessaire en référençant le standard SFM applicable.
Réponds en français, avec formatage Markdown."""
    headers = {"Authorization": f"Bearer {GROQ_API_KEY}"}
    payload = {"model": GROQ_MODEL,
               "messages": [{"role": "user", "content": prompt}]}
    resp = requests.post(GROQ_URL, headers=headers, json=payload, timeout=30)
    resp.raise_for_status()
    return resp.json()["choices"][0]["message"]["content"]

def notify_slack(message):
    if not SLACK_WEBHOOK:
        print("SLACK_WEBHOOK non configuré, skipping.")
        return
    payload = {"text": f"*Remédiation IA — Résultats*\n{message}"}
    requests.post(SLACK_WEBHOOK, json=payload, timeout=10)

if __name__ == "__main__":
    standards, opa = load_context()
    remediation    = ask_llm(standards, opa)
    print(remediation)
    notify_slack(remediation)
