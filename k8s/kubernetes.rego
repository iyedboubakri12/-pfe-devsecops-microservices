package main

# 1. Règle : Interdire les containers sans limites de RAM/CPU
deny contains msg if {
  input.kind == "Deployment"
  container := input.spec.template.spec.containers[_]
  not container.resources.limits
  msg := sprintf("🚨 OPA : Le container <%v> n'a pas de limites de ressources dans %v", [container.name, input.metadata.name])
}

# 2. Règle : Interdire de tourner en ROOT
deny contains msg if {
  input.kind == "Deployment"
  container := input.spec.template.spec.containers[_]
  not container.securityContext.runAsNonRoot == true
  msg := sprintf("🚨 OPA : Le container <%v> doit avoir 'runAsNonRoot: true'", [container.name])
}

# 3. Règle : Interdire le mode PRIVILÉGIÉ
deny contains msg if {
  input.kind == "Deployment"
  container := input.spec.template.spec.containers[_]
  container.securityContext.privileged == true
  msg := sprintf("🚨 OPA : Le container <%v> est en mode PRIVILÉGIÉ. Refusé !", [container.name])
}
