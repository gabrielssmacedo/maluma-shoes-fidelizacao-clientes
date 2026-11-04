# Maluma Shoes - Sistema de Fidelização de Clientes

Este repositório contém a infraestrutura, backend, frontend e pipelines de dados do sistema de fidelização da Maluma Shoes. O projeto utiliza Docker para orquestrar o banco de dados PostgreSQL, backend em Java/Quarkus e Apache Airflow para rotinas de ETL.

> 📘 **Equipe:** depois de atualizar o repositório, siga o [passo a passo da equipe](docs/passo-a-passo-equipe.md) (inclui o `.env` novo e a correção da carga do banco no Windows).

## 🚀 Como Rodar o Projeto (Docker Compose)

Na raiz do projeto (onde está o arquivo `docker-compose.yaml`), abra o terminal e execute:

```bash
docker compose up -d

# OBS: toda infraestrutura será criada, incluindo banco dados com carga inicial, jobs e seu orquestrador aiflow, aplicação backend e interface frontend
```

Para derrubar os containers:
```bash
# mantém os volumes (persistencia de dados dos container)
docker compose down  

# ou

# limpa volumes (ATENÇÃO: os dados gerados nos containers, como por exemplo o banco, serão eliminados)
docker compose down -v   
```

## 📁 Estrutura do Projeto
```bash
maluma-shoes-fidelizacao-clientes/
├── backend/               # Código do servidor/API principal da aplicação
├── frontend/              # Interface do usuário (sistema web)
├── jobs/                  # Orquestração e processamento de dados em lote
│   └── airflow/           # Ambiente completo do Apache Airflow
│       ├── dags/          # Definição das rotinas (DAGs) e scripts Python de ETL
│       │   └── historico_vendas/ # Rotina específica de extração/carga do historico de vendas
│       ├── logs/          # Logs gerados pelas execuções das tasks
│       ├── plugins/       # Extensões ou operadores customizados do Airflow
│       ├── venv/          # Ambiente virtual Python para debug/testes locais
│       └── create_env.*   # Scripts facilitadores para criar o venv local
├── postgres-init/         # Scripts executados automaticamente na criação do banco
│   ├── 01-init...         # Criação do banco postgres utilizado pelo Airflow
│   ├── 02-init...         # Criação de schemas e tabelas da aplicação (tab historico_vendas)
│   └── 03-initial...      # Carga maciça dos 20.000 registros mockados (tab historico_vendas)
├── .env                   <<-- ADICIONE SEU ARQUIVO .env AQUI
├── .gitattributes         # Força LF nos .sh (sem isso, no Windows a carga inicial do banco falha)
└── docker-compose.yaml    # Arquivo de orquestração da infraestrutura Docker
```

> ⚠️ **Windows:** se você clonou o repositório antes do `.gitattributes` existir, os `.sh` de
> `postgres-init/` estão com CRLF e o Postgres aborta a carga (`/bin/bash^M: bad interpreter`),
> deixando o volume **sem a tabela `historico_vendas`**. Para corrigir (no Git Bash):
> ```bash
> rm postgres-init/*.sh && git checkout -- postgres-init
> docker compose down -v && docker compose up -d
> ```

## ☕ Backend (API REST em Java/Quarkus)

Sobe junto com o `docker compose up -d` (serviço `backend`, porta **8081**, acessível só pela própria
máquina). Precisa de duas variáveis no `.env`:

