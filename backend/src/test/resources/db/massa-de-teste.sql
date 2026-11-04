-- Massa fixa dos testes de integração (8 vendas, 3 clientes). Horários em UTC; São Paulo é UTC-3.
--   a2 = 2026-03-09 23:30 em SP (fica no dia 09) e a3 = 2026-03-10 00:30 em SP (fica no dia 10).
--   b1 e c1 têm a mesma data_compra: o desempate é pelo id, decrescente (c1 antes de b1).
INSERT INTO historico_vendas (id, pedido_externo_id, cliente_cpf, data_compra, valor_total, metodo_pagamento, itens_pedido) VALUES
('00000000-0000-0000-0000-0000000000a1', 'pedido-a1', '11111111111', '2026-03-01T12:00:00Z', 100.00, 'PIX', '[]'),
('00000000-0000-0000-0000-0000000000a2', 'pedido-a2', '11111111111', '2026-03-10T02:30:00Z', 200.00, 'BOLETO', '[]'),
('00000000-0000-0000-0000-0000000000a3', 'pedido-a3', '11111111111', '2026-03-10T03:30:00Z', 300.00, 'PIX', '[]'),
('00000000-0000-0000-0000-0000000000a4', 'pedido-a4', '11111111111', '2026-03-15T15:00:00Z', 400.00, 'CARTAO_CREDITO', '[]'),
('00000000-0000-0000-0000-0000000000a5', 'pedido-a5', '11111111111', '2026-03-20T15:00:00Z', 500.00, 'CARTAO_DEBITO', '[]'),
('00000000-0000-0000-0000-0000000000b1', 'pedido-b1', '22222222222', '2026-03-10T15:00:00Z', 129.90, 'PIX', '[]'),
('00000000-0000-0000-0000-0000000000b2', 'pedido-b2', '22222222222', '2026-02-01T10:00:00Z', 89.90, NULL, '[]'),
('00000000-0000-0000-0000-0000000000c1', 'pedido-c1', '33333333333', '2026-03-10T15:00:00Z', 349.80, 'CARTAO_CREDITO',
 '[{"id": "00000000-0000-0000-0000-00000000c101", "compra_id": "00000000-0000-0000-0000-0000000000c1", "sku_externo": "SKU-TNS-001", "nome_produto": "Tenis Runner Pro", "categoria": "Tenis", "tamanho": 40, "cor": "Preto", "preco_unitario": 199.90, "quantidade": 1, "campo_novo": "ignorado"},
   {"id": "00000000-0000-0000-0000-00000000c102", "compra_id": "00000000-0000-0000-0000-0000000000c1", "sku_externo": "SKU-SLT-002", "nome_produto": "Sandalia Salto Bloco", "categoria": "Salto", "tamanho": 36, "cor": "Nude", "preco_unitario": 149.90, "quantidade": 1}]')
ON CONFLICT (id) DO NOTHING;
