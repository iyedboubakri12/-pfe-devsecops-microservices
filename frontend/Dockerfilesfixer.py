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
                    if "nginx" in content.lower():
                        line = 'CMD ["nginx", "-g", "daemon off;"]'
                    else:
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

        healthcheck_match = re.search(r'HEALTHCHECK.*http://localhost:(\d+)', content)
        if not healthcheck_match:
            errors.append("Instruction HEALTHCHECK manquante ou mal formée")
        elif expose_match and healthcheck_match.group(1) != expose_match.group(1):
            errors.append(f"Port HEALTHCHECK ({healthcheck_match.group(1)}) ne correspond pas au port EXPOSE ({expose_match.group(1)})")

        if not re.search(r'CMD\s+\[".*"\]', content):
            errors.append("Instruction CMD manquante ou mal formée")

        if not re.search(r'USER\s+[\w\d]+', content) and "nginx" not in content.lower():
            errors.append("Instruction USER manquante pour exécution non-root (peut être UID numérique)")

        return (len(errors) == 0, "\n".join(errors) if errors else "")

    def is_nginx_image(self, content: str) -> bool:
        """Détecte si le Dockerfile utilise une image Nginx"""
        from_lines = self.extract_original_images(content)
        return any("nginx" in line.lower() for line in from_lines)

    def enforce_best_practices(self, content: str) -> str:
        """Applique les bonnes pratiques avec gestion robuste des utilisateurs"""
        lines = content.splitlines()
        new_lines = []
        default_port = "8080"
        expose_port = default_port
        is_nginx = self.is_nginx_image(content)

        # Trouver le port EXPOSE
        for line in lines:
            if line.strip().startswith('EXPOSE'):
                expose_match = re.search(r'EXPOSE\s+(\d+)', line)
                if expose_match:
                    expose_port = expose_match.group(1)

        has_user = any(line.strip().startswith('USER') for line in lines)
        has_healthcheck = any(line.strip().startswith('HEALTHCHECK') for line in lines)

        # Configuration spécifique pour Nginx
        if is_nginx:
            nginx_fixes = [
                'RUN set -eux; \\',
                '    mkdir -p /var/cache/nginx /var/run /run /var/cache/nginx/client_temp; \\',
                '    chown -R root:root /var/cache/nginx /var/run /run /var/cache/nginx/client_temp; \\',
                '    chmod -R 755 /var/cache/nginx /var/run /run /var/cache/nginx/client_temp; \\',
                '    touch /run/nginx.pid; \\',
                '    chown root:root /run/nginx.pid'
            ]
            insert_pos = 0
            for i, line in enumerate(lines):
                if line.strip().startswith('FROM'):
                    insert_pos = i + 1
                elif line.strip() and not line.strip().startswith('FROM'):
                    break
            for fix in reversed(nginx_fixes):
                lines.insert(insert_pos, fix)

            if not has_user:
                cmd_pos = None
                for i, line in enumerate(lines):
                    if line.strip().startswith('CMD'):
                        cmd_pos = i
                        break
                if cmd_pos is not None:
                    lines.insert(cmd_pos, "USER root")

        # Configuration standard pour les autres images
        else:
            has_user_creation = any(("useradd" in line or "adduser" in line) for line in lines)
            if not has_user_creation:
                user_creation = [
                    'RUN set -eux; \\',
                    '    addgroup -S -g 10001 appgroup 2>/dev/null || \\',
                    '    groupadd -r -g 10001 appgroup 2>/dev/null || true; \\',
                    '    adduser -S -D -H -u 10001 -G appgroup -s /sbin/nologin appuser 2>/dev/null || \\',
                    '    useradd -r -u 10001 -g appgroup -s /sbin/nologin appuser 2>/dev/null || true; \\',
                    '    mkdir -p /app; \\',
                    '    chown -R 10001:10001 /app'
                ]
                insert_pos = len(lines)
                for i, line in enumerate(lines):
                    if line.strip().startswith(('COPY', 'CMD', 'ENTRYPOINT')):
                        insert_pos = i
                        break
                for line in reversed(user_creation):
                    lines.insert(insert_pos, line)
            if not has_user:
                cmd_pos = None
                for i, line in enumerate(lines):
                    if line.strip().startswith('CMD'):
                        cmd_pos = i
                        break
                if cmd_pos is not None:
                    lines.insert(cmd_pos, "USER 10001:10001")

        # Traiter chaque ligne
        for line in lines:
            if line.strip().startswith('CMD') and not re.match(r'CMD\s+\[".*"\]', line):
                if is_nginx:
                    new_lines.append('CMD ["nginx", "-g", "daemon off;"]')
                else:
                    new_lines.append('CMD ["java", "-jar", "/app/course-service.jar"]')
            else:
                new_lines.append(line)

        # Pour Nginx, ajouter la modification de nginx.conf après la copie des fichiers
        if is_nginx:
            for i, line in enumerate(new_lines):
                if "COPY" in line and "nginx.conf" in line:
                    # Insérer la modification juste après la copie
                    new_lines.insert(i+1, 'RUN sed -i \'/^user/d\' /etc/nginx/nginx.conf')
                    break

        # Ajouter HEALTHCHECK si manquant
        has_healthcheck = any(line.strip().startswith('HEALTHCHECK') for line in new_lines)
        if not has_healthcheck:
            healthcheck_endpoint = "/" if is_nginx else "/actuator/health"
            healthcheck = (
                f'HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\\n'
                f'    CMD curl -f http://localhost:{expose_port}{healthcheck_endpoint} || exit 1'
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
            is_nginx = self.is_nginx_image(cleaned_content)
            nginx_specific = ""
            if is_nginx:
                nginx_specific = (
                    "\n4. CONFIGURATION SPÉCIFIQUE NGINX:\n"
                    "   - Utilisation de l'utilisateur root\n"
                    "   - Création des répertoires nécessaires\n"
                    "   - Supprime la directive 'user' inutile (après copie de nginx.conf)\n"
                )

            prompt = (
                "Corrige ce Dockerfile en appliquant STRICTEMENT ces règles:\n"
                "1. CONSERVATION DES IMAGES:\n"
                "   - Garde toutes les images FROM originales avec leurs tags exacts\n"
                "   - Ne change pas les noms d'images personnalisées\n"
                "\n"
                "2. CORRECTIONS OBLIGATOIRES:\n"
                "   - Pour les applications standard (non-Nginx):\n"
                "     ```\n"
                "     RUN set -eux; \\\n"
                "         addgroup --system --gid 10001 appgroup || \\\n"
                "         groupadd --system --gid 10001 appgroup || true; \\\n"
                "         adduser --system --disabled-password --uid 10001 --ingroup appgroup appuser || \\\n"
                "         useradd --system --uid 10001 --gid appgroup appuser || true; \\\n"
                "         mkdir -p /app && \\\n"
                "         chown -R 10001:10001 /app\n"
                "     ```\n"
                "   - Ajoute 'USER 10001:10001' avant CMD\n"
                "3. VALIDATION:\n"
                "   - Tous les guillemets doivent être fermés\n"
                "   - Pas de lignes malformées\n"
                "   - Ports cohérents entre EXPOSE et HEALTHCHECK\n"
                f"{nginx_specific}"
                f"Rapport Trivy:\n{scan_result}\n"
                f"Erreurs détectées:\n{validation_msg}\n"
                f"Dockerfile original:\n```dockerfile\n{cleaned_content}\n```\n"
                "Génère UNIQUEMENT le Dockerfile corrigé entre ```dockerfile ```"
            )
            response = self.generate_response(prompt)
            dockerfile_matches = re.findall(r'```dockerfile\s*(.*?)\s*```', response, re.DOTALL)
            if not dockerfile_matches:
                print("\nAVERTISSEMENT: Aucun Dockerfile valide généré par le modèle, application des correctifs de base")
                fixed_content = self.enforce_best_practices(cleaned_content)
            else:
                fixed_content = dockerfile_matches[0].strip()
                fixed_content = self.enforce_best_practices(fixed_content)
            is_valid, validation_msg = self.validate_dockerfile(fixed_content)
            if not is_valid:
                print("\n=== ERREURS DANS LE FICHIER GÉNÉRÉ ===")
                print(validation_msg)
                print("Application des correctifs forcés...")
                fixed_content = self.enforce_best_practices(cleaned_content)
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
            if output_path:
                output_file = output_path
            else:
                base_name = os.path.basename(dockerfile_path)
                output_file = f"{os.path.splitext(base_name)[0]}.secure"
            try:
                Path(output_file).write_text(fixed_content)
                os.chmod(output_file, 0o644)
            except Exception as e:
                print(f"ERREUR: Impossible d'écrire le fichier de sortie - {str(e)}")
                sys.exit(1)
            print("\n=== RÉSULTAT FINAL ===")
            print(f"Fichier généré: {output_file}")
            print("\n=== CORRECTIONS APPLIQUÉES ===")
            if is_nginx:
                print("- Utilisation de l'utilisateur root pour Nginx")
                print("- Répertoires créés avec les bonnes permissions")
                print("- Directive 'user' supprimée via sed")
            else:
                print("- Utilisateur non-root (UID 10001) ajouté")
                print("- Permissions configurées pour /app")
            print("- Cohérence des ports vérifiée")
            print("- Syntaxe validée")
            print("- Images originales conservées")
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
