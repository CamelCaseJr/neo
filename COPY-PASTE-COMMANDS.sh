#!/bin/bash
# 🚀 COPIE E COLE ESTES COMANDOS NO WSL

# ═══════════════════════════════════════════════════════════════
# PASSO 1: Navegar para o diretório do projeto
# ═══════════════════════════════════════════════════════════════
cd /mnt/c/Users/junior/Documents/fiap/neo

# ═══════════════════════════════════════════════════════════════
# PASSO 2: Tornar scripts executáveis e executar migração
# ═══════════════════════════════════════════════════════════════
chmod +x migrate-to-multimodule.sh
chmod +x run-migration.sh

# Execute o script de migração
./migrate-to-multimodule.sh

# ═══════════════════════════════════════════════════════════════
# PASSO 3: Compilar todos os módulos
# ═══════════════════════════════════════════════════════════════
mvn clean install

# ═══════════════════════════════════════════════════════════════
# PASSO 4: Executar neo-core (TERMINAL 1)
# ═══════════════════════════════════════════════════════════════
# Abra um NOVO terminal WSL e execute:
cd /mnt/c/Users/junior/Documents/fiap/neo/neo-core
./mvnw quarkus:dev

# ═══════════════════════════════════════════════════════════════
# PASSO 5: Executar modelo-ia (TERMINAL 2)
# ═══════════════════════════════════════════════════════════════
# Abra outro NOVO terminal WSL e execute:
cd /mnt/c/Users/junior/Documents/fiap/neo/modelo-ia
../mvnw quarkus:dev -f pom.xml

# ═══════════════════════════════════════════════════════════════
# PASSO 6: Testar Endpoints (TERMINAL 3)
# ═══════════════════════════════════════════════════════════════
# Abra um terceiro terminal para testes:

# Testar neo-core
curl http://localhost:8080/hello
curl http://localhost:8080/api/neos

# Testar modelo-ia
curl http://localhost:8081/api/ia/health
curl http://localhost:8081/api/ia/info

# Testar previsão de perigosidade
curl "http://localhost:8081/api/ia/previsao/perigosidade?magnitude=19.5&diametro=250&velocidade=18.5"

# ═══════════════════════════════════════════════════════════════
# URLs PARA ABRIR NO NAVEGADOR
# ═══════════════════════════════════════════════════════════════
# neo-core Swagger:    http://localhost:8080/q/swagger-ui
# modelo-ia Swagger:   http://localhost:8081/q/swagger-ui
# neo-core Health:     http://localhost:8080/q/health
# modelo-ia Health:    http://localhost:8081/api/ia/health

# ═══════════════════════════════════════════════════════════════
# COMMIT COM GIT FLOW (Após validação)
# ═══════════════════════════════════════════════════════════════
git flow feature start multimodule-structure
git add .
git commit -m "refactor: migrar para estrutura multi-módulo

- Criar módulo neo-core com funcionalidades existentes
- Criar módulo modelo-ia para análise com IA
- Configurar POM pai para gerenciar dependências
- Adicionar serviço de previsão de perigosidade
- Documentar arquitetura e processo de migração"
git flow feature finish multimodule-structure
git push origin develop

# ═══════════════════════════════════════════════════════════════
# ✅ PRONTO! Sua aplicação está rodando em arquitetura multi-módulo
# ═══════════════════════════════════════════════════════════════
