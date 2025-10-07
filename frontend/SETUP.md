# 🌍 NEO Monitor - Frontend Moderno

## ✨ O que foi criado

Um dashboard completo e moderno para visualização e análise de Near Earth Objects (NEOs) com as seguintes funcionalidades:

### 📄 Páginas Criadas

1. **start.html** - Página inicial de boas-vindas
2. **index.html** - Dashboard principal com estatísticas e gráficos
3. **neos.html** - Catálogo completo de NEOs com filtros
4. **ml.html** - Interface de Machine Learning
5. **analytics.html** - Analytics avançado com correlações

### 🎨 Design Features

- ✅ **Dark Theme Moderno** - Interface elegante com cores escuras
- ✅ **Sidebar Navegável** - Menu lateral fixo com ícones
- ✅ **Cards Animados** - Efeitos hover e transições suaves
- ✅ **Gráficos Interativos** - Chart.js com múltiplos tipos de visualização
- ✅ **Responsivo** - Funciona em desktop, tablet e mobile
- ✅ **Totalmente em JavaScript Vanilla** - Sem frameworks pesados

### 📊 Gráficos Implementados

- Doughnut Chart (Pizza) - Distribuição de periculosidade
- Bar Charts - Velocidades e diâmetros
- Line Charts - Distâncias e tendências temporais
- Scatter Plots - Correlações entre variáveis
- Histogramas - Distribuições estatísticas

### 🔌 Integrações com APIs

```javascript
// NEO Core API
GET /api/neos?pagina={n}&tamanho={n}&perigoso={true|false}
GET /api/neos/{id}

// ML Service API
POST /ml/train
POST /ml/train/all
POST /ml/reload
POST /ml/predict
```

### 🚀 Como Iniciar

#### Opção 1: Usando o script BAT (Windows)

```bash
cd frontend
start-server.bat
```

#### Opção 2: Python manualmente

```bash
cd frontend
python -m http.server 3000
```

#### Opção 3: Node.js

```bash
npx http-server frontend -p 3000
```

#### Opção 4: VS Code Live Server

- Instale a extensão "Live Server"
- Clique com botão direito em `start.html`
- Selecione "Open with Live Server"

### 🌐 Acessar

Depois de iniciar o servidor:

```
http://localhost:3000/start.html  (Página de boas-vindas)
http://localhost:3000/index.html  (Dashboard direto)
```

### ⚙️ Configuração dos Backends

Certifique-se de ter os backends rodando:

**Terminal 1 - neo-core:**

```bash
cd neo-core
./mvnw quarkus:dev
```

**Terminal 2 - modelo-ia:**

```bash
cd modelo-ia
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

### 🔧 Configurar CORS

Se encontrar erros de CORS, adicione em `application.properties`:

**neo-core/src/main/resources/application.properties:**

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=accept,authorization,content-type,x-requested-with
quarkus.http.cors.exposed-headers=*
```

**modelo-ia/src/main/resources/application.properties:**

```properties
quarkus.http.port=8081
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=accept,authorization,content-type,x-requested-with
quarkus.http.cors.exposed-headers=*
```

### 📁 Estrutura de Arquivos Criada

```
frontend/
├── start.html              # Página de boas-vindas
├── index.html              # Dashboard principal
├── neos.html               # Catálogo de NEOs
├── ml.html                 # Machine Learning
├── analytics.html          # Analytics avançado
├── start-server.bat        # Script para iniciar servidor (Windows)
├── README.md               # Documentação completa
├── css/
│   └── styles.css          # Estilos globais (900+ linhas)
└── js/
    ├── config.js           # Configurações e utilitários
    ├── dashboard.js        # Lógica do dashboard
    ├── neos.js             # Lógica do catálogo
    ├── ml.js               # Lógica de ML
    └── analytics.js        # Lógica de analytics
```

### 🎯 Funcionalidades por Página

#### 📊 Dashboard (index.html)

- 4 cards de estatísticas principais
- Gráfico de distribuição de periculosidade (Doughnut)
- Gráfico de velocidades (Bar)
- Gráfico de diâmetros (Bar)
- Gráfico de distâncias (Line)
- Tabela com os 10 NEOs mais recentes
- Botão de atualização em tempo real

#### 🪨 NEOs (neos.html)

- Grid responsivo de cards
- Filtro por periculosidade (Todos/Perigosos/Seguros)
- Busca por nome
- Seleção de itens por página (10/20/50/100)
- Paginação funcional
- Modal com detalhes completos ao clicar
- Link para NASA JPL

