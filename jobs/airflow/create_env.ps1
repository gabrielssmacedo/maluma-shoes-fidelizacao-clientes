if (-Not (Test-Path "requirements.txt")) {
    Write-Host "Erro: requirements.txt nao encontrado no diretorio atual." -ForegroundColor Red
    return
}

Write-Host "Criando o ambiente virtual (venv)..." -ForegroundColor Cyan
python -m venv venv

Write-Host "Ativando o ambiente virtual..." -ForegroundColor Cyan
. .\venv\Scripts\Activate.ps1

Write-Host "Atualizando o pip e instalando dependencias..." -ForegroundColor Cyan
python -m pip install --upgrade pip
pip install -r requirements.txt

Write-Host "Ambiente configurado com sucesso!" -ForegroundColor Green