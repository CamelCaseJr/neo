# NEO Monitor - Start Script (PowerShell)
# Este script inicia o servidor HTTP e abre o navegador automaticamente

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   NEO Monitor - Frontend Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Python
Write-Host "Verificando Python..." -ForegroundColor Yellow
try {
    $pythonVersion = python --version 2>&1
    Write-Host "✓ Python encontrado: $pythonVersion" -ForegroundColor Green
}
catch {
    Write-Host "✗ Python não encontrado!" -ForegroundColor Red
    Write-Host "Por favor, instale o Python ou use outro servidor HTTP." -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""
Write-Host "Iniciando servidor HTTP na porta 3000..." -ForegroundColor Yellow
Write-Host ""
Write-Host "URLs disponíveis:" -ForegroundColor Green
Write-Host "  • http://localhost:3000/start.html  (Página inicial)" -ForegroundColor White
Write-Host "  • http://localhost:3000/index.html  (Dashboard)" -ForegroundColor White
Write-Host ""
Write-Host "Abrindo navegador em 3 segundos..." -ForegroundColor Cyan
Write-Host ""
Write-Host "Pressione Ctrl+C para parar o servidor" -ForegroundColor Yellow
Write-Host ""

# Aguardar 3 segundos e abrir o navegador
Start-Sleep -Seconds 3
Start-Process "http://localhost:3000/start.html"

# Iniciar servidor Python
Set-Location $PSScriptRoot
python -m http.server 3000
