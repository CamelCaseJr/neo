# 🚀 Guia de Migração para Multi-Módulo

## Passo a Passo para Migração

### 1️⃣ Executar o script de migração automática

```bash
cd /mnt/c/Users/junior/Documents/fiap/neo
chmod +x migrate-to-multimodule.sh
./migrate-to-multimodule.sh
```

### 2️⃣ Verificar a estrutura criada

```bash
tree -L 2
# Deve mostrar:
# .
# ├── pom.xml                 # POM pai
# ├── neo-core/               # Módulo principal
# │   ├── pom.xml
# │   └── src/
# └── modelo-ia/              # Módulo IA
#     ├── pom.xml
#     └── src/
```

### 3️⃣ Compilar todos os módulos

```bash
mvn clean install
```

Se houver erro sobre mvnw, execute:
```bash
mvn wrapper:wrapper
```

### 4️⃣ Testar neo-core

**Terminal 1:**
```bash
cd neo-core
./mvnw quarkus:dev
```

**Acessar:**
- http://localhost:8080
- http://localhost:8080/q/swagger-ui

**Testar endpoint:**
```bash
curl http://localhost:8080/hello
```

### 5️⃣ Testar modelo-ia

**Terminal 2:**
```bash
cd modelo-ia
../mvnw quarkus:dev -f pom.xml
```

**Acessar:**
- http://localhost:8081
- http://localhost:8081/q/swagger-ui

**Testar endpoints:**
```bash
# Health check
curl http://localhost:8081/api/ia/health

# Info
curl http://localhost:8081/api/ia/info

# Previsão de perigosidade
curl "http://localhost:8081/api/ia/previsao/perigosidade?magnitude=19.5&diametro=250&velocidade=18.5"
```

### 6️⃣ Testar integração entre módulos

O módulo `modelo-ia` pode importar classes do `neo-core`:

**Exemplo em modelo-ia:**
```java
import org.acme.domain.models.NeoObject;  // Do neo-core
import org.acme.domain.dtos.FeedResponse;  // Do neo-core
```

## ⚠️ Solução de Problemas

### Problema 1: Maven Wrapper não encontrado
```bash
mvn wrapper:wrapper
chmod +x mvnw
```

### Problema 2: Porta já em uso
Editar `application.properties` de cada módulo:
```properties
# neo-core
quarkus.http.port=8080

# modelo-ia
quarkus.http.port=8081
```

### Problema 3: Erro de compilação MapStruct
```bash
cd neo-core
mvn clean compile
```

### Problema 4: Banco de dados não conecta
Verificar se o PostgreSQL está rodando:
```bash
cd neo-core
docker-compose up -d
```

## 📋 Checklist de Migração

- [ ] Script de migração executado com sucesso
- [ ] Estrutura de diretórios criada
- [ ] Compilação `mvn clean install` bem-sucedida
- [ ] neo-core inicia na porta 8080
- [ ] modelo-ia inicia na porta 8081
- [ ] Swagger UI acessível em ambos os módulos
- [ ] Endpoint de health do modelo-ia responde
- [ ] Endpoint de previsão funciona

## 🎯 Após Migração

### Git Flow
```bash
# Iniciar feature
git flow feature start multimodule-refactor

# Adicionar arquivos
git add .
git commit -m "refactor: migrar para estrutura multi-módulo

- Criar módulo neo-core com funcionalidades existentes
- Criar módulo modelo-ia para análise com IA
- Configurar POM pai para gerenciar dependências
- Adicionar serviço de previsão de perigosidade"

# Finalizar feature
git flow feature finish multimodule-refactor
```

### Deploy
Cada módulo pode ser deployado independentemente:

```bash
# Build do neo-core
cd neo-core
./mvnw package

# Build do modelo-ia
cd modelo-ia
../mvnw package -f pom.xml
```

### Docker (futuro)
Criar Dockerfiles separados para cada módulo:
- `neo-core/src/main/docker/Dockerfile.jvm`
- `modelo-ia/src/main/docker/Dockerfile.jvm`

## 📚 Documentação

- [README Principal](README.md)
- [README neo-core](neo-core/README-original.md)
- Swagger UI: http://localhost:8080/q/swagger-ui e http://localhost:8081/q/swagger-ui

## 🤝 Contribuindo

Ao adicionar novas funcionalidades:
1. Decida qual módulo é mais apropriado
2. Funcionalidades de negócio → `neo-core`
3. Funcionalidades de IA/ML → `modelo-ia`
4. Mantenha as portas diferentes
5. Use dependências do POM pai
