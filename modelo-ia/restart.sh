#!/bin/bash

echo "🔍 Procurando processo na porta 8081..."

# Encontrar o PID do processo usando a porta 8081
PID=$(lsof -ti:8081 2>/dev/null || fuser 8081/tcp 2>/dev/null | awk '{print $1}')

if [ -z "$PID" ]; then
    echo "✓ Porta 8081 está livre!"
else
    echo "❌ Processo encontrado (PID: $PID)"
    echo "🔪 Matando processo..."
    kill -9 $PID 2>/dev/null || sudo kill -9 $PID
    sleep 2
    echo "✓ Processo finalizado!"
fi

echo ""
echo "🚀 Iniciando modelo-ia na porta 8081..."
echo ""

cd "$(dirname "$0")"
./mvnw quarkus:dev
