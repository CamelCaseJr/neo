#!/bin/bash
# 🚀 Comandos para executar a migração no WSL

echo "════════════════════════════════════════════════════════════════"
echo "  🌟 Migração para Estrutura Multi-Módulo - NEO Project"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variáveis
ROOT_DIR="/mnt/c/Users/junior/Documents/fiap/neo"

echo -e "${BLUE}📂 Diretório do projeto:${NC} $ROOT_DIR"
echo ""

# Verificar se está no diretório correto
cd "$ROOT_DIR" || exit 1

echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  PASSO 1: Executar Script de Migração${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo "Execute os seguintes comandos:"
echo ""
echo -e "${GREEN}chmod +x migrate-to-multimodule.sh${NC}"
echo -e "${GREEN}./migrate-to-multimodule.sh${NC}"
echo ""
read -p "Pressione ENTER para executar..."

chmod +x migrate-to-multimodule.sh
./migrate-to-multimodule.sh

echo ""
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  PASSO 2: Verificar Estrutura Criada${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo "Estrutura esperada:"
echo ""
tree -L 2 -I 'target|node_modules'
echo ""
read -p "Pressione ENTER para continuar..."

echo ""
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  PASSO 3: Compilar Todos os Módulos${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${GREEN}mvn clean install${NC}"
echo ""
read -p "Pressione ENTER para compilar..."

mvn clean install

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Compilação bem-sucedida!${NC}"
else
    echo ""
    echo -e "${RED}❌ Erro na compilação. Verifique os logs acima.${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  PRÓXIMOS PASSOS${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${BLUE}Para executar os módulos, abra 2 terminais:${NC}"
echo ""
echo -e "${GREEN}Terminal 1 - neo-core (porta 8080):${NC}"
echo "  cd $ROOT_DIR/neo-core"
echo "  ./mvnw quarkus:dev"
echo ""
echo -e "${GREEN}Terminal 2 - modelo-ia (porta 8081):${NC}"
echo "  cd $ROOT_DIR/modelo-ia"
echo "  ../mvnw quarkus:dev -f pom.xml"
echo ""
echo -e "${BLUE}URLs para testar:${NC}"
echo "  • neo-core: http://localhost:8080"
echo "  • neo-core Swagger: http://localhost:8080/q/swagger-ui"
echo "  • modelo-ia: http://localhost:8081"
echo "  • modelo-ia Swagger: http://localhost:8081/q/swagger-ui"
echo ""
echo -e "${BLUE}Testes rápidos:${NC}"
echo "  curl http://localhost:8080/hello"
echo "  curl http://localhost:8081/api/ia/health"
echo "  curl http://localhost:8081/api/ia/info"
echo "  curl 'http://localhost:8081/api/ia/previsao/perigosidade?magnitude=19.5&diametro=250&velocidade=18.5'"
echo ""
echo -e "${BLUE}📚 Documentação:${NC}"
echo "  • README.md - Visão geral"
echo "  • MIGRATION-GUIDE.md - Guia de migração"
echo "  • ARCHITECTURE.md - Arquitetura detalhada"
echo ""
echo -e "${GREEN}✨ Migração concluída com sucesso!${NC}"
echo ""
