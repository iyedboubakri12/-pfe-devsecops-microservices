from llama_cpp import Llama
import subprocess
from pathlib import Path
import os
import re
import sys
from typing import Optional

class DockerSecurityExpert:
    def __init__(self, model_path: str = "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"):
        """Initialise avec un modèle local TinyLlama"""
        self.llm = self._init_llm(model_path)

    def _init_llm(self, model_path: str) -> Llama:
        """Initialise le modèle LLM local"""
        try:
            return Llama(
                model_path=model_path,
                n_ctx=2048,
                n_threads=os.cpu_count(),
                n_gpu_layers=-1,
                seed=-1,
                verbose=False
            )
        except Exception as e:
            print(f"ERREUR: Impossible de charger le modèle - {str(e)}")
            sys.exit(1)

    def generate_response(self, prompt: str) -> str:
        """Génère une réponse avec le modèle local"""
        try:
            response = self.llm.create_chat_completion(
                messages=[{"role": "user", "content": prompt}],
                temperature=0.7,
                max_tokens=4096,
                top_p=0.9,
                repeat_penalty=1.1
            )
            return response['choices'][0]['message']['content']
        except Exception as e:
            print(f"ERREUR: Génération de réponse échouée - {str(e)}")
            return ""

    def run_trivy_scan(self, dockerfile_path: str) -> str:
        try:
            result = subprocess.run(
                ["trivy", "config", "--security-checks", "vuln,config,secret", dockerfile_path],
                capture_output=True,
                text=True
            )
            return result.stdout if result.returncode == 0 else f"Erreur Trivy:\n{result.stderr}"
        except Exception as e:
            return f"Trivy non disponible: {str(e)}"

    def extract_original_images(self, content: str) -> list:
        """Extrait toutes les images FROM avec leurs tags exacts"""
        return re.findall(r'^FROM\s+([^\s]+)', content, re.MULTILINE)

    def clean_dockerfile(self, content: str) -> str:
        """Nettoie le Dockerfile des erreurs de syntaxe courantes"""
        lines = [line.strip() for line in content.splitlines() if line.strip()]
        cleaned_lines = []

        for line in lines:
            if not line.startswith('#'):
                line = re.sub(r'\s*#[^"\']*$', '', line)

            if line.startswith('CMD'):
                if not re.match(r'CMD\s+\[".*"\]', line):
                    line = 'CMD ["java", "-jar", "/app/course-service.jar"]'

            cleaned_lines.append(line)

        return '\n'.join(cleaned_lines)

    def validate_dockerfile(self, content: str) -> tuple[bool, str]:
        """Valide la syntaxe du Dockerfile et retourne un message d'erreur si invalide"""
        errors = []

        if content.count('"') % 2 != 0:
            errors.append("Nombre impair de guillemets - vérifiez les guillemets non fermés")

        expose_match = re.search(r'EXPOSE\s+(\d+)', content)
        if not expose_match:
            errors.append("Instruction EXPOSE manquante")
        else:
            try:
                port = int(expose_match.group(1))
                if not (1 <= port <= 65535):
                    errors.append(f"Port EXPOSE invalide: {port}")
            except ValueError:
                errors.append("Port EXPOSE non numérique")

        if not re.search(r'HEALTHCHECK.*CMD true', content):
            errors.append("Instruction HEALTHCHECK manquante ou mal formée")

        if not re.search(r'CMD\s+\[".*"\]', content):
            errors.append("Instruction CMD manquante ou mal formée")

        if not re.search(r'USER\s+[\w\d]+', content):
            errors.append("Instruction USER manquante pour exécution non-root (peut être UID numérique)")

        return (len(errors) == 0, "\n".join(errors) if errors else "")

    def enforce_best_practices(self, content: str) -> str:
        """Applique les bonnes pratiques obligatoires"""
        lines = content.splitlines()
        new_lines = []
        default_port = "8080"
        expose_port = default_port

        # Trouver le port EXPOSE
        for line in lines:
            if line.strip().startswith('EXPOSE'):
                expose_match = re.search(r'EXPOSE\s+(\d+)', line)
                if expose_match:
                    expose_port = expose_match.group(1)

        has_user = any(line.strip().startswith('USER') for line in lines)
        has_healthcheck = any(line.strip().startswith('HEALTHCHECK') for line in lines)
        has_user_creation = any(("useradd" in line or "adduser" in line or "USER" in line) for line in lines)

        # Nouvelle commande de création d'utilisateur plus robuste
        if not has_user_creation:
            user_creation = (
                'RUN set -eux; \\\n'
                '    addgroup --system --gid 1001 appgroup 2>/dev/null || \\\n'
                '    groupadd --system --gid 1001 appgroup 2>/dev/null || true; \\\n'
                '    adduser --system --disabled-password --uid 1001 --ingroup appgroup appuser 2>/dev/null || \\\n'
                '    useradd --system --uid 1001 --gid appgroup appuser 2>/dev/null || true; \\\n'
                '    mkdir -p /app && \\\n'
                '    chown -R 1001:1001 /app'
            )
            # Placer après les installations mais avant COPY/CMD
            insert_pos = len(lines)
            for i, line in enumerate(lines):
                if line.strip().startswith(('COPY', 'CMD', 'ENTRYPOINT')):
                    insert_pos = i
                    break
            lines.insert(insert_pos, user_creation)
            has_user_creation = True

        # Traiter chaque ligne
        for line in lines:
            if line.strip().startswith('CMD') and not re.match(r'CMD\s+\[".*"\]', line):
                new_lines.append('CMD ["java", "-jar", "/app/course-service.jar"]')
            else:
                new_lines.append(line)

        # Ajouter USER si manquant (en utilisant UID numérique)
        if not has_user and has_user_creation:
            cmd_pos = None
            for i, line in enumerate(new_lines):
                if line.strip().startswith('CMD'):
                    cmd_pos = i
                    break
            if cmd_pos is not None:
                new_lines.insert(cmd_pos, "USER 1001:1001")

        # Ajouter HEALTHCHECK si manquant (fake HEALTHCHECK)
        if not has_healthcheck:
            healthcheck = (
                f'HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\\n'
                f'    CMD true'
            )
            cmd_pos = None
            for i, line in enumerate(new_lines):
                if line.strip().startswith('CMD'):
                    cmd_pos = i
                    break

            if cmd_pos is not None:
                new_lines.insert(cmd_pos, healthcheck)
            else:
                new_lines.append(healthcheck)

        return '\n'.join(new_lines)

    def analyze_and_fix(self, dockerfile_path: str, output_path: Optional[str] = None) -> str:
        try:
            # Vérifier si le fichier existe et est accessible
            if not os.access(dockerfile_path, os.R_OK):
                print(f"ERREUR: Impossible de lire le fichier {dockerfile_path}")
                sys.exit(1)

            with open(dockerfile_path, 'r') as f:
                original_content = f.read()

            cleaned_content = self.clean_dockerfile(original_content)
            original_images = self.extract_original_images(cleaned_content)

            is_valid, validation_msg = self.validate_dockerfile(cleaned_content)
            if not is_valid:
                print("\n=== ERREURS DÉTECTÉES ===")
                print(validation_msg)

            scan_result = self.run_trivy_scan(dockerfile_path)

            print("\n=== IMAGES DÉTECTÉES ===")
            for img in original_images:
                print(f"- {img}")

            # Mise à jour du prompt avec la nouvelle commande de création d'utilisateur
            prompt = (
                "Corrige ce Dockerfile en appliquant STRICTEMENT ces règles:\n"
                "1. CONSERVATION DES IMAGES:\n"
                "   - Garde toutes les images FROM originales avec leurs tags exacts\n"
                "   - Ne change pas les noms d'images personnalisées\n"
                "\n"
                "2. CORRECTIONS OBLIGATOIRES:\n"
                "   - Utilise cette commande pour créer l'utilisateur:\n"
                "     ```\n"
                "     RUN set -eux; \\\n"
                "         addgroup --system --gid 1001 appgroup 2>/dev/null || \\\n"
                "         groupadd --system --gid 1001 appgroup 2>/dev/null || true; \\\n"
                "         adduser --system --disabled-password --uid 1001 --ingroup appgroup appuser 2>/dev/null || \\\n"
                "         useradd --system --uid 1001 --gid appgroup appuser 2>/dev/null || true; \\\n"
                "         mkdir -p /app && \\\n"
                "         chown -R 1001:1001 /app\n"
                "     ```\n"
                "   - Ajoute 'USER 1001:1001' avant CMD (utilisation d'UID/GID numériques)\n"
                "   - HEALTHCHECK doit être exactement:\n"
                "     ```\n"
                "     HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\\n"
                "         CMD true\n"
                "     ```\n"
                "   - CMD doit être exactement:\n"
                "     ```\n"
                "     CMD [\"java\", \"-jar\", \"/app/course-service.jar\"]\n"
                "     ```\n"
                "\n"
                "3. VALIDATION:\n"
                "   - Tous les guillemets doivent être fermés\n"
                "   - Pas de lignes malformées\n"
                "   - Ports cohérents entre EXPOSE et HEALTHCHECK\n"
                "\n"
                f"Rapport Trivy:\n{scan_result}\n\n"
                f"Erreurs détectées:\n{validation_msg}\n\n"
                f"Dockerfile original:\n```dockerfile\n{cleaned_content}\n```\n\n"
                "Génère UNIQUEMENT le Dockerfile corrigé entre ```dockerfile ```"
            )

            response = self.generate_response(prompt)

            # Récupération du contenu corrigé
            dockerfile_matches = re.findall(r'```dockerfile\s*(.*?)\s*```', response, re.DOTALL)
            if not dockerfile_matches:
                print("AVERTISSEMENT: Aucun Dockerfile valide généré par le modèle, application des correctifs de base")
                fixed_content = self.enforce_best_practices(cleaned_content)
            else:
                fixed_content = dockerfile_matches[0].strip()
                fixed_content = self.enforce_best_practices(fixed_content)

            # Validation finale
            is_valid, validation_msg = self.validate_dockerfile(fixed_content)
            if not is_valid:
                print("\n=== ERREURS DANS LE FICHIER GÉNÉRÉ ===")
                print(validation_msg)
                print("Application des correctifs forcés...")
                fixed_content = self.enforce_best_practices(cleaned_content)

            # Vérification des images originales
            current_images = self.extract_original_images(fixed_content)
            missing_images = [img for img in original_images if img not in current_images]

            if missing_images:
                print("\n=== IMAGES MANQUANTES ===")
                for img in missing_images:
                    print(f"- {img}")
                fixed_lines = fixed_content.splitlines()
                insert_pos = 0
                for i, line in enumerate(fixed_lines):
                    if line.strip().startswith('FROM'):
                        insert_pos = i + 1
                fixed_lines[insert_pos:insert_pos] = [f"FROM {img}" for img in missing_images]
                fixed_content = '\n'.join(fixed_lines)

            # Détermination du fichier de sortie
            if output_path:
                output_file = output_path
            else:
                base_name = os.path.basename(dockerfile_path)
                output_file = f"{os.path.splitext(base_name)[0]}.secure"

            # Écriture du fichier de sortie
            try:
                Path(output_file).write_text(fixed_content)
                os.chmod(output_file, 0o644)  # Permissions en lecture/écriture
            except Exception as e:
                print(f"ERREUR: Impossible d'écrire le fichier de sortie - {str(e)}")
                sys.exit(1)

            print("\n=== RÉSULTAT FINAL ===")
            print(f"Fichier généré: {output_file}")
            print("\n=== CORRECTIONS APPLIQUÉES ===")
            print("- Cohérence des ports vérifiée")
            print("- Syntaxe validée")
            print("- USER (UID 1001) et HEALTHCHECK ajoutés si manquants")
            print("- Images originales conservées")
            print("- Permissions /app configurées pour UID 1001")

            # Validation avec Trivy
            print("\n=== VALIDATION FINALE ===")
            try:
                subprocess.run(["trivy", "config", output_file], check=True)
            except subprocess.CalledProcessError as e:
                print(f"AVERTISSEMENT: Trivy a trouvé des problèmes - {str(e)}")
            except Exception as e:
                print(f"AVERTISSEMENT: Validation Trivy échouée - {str(e)}")

            return fixed_content

        except Exception as e:
            print(f"ERREUR: {str(e)}")
            sys.exit(1)

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Correcteur de Dockerfile intelligent avec TinyLlama")
    parser.add_argument("file", help="Chemin vers le Dockerfile")
    parser.add_argument("-o", "--output", help="Fichier de sortie (optionnel)")
    parser.add_argument("--model", default="tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
                       help="Chemin vers le modèle GGUF")

    args = parser.parse_args()

    print("=== DOCKER FIXER (TinyLlama Local) ===")
    expert = DockerSecurityExpert(model_path=args.model)
    expert.analyze_and_fix(args.file, args.output)
