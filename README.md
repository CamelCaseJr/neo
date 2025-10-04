# NEO Multi-Module Project

Projeto multi-módulo Quarkus para gerenciamento de NEOs (Near Earth Objects) e análise com modelos de IA.

## Estrutura do Projeto

```
neo-parent/
├── neo-core/           # Módulo principal - API REST, persistência, integração NASA
│   ├── src/main/java/org/acme/
│   │   ├── controller/    # Controllers REST
│   │   ├── service/       # Serviços de negócio
│   │   ├── repository/    # Repositórios JPA
│   │   ├── domain/        # Modelos e DTOs
│   │   └── config/        # Configurações
│   └── application.properties (porta 8080)
│
├── modelo-ia/          # Módulo de IA - Análise e previsão
│   ├── src/main/java/org/acme/ia/
│   │   ├── controller/    # Controllers de IA
│   │   ├── service/       # Serviços de ML/IA
│   │   └── dto/           # DTOs específicos de IA
│   └── application.properties (porta 8081)
│
└── pom.xml             # POM pai (agregador)
```

## Módulos

### 1. **neo-core** (porta 8080)
Módulo principal contendo:
- ✅ API REST para gerenciamento de NEOs
- ✅ Integração com NASA NeoWs API
- ✅ Persistência PostgreSQL com Flyway
- ✅ Armazenamento MinIO (S3)
- ✅ Exportação CSV dos dados
- ✅ Retry com exponential backoff para rate limit

**Endpoints principais:**
- `GET /api/neos` - Listar NEOs
- `GET /api/neos/importar` - Importar dados da NASA
- `GET /api/neos/perigosos` - Listar NEOs perigosos

### 2. **modelo-ia** (porta 8081)
Módulo de Inteligência Artificial contendo:
- ✅ Serviço de previsão de perigosidade
- ✅ Cálculo de score de risco
- ✅ API REST para análise de NEOs
- 🔄 Integração com neo-core (via dependência Maven)

**Endpoints principais:**
- `GET /api/ia/health` - Status do módulo
- `GET /api/ia/info` - Informações do módulo
- `GET /api/ia/previsao/perigosidade` - Prever perigosidade de um NEO

## Como Compilar

### Compilar todos os módulos
```bash
cd /mnt/c/Users/junior/Documents/fiap/neo
mvn clean install -f pom-parent.xml
```

### Compilar apenas neo-core
```bash
mvn clean install -f neo-core/pom.xml
```

### Compilar apenas modelo-ia
```bash
mvn clean install -f modelo-ia/pom.xml
```

## Como Executar

### Executar neo-core
```bash
cd neo-core
./mvnw quarkus:dev
# Ou no Windows:
mvnw.cmd quarkus:dev
```
Acesse: http://localhost:8080

### Executar modelo-ia
```bash
cd modelo-ia
../mvnw quarkus:dev -f pom.xml
# Ou no Windows:
..\mvnw.cmd quarkus:dev -f pom.xml
```
Acesse: http://localhost:8081

### Executar ambos simultaneamente
Terminal 1:
```bash
cd neo-core
./mvnw quarkus:dev
```

Terminal 2:
```bash
cd modelo-ia
../mvnw quarkus:dev -f pom.xml
```

## Migração Realizada

### O que foi feito:
1. ✅ Criado POM pai (`pom-parent.xml`) para gerenciar dependências
2. ✅ Movido código existente para módulo `neo-core/`
3. ✅ Criado novo módulo `modelo-ia/` com estrutura básica
4. ✅ Configurado portas diferentes (8080 e 8081)
5. ✅ Criado serviço de previsão de perigosidade com IA
6. ✅ Estabelecida dependência entre módulos

### Próximos passos para migração completa:

```bash
# 1. Renomear POM pai
mv pom-parent.xml pom.xml

# 2. Mover código para neo-core
mkdir -p neo-core/src
mv src/* neo-core/src/
mv mvnw neo-core/
mv mvnw.cmd neo-core/

# 3. Mover arquivos de configuração
mv docker-compose.yml neo-core/
mv README.md README-old.md
```

## Exemplo de Uso - API de Previsão IA

```bash
# Prever perigosidade de um NEO
curl "http://localhost:8081/api/ia/previsao/perigosidade?magnitude=19.5&diametro=250&velocidade=18.5"

# Resposta:
{
  "ehPerigoso": true,
  "scoreRisco": 65.5,
  "justificativa": "Análise baseada em: Magnitude=19.50, Diâmetro=250.00 m, Velocidade=18.50 km/s. Score de risco: 65.5%"
}
```

## Tecnologias

- **Quarkus 3.28.1** - Framework Java
- **Java 17** - Linguagem
- **PostgreSQL** - Banco de dados
- **MinIO/S3** - Object storage
- **Maven** - Build tool
- **MapStruct** - Object mapping
- **Flyway** - Database migrations
- **Jackson** - JSON/CSV processing

## Dependências entre Módulos

```
modelo-ia --> neo-core
    (pode usar classes, services e DTOs do neo-core)
```

## Swagger UI

- **neo-core**: http://localhost:8080/q/swagger-ui
- **modelo-ia**: http://localhost:8081/q/swagger-ui

## Observações

- Os módulos podem ser executados independentemente
- `modelo-ia` tem acesso às classes do `neo-core` via dependência Maven
- Cada módulo tem seu próprio `application.properties`
- O POM pai gerencia versões de todas as dependências