| Variável | Para quê |
|---|---|
| `API_KEY` | **Obrigatória.** Chave que toda chamada à API envia no header `X-API-Key` (sem ela o backend não sobe). Gere uma com `python -c "import secrets; print(secrets.token_urlsafe(24))"`. |
| `SWAGGER_ENABLED` | `true` liga a documentação interativa em [http://localhost:8081/q/swagger-ui](http://localhost:8081/q/swagger-ui) (botão **Authorize** para informar a chave). Padrão: desligado. |

**Autenticação:** chave de API única no header `X-API-Key`. Sem ela, ou com a chave errada, a resposta
é `401` com `code: NAO_AUTENTICADO`. O health check (`/q/health`) não exige chave.

| Endpoint | O que faz |
|---|---|
| `GET /api/v1/vendas?page&size&cpf&data_inicio&data_fim` | Histórico geral para a tabela principal, sem os itens do pedido. Todos os filtros são opcionais. |
| `GET /api/v1/clientes/{cpf}/vendas?page&size` | Todo o histórico de um cliente, com os itens de cada pedido. CPF sem compras devolve página vazia. |

- **Paginação:** `page` começa em 0 (padrão 0); `size` padrão 20, máximo 100.
- **Ordem:** da compra mais recente para a mais antiga.
- **CPF:** 11 dígitos, com ou sem pontuação (`123.456.789-01` ou `12345678901`).
- **Datas:** `yyyy-MM-dd`, no fuso de São Paulo. A `data_fim` entra inteira (até 23:59:59).
- **Erros:** JSON no formato RFC 9457 com o campo `code`: `CPF_INVALIDO`, `DATA_INVALIDA`,
  `PERIODO_INVALIDO`, `PAGINA_INVALIDA` (400), `NAO_AUTENTICADO` (401) ou `NAO_ENCONTRADO` (404).

```bash
curl -H "X-API-Key: $API_KEY" "http://localhost:8081/api/v1/vendas?page=0&size=20&data_inicio=2026-10-01&data_fim=2026-10-31"
curl -H "X-API-Key: $API_KEY" "http://localhost:8081/api/v1/clientes/246.913.578-02/vendas?size=5"
```

Resposta paginada:
```json
{
  "content": [{ "id": "…", "pedidoExternoId": "…", "clienteCpf": "34567169392",
                "dataCompra": "2026-11-01T02:28:27.331598Z", "valorTotal": 638.80,
                "metodoPagamento": "PIX" }],
  "page": 0, "size": 20, "totalElements": 807, "totalPages": 41
}
```

**Desenvolvimento local** (precisa de Java 25; o Maven vem pelo wrapper):
```bash
cd backend
./mvnw clean verify      # testes + cobertura mínima de 80% (o Docker Desktop precisa estar aberto:
                         # os testes sobem um Postgres temporário)
./mvnw spotless:apply    # formata o código (Google Java Format)
# rodar fora do Docker, apontando para o Postgres do compose (porta 5433):
API_KEY=... POSTGRES_USER=... POSTGRES_PASSWORD=... POSTGRES_DB=sistema_fidelizacao_clientes ./mvnw quarkus:dev
```

### 🎲 Como checar os dados pelo pgAdmin

1. Acesse o painel do pgAdmin pelo navegador ([http://localhost:8084](http://localhost:8084)).
2. Faça o login com as credenciais definidas no seu arquivo `.env`.
3. Adicione um novo servidor com nome `maluma_shoes`, informando host, port, database, user e password (todas as informações no `.env`).
4. Navegue até o banco `sistema_fidelizacao_clientes` > **Schemas** > **Tables** para visualizar a tabela `historico_vendas`.

### 📊 Airflow e Rotina ETL

O Apache Airflow é utilizado para buscar novos dados de vendas (via API mockada) e inseri-los no banco sem duplicidade.

1. Acesse o painel do Airflow no navegador em [http://localhost:8082](http://localhost:8082).
2. Faça login com o usuário administrador padrão.
3. Na lista de DAGs, localize `etl_historico_vendas`.
4. Para rodar manualmente, clique no botão de **Play** (Trigger DAG). Você pode acompanhar o progresso e os logs clicando nos quadrados da visualização **Grid**.

A rotina carrega as vendas de **ontem** (no fuso de São Paulo) em relação à data da execução: a agendada
das 03:00 carrega o dia anterior, e reexecutar uma execução antiga carrega o dia dela. Para carregar um
dia específico (por exemplo, o dia das vendas do mock, 30/10/2026), use **Trigger DAG w/ config** com:
```json
{"data": "2026-10-30"}
```
O CPF é gravado só com os 11 dígitos (pontuação é removida); venda com CPF inválido é ignorada e
aparece no log.
