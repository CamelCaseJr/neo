#!/bin/bash

# NEO Monitor - Start Script (Linux/WSL/macOS)
# Este script inicia o servidor HTTP e abre o navegador automaticamente

echo "========================================"
echo "   NEO Monitor - Frontend Server"
echo "========================================"
echo ""

# Verificar Python3
echo "Verificando Python3..."
if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 não encontrado!"
    echo ""
    echo "Instale o Python3 com:"
    echo "  sudo apt update"
    echo "  sudo apt install python3"
    echo ""
    exit 1
fi

PYTHON_VERSION=$(python3 --version)
echo "✓ Python3 encontrado: $PYTHON_VERSION"
echo ""

echo "Iniciando servidor HTTP na porta 3000..."
echo ""
echo "URLs disponíveis:"
echo "  • http://localhost:3000/start.html  (Página inicial)"
echo "  • http://localhost:3000/index.html  (Dashboard)"
echo ""
echo "Pressione Ctrl+C para parar o servidor"
echo ""

# Tentar abrir o navegador (funciona no WSL2)
if command -v explorer.exe &> /dev/null; then
    sleep 2
    explorer.exe "http://localhost:3000/start.html" 2>/dev/null &
fi

# Iniciar servidor Python
python3 -m http.server 3000
