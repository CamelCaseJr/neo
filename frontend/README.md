# NEO Monitor - Frontend

Dashboard moderno e interativo para monitoramento de Near Earth Objects (NEOs).

## 🚀 Características

- **Dashboard Principal**: Visão geral com estatísticas e gráficos dos NEOs
- **Catálogo de NEOs**: Listagem completa com filtros e busca
- **Machine Learning**: Interface para treinamento de modelos e predições
- **Analytics Avançado**: Análises estatísticas detalhadas e correlações

## 🎨 Tecnologias

- **HTML5** - Estrutura semântica
- **CSS3** - Design moderno com variáveis CSS e animações
- **JavaScript (Vanilla)** - Lógica da aplicação
- **Chart.js** - Visualizações e gráficos interativos

## 📁 Estrutura

```
frontend/
├── index.html          # Dashboard principal
├── neos.html           # Catálogo de NEOs
├── ml.html             # Machine Learning
├── analytics.html      # Analytics avançado
├── css/
│   └── styles.css      # Estilos globais
└── js/
    ├── config.js       # Configurações e utilitários
    ├── dashboard.js    # Lógica do dashboard
    ├── neos.js         # Lógica do catálogo
    ├── ml.js           # Lógica de ML
    └── analytics.js    # Lógica de analytics
```

## 🔧 Configuração

### 1. Configurar URLs das APIs

Edite o arquivo `js/config.js` e ajuste as URLs dos serviços:

```javascript
const API_CONFIG = {
  NEO_CORE_URL: "http://localhost:8080", // URL do neo-core
  ML_SERVICE_URL: "http://localhost:8081", // URL do modelo-ia
};
```

### 2. Iniciar os Serviços Backend

Certifique-se de que os serviços backend estejam rodando:

```bash
# Neo-core (porta 8080)
cd neo-core
./mvnw quarkus:dev

# Modelo-IA (porta 8081)
cd modelo-ia
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

### 3. Servir o Frontend

Você pode usar qualquer servidor HTTP estático. Algumas opções:

**Opção 1: Python**

```bash
cd frontend
python -m http.server 3000
```

**Opção 2: Node.js (http-server)**

```bash
npx http-server frontend -p 3000
```

**Opção 3: Live Server (VS Code)**

- Instale a extensão "Live Server"
- Clique com botão direito em `index.html`
- Selecione "Open with Live Server"

### 4. Acessar a Aplicação

Abra seu navegador e acesse:

```
http://localhost:3000
```

## 📊 Funcionalidades

### Dashboard

- Cards de estatísticas (Total de NEOs, Perigosos, Seguros, Precisão do ML)
- Gráfico de distribuição de periculosidade
- Gráfico de velocidade relativa
- Gráfico de diâmetros
- Gráfico de distâncias
- Tabela com NEOs recentes

### Catálogo de NEOs

- Grid de cards com todos os NEOs
- Filtros por periculosidade
- Busca por nome
- Paginação
- Modal com detalhes completos
- Link para NASA JPL

### Machine Learning

- Formulário para treinar modelo com período específico
- Opção para treinar com todos os dados
- Métricas de desempenho (Accuracy, Precision, Recall, F1-Score)
- Predição de periculosidade com inputs customizados
- Histórico de treinamentos (gráfico)
- Recarga do modelo

### Analytics

- Estatísticas avançadas
- Histograma de distribuição de diâmetros
- Scatter plots (Velocidade vs Periculosidade, Distância vs Diâmetro)
- Gráfico de aproximações mensais
- Matriz de correlação
- Tabela estatística comparativa

## 🎨 Design

### Paleta de Cores

- **Primary**: #6366f1 (Indigo)
- **Secondary**: #8b5cf6 (Purple)
- **Success**: #10b981 (Green)
- **Danger**: #ef4444 (Red)
- **Warning**: #f59e0b (Orange)
- **Info**: #3b82f6 (Blue)

### Tipografia

- **Fonte**: Inter (Google Fonts)
- **Pesos**: 300, 400, 500, 600, 700

### Layout

- Sidebar fixa com navegação
- Design responsivo (mobile-first)
- Dark theme moderno
- Animações suaves
- Cards com hover effects

## 🔌 APIs Utilizadas

### NEO Core API

- `GET /api/neos?pagina={n}&tamanho={n}&perigoso={true|false}`
- `GET /api/neos/{id}`

### ML Service API

- `POST /ml/train` - Treinar com período
- `POST /ml/train/all` - Treinar com tudo
- `POST /ml/reload` - Recarregar modelo
- `POST /ml/predict` - Fazer predição

## 🚨 CORS

Se encontrar problemas de CORS, adicione no backend (application.properties):

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.http.cors.methods=GET,POST,PUT,DELETE
quarkus.http.cors.headers=accept,content-type
```

## 📱 Responsividade

O dashboard é totalmente responsivo e funciona em:

- Desktop (1920px+)
- Laptop (1366px - 1920px)
- Tablet (768px - 1366px)
- Mobile (320px - 768px)

## 🛠️ Customização

### Adicionar novos gráficos

1. Adicione um canvas no HTML
2. Crie a função no arquivo JS correspondente
3. Use Chart.js para renderizar

### Modificar cores

Edite as variáveis CSS em `styles.css`:

```css
:root {
  --primary: #6366f1;
  --secondary: #8b5cf6;
  /* ... */
}
```

## 📝 Notas

- Os dados são carregados em tempo real das APIs
- Gráficos são interativos (hover para detalhes)
- Interface otimizada para performance
- Sem dependências de frameworks (Vanilla JS)

## 🐛 Troubleshooting

**Problema**: Dados não carregam

- Verifique se os backends estão rodando
- Confirme as URLs em `js/config.js`
- Verifique o console do navegador (F12)

**Problema**: CORS error

- Adicione configuração CORS no backend
- Use um proxy ou configure o servidor

**Problema**: Gráficos não aparecem

- Verifique se Chart.js está carregando (CDN)
- Confirme que os dados estão no formato correto

## 📄 Licença

Este projeto faz parte do trabalho acadêmico da FIAP.
