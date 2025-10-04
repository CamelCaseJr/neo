#!/bin/bash

echo "🚀 Migrando projeto NEO para estrutura multi-módulo..."

# Diretório raiz
ROOT_DIR="/mnt/c/Users/junior/Documents/fiap/neo"
cd "$ROOT_DIR"

echo "📁 Criando estrutura de diretórios..."

# Criar diretório neo-core se não existir
mkdir -p neo-core

# Mover código fonte para neo-core
echo "📦 Movendo código fonte para neo-core..."
if [ -d "src" ]; then
    mv src neo-core/
    echo "✅ Código fonte movido"
fi

# Mover arquivos de build para neo-core
echo "🔧 Movendo arquivos de build..."
if [ -f "mvnw" ]; then
    mv mvnw neo-core/
fi
if [ -f "mvnw.cmd" ]; then
    mv mvnw.cmd neo-core/
fi
if [ -d ".mvn" ]; then
    mv .mvn neo-core/
fi

# Mover docker-compose para neo-core
if [ -f "docker-compose.yml" ]; then
    mv docker-compose.yml neo-core/
    echo "✅ docker-compose.yml movido"
fi

# Renomear POM pai
echo "📝 Configurando POM pai..."
if [ -f "pom-parent.xml" ]; then
    # Backup do pom.xml original
    if [ -f "pom.xml" ]; then
        mv pom.xml pom-old.xml
        echo "✅ Backup do pom.xml original criado (pom-old.xml)"
    fi
    
    mv pom-parent.xml pom.xml
    echo "✅ POM pai configurado"
fi

# Mover README antigo
if [ -f "README.md" ]; then
    mv README.md neo-core/README-original.md
    echo "✅ README original movido para neo-core/"
fi

if [ -f "README-multimodule.md" ]; then
    mv README-multimodule.md README.md
    echo "✅ Novo README instalado"
fi

# Limpar arquivos temporários
echo "🧹 Limpando arquivos temporários..."
if [ -d "target" ]; then
    mv target neo-core/
    echo "✅ Diretório target movido"
fi

# Criar arquivo .mvn/wrapper para o root se necessário
echo "🔧 Configurando Maven Wrapper no root..."
mkdir -p .mvn/wrapper
if [ -f "neo-core/.mvn/wrapper/maven-wrapper.properties" ]; then
    cp -r neo-core/.mvn/* .mvn/
fi

# Copiar mvnw para o root
if [ -f "neo-core/mvnw" ] && [ ! -f "mvnw" ]; then
    cp neo-core/mvnw ./
    cp neo-core/mvnw.cmd ./
    chmod +x mvnw
fi

echo ""
echo "✅ Migração concluída!"
echo ""
echo "📂 Estrutura atual:"
echo "neo/"
echo "├── pom.xml                    # POM pai (agregador)"
echo "├── neo-core/                  # Módulo principal (porta 8080)"
echo "│   ├── pom.xml"
echo "│   ├── src/"
echo "│   └── docker-compose.yml"
echo "└── modelo-ia/                 # Módulo IA (porta 8081)"
echo "    ├── pom.xml"
echo "    └── src/"
echo ""
echo "🎯 Próximos passos:"
echo ""
echo "1️⃣  Compilar todos os módulos:"
echo "   mvn clean install"
echo ""
echo "2️⃣  Executar neo-core (Terminal 1):"
echo "   cd neo-core"
echo "   ./mvnw quarkus:dev"
echo ""
echo "3️⃣  Executar modelo-ia (Terminal 2):"
echo "   cd modelo-ia"
echo "   ../mvnw quarkus:dev -f pom.xml"
echo ""
echo "📚 Mais informações: README.md"
