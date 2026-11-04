-- Cópia do DDL de postgres-init/02-init-app-tables.sh, para o Postgres efêmero dos testes.
-- O SchemaDeTesteSincronizadoTest reprova o build se esta cópia divergir do script original.

    CREATE TABLE IF NOT EXISTS historico_vendas (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        pedido_externo_id VARCHAR(100) UNIQUE NOT NULL,
        cliente_cpf VARCHAR(14) NOT NULL,
        data_compra TIMESTAMP WITH TIME ZONE NOT NULL,
        valor_total NUMERIC(10, 2) NOT NULL,
        metodo_pagamento VARCHAR(50),
        itens_pedido JSONB NOT NULL
    );

    CREATE INDEX IF NOT EXISTS idx_historico_vendas_cliente_cpf ON historico_vendas(cliente_cpf);
    CREATE INDEX IF NOT EXISTS idx_historico_vendas_data_compra ON historico_vendas(data_compra DESC);
