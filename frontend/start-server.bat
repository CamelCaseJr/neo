@echo off
echo ========================================
echo   NEO Monitor - Frontend Server
echo ========================================
echo.

echo Verificando Python...
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Python nao encontrado!
    echo Por favor, instale o Python ou use outro servidor HTTP.
    pause
    exit /b 1
)

echo Python encontrado!
echo.
echo Iniciando servidor HTTP na porta 3000...
echo Acesse: http://localhost:3000/start.html
echo.
echo Pressione Ctrl+C para parar o servidor
echo.

cd /d "%~dp0"
python -m http.server 3000
