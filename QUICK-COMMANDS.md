# ⚡ Comandos Rápidos - Multi-Módulo NEO

## 🚀 Migração Inicial

```bash
cd /mnt/c/Users/junior/Documents/fiap/neo

# Executar migração automática
chmod +x migrate-to-multimodule.sh
./migrate-to-multimodule.sh

# OU executar com assistente interativo
chmod +x run-migration.sh
./run-migration.sh
```

## 🔨 Build

```bash
# Build completo (todos os módulos)
mvn clean install

# Build apenas neo-core
cd neo-core && mvn clean install

# Build apenas modelo-ia
cd modelo-ia && mvn clean install -f pom.xml

# Build sem testes
mvn clean install -DskipTests
```

## ▶️ Executar Aplicações

### Opção 1: Ambos os módulos simultaneamente

**Terminal 1 (neo-core):**
```bash
cd /mnt/c/Users/junior/Documents/fiap/neo/neo-core
./mvnw quarkus:dev
```

**Terminal 2 (modelo-ia):**
```bash
cd /mnt/c/Users/junior/Documents/fiap/neo/modelo-ia
../mvnw quarkus:dev -f pom.xml
```

### Opção 2: Apenas um módulo

```bash
# Só neo-core
cd neo-core && ./mvnw quarkus:dev

# Só modelo-ia
cd modelo-ia && ../mvnw quarkus:dev -f pom.xml
```

## 🧪 Testes

```bash
# Testar neo-core
curl http://localhost:8080/hello
curl http://localhost:8080/api/neos

# Testar modelo-ia
curl http://localhost:8081/api/ia/health
curl http://localhost:8081/api/ia/info

# Previsão de perigosidade
curl "http://localhost:8081/api/ia/previsao/perigosidade?magnitude=19.5&diametro=250&velocidade=18.5"

# Importar dados da NASA (neo-core)
curl "http://localhost:8080/api/neos/importar?inicio=2024-01-01&fim=2024-01-07"
```

## 🌐 URLs Importantes

| Serviço | URL | Porta |
|---------|-----|-------|
| neo-core | http://localhost:8080 | 8080 |
| neo-core Swagger | http://localhost:8080/q/swagger-ui | 8080 |
| neo-core Health | http://localhost:8080/q/health | 8080 |
| modelo-ia | http://localhost:8081 | 8081 |
| modelo-ia Swagger | http://localhost:8081/q/swagger-ui | 8081 |
| modelo-ia Health | http://localhost:8081/api/ia/health | 8081 |

## 🐳 Docker (neo-core)

```bash
cd neo-core

# Subir PostgreSQL e MinIO
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar
docker-compose down

# Parar e remover volumes
docker-compose down -v
```

## 📝 Git Flow

```bash
# Iniciar feature
git flow feature start multimodule-structure

# Adicionar mudanças
git add .
git commit -m "refactor: migrar para arquitetura multi-módulo"

# Finalizar feature
git flow feature finish multimodule-structure

# Push
git push origin develop
```

## 🔍 Debug

### Ver logs detalhados
```bash
# neo-core com debug
cd neo-core
./mvnw quarkus:dev -Dquarkus.log.level=DEBUG

# modelo-ia com debug
cd modelo-ia
../mvnw quarkus:dev -f pom.xml -Dquarkus.log.level=DEBUG
```

### Limpar tudo e recompilar
```bash
# Limpar todos os módulos
mvn clean

# Recompilar
mvn clean install -DskipTests

# Forçar atualização de dependências
mvn clean install -U
```

## 📦 Packaging

```bash
# Gerar JARs
mvn package

# JARs estarão em:
# neo-core/target/quarkus-app/
# modelo-ia/target/quarkus-app/

# Executar JAR
java -jar neo-core/target/quarkus-app/quarkus-run.jar
java -jar modelo-ia/target/quarkus-app/quarkus-run.jar
```

## 🛠️ Utilitários

### Verificar estrutura
```bash
tree -L 3 -I 'target|node_modules'
```

### Verificar portas em uso
```bash
# Ver processos na porta 8080
lsof -i :8080

# Ver processos na porta 8081
lsof -i :8081

# Matar processo
kill -9 <PID>
```

### Maven Wrapper
```bash
# Reinstalar wrapper
mvn wrapper:wrapper

# Tornar executável
chmod +x mvnw
chmod +x neo-core/mvnw
chmod +x modelo-ia/mvnw
```

## 📊 Monitoramento

### Métricas Quarkus
```bash
# neo-core
curl http://localhost:8080/q/metrics

# modelo-ia
curl http://localhost:8081/q/metrics
```

### Health Check
```bash
# neo-core
curl http://localhost:8080/q/health
curl http://localhost:8080/q/health/live
curl http://localhost:8080/q/health/ready

# modelo-ia
curl http://localhost:8081/q/health
```

## 🔧 Troubleshooting

### Erro: "Port already in use"
```bash
# Matar processo na porta 8080
lsof -ti:8080 | xargs kill -9

# Matar processo na porta 8081
lsof -ti:8081 | xargs kill -9
```

### Erro: "Cannot find mvnw"
```bash
cd /mnt/c/Users/junior/Documents/fiap/neo
mvn wrapper:wrapper
chmod +x mvnw
cp mvnw neo-core/
cp mvnw modelo-ia/
```

### Erro: MapStruct não gera classes
```bash
cd neo-core
mvn clean compile
```

### Erro: PostgreSQL não conecta
```bash
cd neo-core
docker-compose up -d postgres
docker-compose logs postgres
```

## 📚 Documentação

```bash
# Ler documentação
cat README.md
cat MIGRATION-GUIDE.md
cat ARCHITECTURE.md
```

## 🎯 Atalhos Úteis no Quarkus Dev Mode

Quando rodando `quarkus:dev`, pressione:
- `h` - Ajuda
- `r` - Restart
- `d` - Debug
- `i` - Info
- `s` - Testes
- `w` - Rebuild
- `q` - Quit

## 🚀 Deploy em Produção (Futuro)

```bash
# Build nativo
mvn package -Pnative

# Docker build
docker build -f src/main/docker/Dockerfile.jvm -t neo-core:latest ./neo-core
docker build -f src/main/docker/Dockerfile.jvm -t modelo-ia:latest ./modelo-ia

# Kubernetes
kubectl apply -f k8s/neo-core/
kubectl apply -f k8s/modelo-ia/
```
