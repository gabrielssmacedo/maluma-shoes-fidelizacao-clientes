#!/bin/bash

if [ ! -f "requirements.txt" ]; then
    echo "Erro: requirements.txt nao encontrado no diretorio atual."
    return 1 2>/dev/null || exit 1
fi

echo "Criando o ambiente virtual (venv)..."
python3 -m venv venv

echo "Ativando o ambiente virtual..."
source venv/bin/activate

echo "Atualizando o pip e instalando dependencias..."
pip install --upgrade pip
pip install -r requirements.txt

echo "Ambiente configurado com sucesso!"