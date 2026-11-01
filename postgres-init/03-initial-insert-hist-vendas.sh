#!/bin/bash
set -e

# Conecta no banco principal da aplicação usando as variáveis de ambiente
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL

    -- 1. Cria o Catálogo Fictício (Tabela Temporária que se apaga sozinha ao fim da execução)
    CREATE TEMP TABLE tmp_catalogo (
        id INT PRIMARY KEY,
        sku VARCHAR(50),
        nome VARCHAR(100),
        categoria VARCHAR(50),
        preco NUMERIC(10, 2)
    );

    -- 2. Preenche o catálogo com variedades realistas de uma loja de calçados
    INSERT INTO tmp_catalogo (id, sku, nome, categoria, preco) VALUES
    (1, 'SKU-TNS-001', 'Tênis Runner Pro', 'Tênis', 199.90),
    (2, 'SKU-TNS-002', 'Tênis Casual Lona', 'Tênis', 129.90),
    (3, 'SKU-TNS-003', 'Tênis Slip On Confort', 'Tênis', 149.50),
    (4, 'SKU-BTA-001', 'Bota Chelsea Couro', 'Bota', 259.90),
    (5, 'SKU-BTA-002', 'Coturno Tratorado', 'Bota', 299.00),
    (6, 'SKU-BTA-003', 'Bota Montaria Elegance', 'Bota', 349.90),
    (7, 'SKU-SLT-001', 'Scarpin Verniz Clássico', 'Salto', 179.90),
    (8, 'SKU-SLT-002', 'Sandália Salto Bloco', 'Salto', 159.00),
    (9, 'SKU-SLT-003', 'Peep Toe Festa', 'Salto', 210.00),
    (10, 'SKU-SND-001', 'Papete Anatômica', 'Sandália', 119.90),
    (11, 'SKU-SND-002', 'Rasteira Pedraria', 'Sandália', 89.90),
    (12, 'SKU-SND-003', 'Sandália Flatform', 'Sandália', 139.90),
    (13, 'SKU-MOC-001', 'Mocassim Suede', 'Sapato', 169.90),
    (14, 'SKU-MOC-002', 'Oxford Tratorado', 'Sapato', 199.90),
    (15, 'SKU-SFT-001', 'Sapatilha Bico Fino', 'Sapatilha', 79.90),
    (16, 'SKU-SFT-002', 'Sapatilha Matelassê', 'Sapatilha', 89.90),
    (17, 'SKU-ACS-001', 'Palmilha Gel Conforto', 'Acessório', 49.90),
    (18, 'SKU-ACS-002', 'Meia Algodão Invisível', 'Acessório', 19.90),
    (19, 'SKU-SPT-001', 'Sapato Social Couro', 'Sapato', 279.90),
    (20, 'SKU-TNS-004', 'Tênis Esportivo Max', 'Tênis', 249.90);

    -- 3. Geração em Massa e Inserção das 20.000 Vendas
    INSERT INTO historico_vendas (id, pedido_externo_id, cliente_cpf, data_compra, valor_total, metodo_pagamento, itens_pedido)
    WITH compras_base AS (
        SELECT
            gen_random_uuid() AS compra_uuid,
            gen_random_uuid()::text AS pedido_externo_id,
            
            -- SIMULAÇÃO DE FIDELIDADE (Curva Exponencial):
            -- random()^4 força matematicamente que IDs de 1 a 1000 sejam sorteados frequentemente (Clientes Fiéis 5-10+ compras),
            -- enquanto IDs acima de 5000 raramente aparecem (Clientes Únicos/Esporádicos). Base de ~8000 CPFs.
            (random()^1.5 * 15000)::int + 1 AS cliente_id_base,
            
            -- Data aleatória distribuída nos últimos 2 anos até a data atual
            NOW() - (random() * 730 || ' days')::interval AS data_compra,
            
            -- Distribuição de pagamentos (Cartão de Crédito e Pix têm mais probabilidade)
            (ARRAY['PIX', 'PIX', 'CARTAO_CREDITO', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'BOLETO'])[floor(random()*6)+1] AS metodo_pagamento,
            
            -- Quantidade de SKUs diferentes no carrinho (Geralmente 1, raramente 2 ou 3)
            (random()^3 * 2 + 1)::int AS qtd_itens_diferentes
        FROM generate_series(1, 20000)
    ),
    itens_expandidos AS (
        SELECT
            c.compra_uuid,
            c.pedido_externo_id,
            -- Mapeia o ID do cliente fiel gerado acima para um CPF fixo e consistente de 11 dígitos
            lpad(((c.cliente_id_base::bigint * 12345678901) % 99999999999)::text, 11, '0') AS cliente_cpf,
            c.data_compra,
            c.metodo_pagamento,
            
            -- Escolhe produto aleatório do catálogo (IDs de 1 a 20)
            (floor(random()*20)+1)::int AS produto_id,
            -- Quantidade comprada do mesmo produto (geralmente 1 par, raramente 2)
            (random()^4 * 1 + 1)::int AS qtd_comprada,
            -- Tamanho aleatório realista do calçado (34 a 43)
            floor(random()*10 + 34)::int AS tamanho_calcado,
            -- Cor aleatória
            (ARRAY['Preto', 'Branco', 'Marrom', 'Nude', 'Azul Marinho', 'Bege', 'Vermelho'])[floor(random()*7)+1] AS cor_calcado
        FROM compras_base c
        -- Expande as linhas com base em quantos itens diferentes foram sorteados para aquele carrinho
        CROSS JOIN LATERAL generate_series(1, c.qtd_itens_diferentes)
    ),
    itens_com_valores AS (
        SELECT
            i.compra_uuid,
            i.pedido_externo_id,
            i.cliente_cpf,
            i.data_compra,
            i.metodo_pagamento,
            cat.sku,
            cat.nome,
            cat.categoria,
            cat.preco,
            i.qtd_comprada,
            i.tamanho_calcado,
            i.cor_calcado,
            (cat.preco * i.qtd_comprada) AS subtotal
        FROM itens_expandidos i
        JOIN tmp_catalogo cat ON cat.id = i.produto_id
    )
    -- Reagrupamento Final
    SELECT
        compra_uuid AS id,
        pedido_externo_id,
        cliente_cpf,
        data_compra,
        -- Regra respeitada: Valor total é a soma matemática perfeita dos itens do JSON
        SUM(subtotal) AS valor_total,
        metodo_pagamento,
        -- Montagem dinâmica do JSON Array exigido pelo schema
        jsonb_agg(
            jsonb_build_object(
                'id', gen_random_uuid()::text,
                'compra_id', compra_uuid::text,
                'sku_externo', sku,
                'nome_produto', nome,
                'categoria', categoria,
                'tamanho', tamanho_calcado,
                'cor', cor_calcado,
                'preco_unitario', preco,
                'quantidade', qtd_comprada
            )
        ) AS itens_pedido
    FROM itens_com_valores
    GROUP BY
        compra_uuid,
        pedido_externo_id,
        cliente_cpf,
        data_compra,
        metodo_pagamento;

    -- 4. Otimização de busca após a injeção maciça de dados (Opcional)
    ANALYZE historico_vendas;

EOSQL