#### 🤖 Machine Learning (ml.html)

- 4 cards com métricas do modelo (Accuracy, Precision, Recall, F1)
- Formulário para treinar com período específico
- Botão para treinar com todos os dados
- Formulário de predição com 4 features
- Botão para recarregar modelo
- Gráfico de histórico de treinamentos
- Seção informativa sobre o modelo

#### 📈 Analytics (analytics.html)

- 4 cards com estatísticas avançadas
- Histograma de distribuição de diâmetros
- Scatter plot: Velocidade vs Periculosidade
- Scatter plot: Distância vs Diâmetro
- Gráfico de aproximações mensais
- Gráfico de correlações
- Tabela comparativa (Todos/Perigosos/Seguros)

### 🎨 Paleta de Cores

```css
Primary (Indigo):    #6366f1
Secondary (Purple):  #8b5cf6
Success (Green):     #10b981
Danger (Red):        #ef4444
Warning (Orange):    #f59e0b
Info (Blue):         #3b82f6

Backgrounds:
- Primary:   #0f172a
- Secondary: #1e293b
- Tertiary:  #334155
```

### ✨ Destaques Técnicos

1. **Performance**: JavaScript Vanilla (sem jQuery/React)
2. **Responsividade**: Mobile-first design
3. **Acessibilidade**: Estrutura semântica HTML5
4. **Animações**: CSS animations e transitions suaves
5. **Gráficos**: Chart.js 4.4.0 via CDN
6. **Fonte**: Inter do Google Fonts
7. **Toast Notifications**: Sistema de notificações custom
8. **Loading States**: Loaders animados durante carregamento
9. **Error Handling**: Tratamento de erros com mensagens claras
10. **Code Organization**: Separação clara de responsabilidades

### 📱 Responsividade

- **Desktop (1920px+)**: Layout completo com sidebar
- **Laptop (1366-1920px)**: Layout otimizado
- **Tablet (768-1366px)**: Grid adaptativo
- **Mobile (320-768px)**: Sidebar oculta, cards empilhados

### 🐛 Troubleshooting

**Problema: Dados não carregam**

- ✓ Verifique se neo-core está em http://localhost:8080
- ✓ Verifique se modelo-ia está em http://localhost:8081
- ✓ Abra DevTools (F12) e veja o console
- ✓ Confirme CORS configurado nos backends

**Problema: Gráficos não aparecem**

- ✓ Verifique conexão com internet (Chart.js via CDN)
- ✓ Confirme que há dados retornados das APIs
- ✓ Veja o console para erros JavaScript

**Problema: CORS Error**

- ✓ Adicione configuração CORS nos backends
- ✓ Reinicie os serviços Quarkus
- ✓ Limpe cache do navegador (Ctrl+Shift+Delete)

### 📚 Bibliotecas Utilizadas

- **Chart.js 4.4.0** - Gráficos interativos
- **Google Fonts (Inter)** - Tipografia moderna
- **Fetch API** - Requisições HTTP
- **CSS Grid & Flexbox** - Layouts responsivos

### 🎓 Aprendizados

Este projeto demonstra:

- ✅ Integração com APIs REST
- ✅ Visualização de dados com gráficos
- ✅ Interface moderna e responsiva
- ✅ Boas práticas de JavaScript
- ✅ Organização de código frontend
- ✅ UX/UI design moderno
- ✅ Tratamento de estados assíncronos

### 🚀 Próximos Passos (Opcional)

Se quiser evoluir o projeto:

- [ ] Adicionar autenticação de usuários
- [ ] Implementar PWA (Progressive Web App)
- [ ] Adicionar tema claro/escuro toggle
- [ ] Exportar dados para CSV/PDF
- [ ] Adicionar mais filtros avançados
- [ ] Implementar WebSocket para updates em tempo real
- [ ] Adicionar testes unitários
- [ ] Deploy em serviço de hospedagem

### 📄 Licença

Projeto acadêmico - FIAP

### 👨‍💻 Como Contribuir

1. Clone o repositório
2. Faça suas modificações
3. Teste localmente
4. Commit com mensagens claras
5. Push e abra PR

---

**Desenvolvido com ❤️ para o curso FIAP**

**Tecnologias:** HTML5, CSS3, JavaScript ES6+, Chart.js
**Paradigma:** JAMstack (JavaScript, APIs, Markup)
