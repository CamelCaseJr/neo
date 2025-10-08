# 🏗️ Arquitetura Multi-Módulo NEO

## Visão Geral

```
┌─────────────────────────────────────────────────────────────────┐
│                        NEO Parent Project                        │
│                         (Agregador Maven)                        │
└─────────────────────────────────────────────────────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 │                               │
                 ▼                               ▼
┌────────────────────────────────┐  ┌──────────────────────────────┐
│         NEO-CORE               │  │       MODELO-IA              │
│       (Porta 8080)             │◄─┤      (Porta 8081)            │
│                                │  │                              │
│  • API REST NEOs               │  │  • Previsão Perigosidade     │
│  • Integração NASA             │  │  • Score de Risco            │
│  • Persistência PostgreSQL     │  │  • Análise ML/IA             │
│  • Storage MinIO/S3            │  │  • Integra com neo-core      │
│  • Exportação CSV              │  │                              │
│  • Retry para Rate Limit       │  │                              │
└────────────────────────────────┘  └──────────────────────────────┘
         │                                        │
         ▼                                        ▼
┌────────────────────┐                  ┌──────────────────┐
│    PostgreSQL      │                  │   (Futuramente)  │
│                    │                  │   Modelos ML/IA  │
│  • Tabela neo      │                  │   • TensorFlow   │
│  • Flyway          │                  │   • DJL          │
└────────────────────┘                  │   • ONNX         │
         │                               └──────────────────┘
         ▼
┌────────────────────┐
│      MinIO/S3      │
│                    │
│  • JSON Raw        │
│  • CSV Export      │
└────────────────────┘
```

## Fluxo de Dados

### 1. Importação de Dados (neo-core)
```
NASA API → neo-core → PostgreSQL
              ↓
           MinIO/S3 (CSV)
```

### 2. Análise com IA (modelo-ia)
```
Cliente → modelo-ia → PrevisaoService
              ↓
         (pode consultar)
              ↓
          neo-core → PostgreSQL
```

## Comunicação Entre Módulos

### Opção 1: Dependência Maven (Atual)
```java
// Em modelo-ia
import org.acme.domain.models.NeoObject;  // Do neo-core
import org.acme.service.NeoService;        // Do neo-core

@Inject
NeoService neoService;  // Injeta serviço do neo-core
```

### Opção 2: REST API (Futuro)
```java
// Em modelo-ia
@RestClient
NeoCoreClient neoClient;  // Chamada HTTP para neo-core:8080
```

## Estrutura de Pacotes

### neo-core
```
org.acme
├── controller/
│   ├── NeoController.java
│   └── NeoWsClient.java
├── service/
│   ├── NeoService.java
│   └── ArmazenamentoMinioService.java
├── repository/
│   └── NeoRepository.java
├── domain/
│   ├── models/
│   │   └── NeoObject.java
│   ├── dtos/
│   │   ├── FeedResponse.java
│   │   └── NeoObjectResponse.java
│   └── mapper/
│       └── NeoObjectMapper.java
└── config/
    └── RestClientsConfig.java
```

### modelo-ia
```
org.acme.ia
├── controller/
│   └── PrevisaoController.java
├── service/
│   └── PrevisaoPerigosidadeService.java
└── dto/
    └── PrevisaoResponse.java
```

## Endpoints

### neo-core (http://localhost:8080)
```
GET  /api/neos                    # Listar todos os NEOs
GET  /api/neos/{id}               # Obter NEO por ID
GET  /api/neos/perigosos          # Listar NEOs perigosos
GET  /api/neos/importar           # Importar da NASA
POST /api/neos                    # Criar NEO
PUT  /api/neos/{id}               # Atualizar NEO
DELETE /api/neos/{id}             # Deletar NEO
```

### modelo-ia (http://localhost:8081)
```
GET /api/ia/health                # Status do módulo
GET /api/ia/info                  # Informações
GET /api/ia/previsao/perigosidade # Prever perigosidade
```

## Dependências Maven

### POM Pai (Aggregator)
```xml
<modules>
    <module>neo-core</module>
    <module>modelo-ia</module>
</modules>
```

### neo-core/pom.xml
```xml
<parent>
    <groupId>org.acme</groupId>
    <artifactId>neo-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>

<artifactId>neo-core</artifactId>
```

### modelo-ia/pom.xml
```xml
<parent>
    <groupId>org.acme</groupId>
    <artifactId>neo-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>

<artifactId>modelo-ia</artifactId>

<dependencies>
    <!-- Depende do neo-core -->
    <dependency>
        <groupId>org.acme</groupId>
        <artifactId>neo-core</artifactId>
    </dependency>
</dependencies>
```

## Benefícios da Arquitetura Multi-Módulo

✅ **Separação de Responsabilidades**
- neo-core: Operações de negócio e persistência
- modelo-ia: Análise e machine learning

✅ **Desenvolvimento Paralelo**
- Times diferentes podem trabalhar em módulos diferentes

✅ **Deploy Independente**
- Cada módulo pode ser deployado separadamente

✅ **Reutilização de Código**
- modelo-ia reutiliza DTOs e modelos do neo-core

✅ **Escalabilidade**
- Pode escalar módulos independentemente
- neo-core: 3 instâncias
- modelo-ia: 5 instâncias (mais demanda de IA)

✅ **Testabilidade**
- Testes isolados por módulo
- Testes de integração entre módulos

## Evolução Futura

### Fase 1: ✅ (Atual)
- Estrutura multi-módulo
- Comunicação via dependência Maven
- Serviço básico de previsão

### Fase 2: 🔄 (Próxima)
- Adicionar modelos reais de ML
- REST Client entre módulos
- Cache distribuído (Redis)

### Fase 3: 🎯 (Futuro)
- Microserviços independentes
- Message Queue (Kafka/RabbitMQ)
- Service Discovery (Consul)
- API Gateway

### Fase 4: 🚀 (Longo Prazo)
- Deploy em Kubernetes
- Observabilidade (Prometheus + Grafana)
- CI/CD completo
- Feature Flags
