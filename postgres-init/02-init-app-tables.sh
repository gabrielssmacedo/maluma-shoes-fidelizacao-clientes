#!/bin/bash
set -e

# Conecta no banco principal da aplicação usando as variáveis de ambiente
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL

    -- Cria a tabela apenas se ela não existir
    CREATE TABLE IF NOT EXISTS historico_vendas (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        pedido_externo_id VARCHAR(100) UNIQUE NOT NULL,
        cliente_cpf VARCHAR(14) NOT NULL,
        data_compra TIMESTAMP WITH TIME ZONE NOT NULL,
        valor_total NUMERIC(10, 2) NOT NULL,
        metodo_pagamento VARCHAR(50),
        itens_pedido JSONB NOT NULL
    );

    -- Cria os índices apenas se eles não existirem
    CREATE INDEX IF NOT EXISTS idx_historico_vendas_cliente_cpf ON historico_vendas(cliente_cpf);
    CREATE INDEX IF NOT EXISTS idx_historico_vendas_data_compra ON historico_vendas(data_compra DESC);

EOSQL