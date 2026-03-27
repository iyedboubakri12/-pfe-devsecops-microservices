apiVersion: templates.gatekeeper.sh/v1
kind: ConstraintTemplate
metadata:
  name: k8srequiredresources
spec:
  crd:
    spec:
      names:
        kind: K8sRequiredResources
  targets:
    - target: admission.k8s.gatekeeper.sh
      rego: |
        package k8srequiredresources
        violation[{"msg": msg}] {
          container := input.review.object.spec.template.spec.containers[_]
          not container.resources.limits
          msg := sprintf("🚨 SECURITE PFE : Le container <%v> n'a pas de limites de ressources. Refusé !", [container.name])
        }
        # 2. Règle : Interdiction de tourner en ROOT
        violation[{"msg": msg}] {
          container := input.review.object.spec.template.spec.containers[_]
          not container.securityContext.runAsNonRoot == true
          msg := sprintf("🚨 SECURITE PFE [PRIVILEGES] : Le container <%v> doit avoir 'runAsNonRoot: true'. Interdiction de tourner en ROOT !", [container.name])
        }

        # 3. Règle : Interdiction du mode PRIVILÉGIÉ
        violation[{"msg": msg}] {
          container := input.review.object.spec.template.spec.containers[_]
          container.securityContext.privileged == true
          msg := sprintf("🚨 SECURITE PFE [ESCAPE] : Le container <%v> est en mode PRIVILÉGIÉ. Risque d'évasion de container. Refusé !", [container.name])
        }
