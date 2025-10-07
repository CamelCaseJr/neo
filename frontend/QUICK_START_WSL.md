# 🚀 Guia Rápido - Como Iniciar o Frontend

## 📍 Você está em: `/mnt/c/Users/junior/Documents/fiap/neo/frontend`

---

## ✅ SOLUÇÃO RÁPIDA (WSL/Linux):

### Opção 1: Usar Python3 (Recomendado)

```bash
python3 -m http.server 3000
```

Depois acesse no navegador: `http://localhost:3000/start.html`

---

### Opção 2: Usar o script shell

```bash
# Dar permissão de execução
chmod +x start-server.sh

# Executar
./start-server.sh
```

---

### Opção 3: Usar Node.js (se instalado)

```bash
npx http-server -p 3000
```

---

## ❌ O QUE NÃO FAZER:

```bash
# ❌ NÃO faça isso (você já está na pasta frontend):
cd frontend

# ❌ NÃO use isso (não funciona no WSL):
start-server.bat

# ❌ NÃO use isso (no WSL, use python3):
python -m http.server 3000
```

---

## 🔧 Se Python3 não estiver instalado:

```bash
# Atualizar pacotes
sudo apt update

# Instalar Python3
sudo apt install python3

# Verificar instalação
python3 --version
```

---

## 📋 COMANDOS CORRETOS (copie e cole):

### 1️⃣ Iniciar o Frontend:

```bash
python3 -m http.server 3000
```

### 2️⃣ Em outro terminal, iniciar neo-core:

```bash
cd /mnt/c/Users/junior/Documents/fiap/neo/neo-core
./mvnw quarkus:dev
```

### 3️⃣ Em outro terminal, iniciar modelo-ia:

```bash
cd /mnt/c/Users/junior/Documents/fiap/neo/modelo-ia
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

---

## 🌐 Acessar no navegador:

- **Página inicial:** http://localhost:3000/start.html
- **Dashboard:** http://localhost:3000/index.html
- **NEOs:** http://localhost:3000/neos.html
- **ML:** http://localhost:3000/ml.html
- **Analytics:** http://localhost:3000/analytics.html

---

## 💡 DICA: Abrir navegador no Windows pelo WSL

```bash
# Depois de iniciar o servidor, abra o navegador Windows:
explorer.exe "http://localhost:3000/start.html"
```

---

## 🐛 Troubleshooting:

**Problema: "Address already in use"**

```bash
# Matar processo na porta 3000
sudo lsof -ti:3000 | xargs kill -9

# Ou usar outra porta
python3 -m http.server 8000
```

**Problema: "Permission denied"**

```bash
# Se precisar de permissões
chmod +x start-server.sh
```

**Problema: Python3 não encontrado**

```bash
sudo apt update
sudo apt install python3
```

---

## ✅ COMANDO ÚNICO PARA COPIAR:

```bash
# Inicie o servidor (certifique-se de estar na pasta frontend)
python3 -m http.server 3000
```

Depois acesse: **http://localhost:3000/start.html**

---

Pronto! 🎉
