# Maluma Shoes — Frontend

Esta pasta contém a aplicação React integrada à API real do Maluma Shoes. Não utiliza dados simulados.

## Localização

Mantenha esta pasta `frontend` na raiz do repositório, ao lado da pasta `backend` e do arquivo `.env`:

```text
projeto/
├── .env
├── backend/
└── frontend/
```

O arquivo `.env` deve ser o mesmo usado pelo backend e conter a variável `API_KEY` com a chave configurada para a API. O frontend lê essa chave pelo Vite e a envia no cabeçalho `X-API-Key` através do proxy local; a chave não é exposta no código executado pelo navegador.

Se o backend estiver em outro endereço, configure `BACKEND_URL` no `.env`. O padrão é `http://localhost:8081`.

## Executar

Com o PostgreSQL e o backend já iniciados, abra um terminal nesta pasta e execute:

```bash
npm install
npm run dev
```

Acesse `http://localhost:5173`.

## Integração implementada

- `GET /api/v1/vendas`: listagem, filtro por CPF, período e paginação.
- `GET /api/v1/clientes/{cpf}/vendas`: histórico do cliente com os itens de cada pedido.
- Autenticação por `X-API-Key`, lida do `.env` da raiz pelo proxy do Vite.
- Tratamento dos códigos de erro definidos pelo backend.

O frontend depende do backend e do PostgreSQL estarem em execução. A pasta frontend não inicia esses serviços.
