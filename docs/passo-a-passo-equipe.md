# Passo a passo da equipe: backend de histórico de vendas

Guia para quem vai rodar o projeto depois da chegada do backend (`/backend`), da correção da carga
inicial do banco no Windows e dos ajustes na ETL do Airflow. Siga na ordem.

## 1. Pré-requisitos

- **Docker Desktop** aberto.
- **Git** (no Windows, use o **Git Bash** para os comandos abaixo).
- Só para quem vai mexer no backend: **Java 25**. O Maven não precisa instalar, ele vem pelo
  `./mvnw`.

## 2. Atualizar o repositório

```bash
git checkout main
git pull
```

### Clonou antes desta atualização e usa Windows? Faça isto uma vez

Os scripts de `postgres-init/` estavam saindo com quebra de linha do Windows (CRLF), e o Postgres
abortava a carga inicial (`/bin/bash^M: bad interpreter`). O banco ficava **sem a tabela
`historico_vendas`** e sem o banco do Airflow. O novo `.gitattributes` corrige isso, mas os arquivos
que já estão na sua máquina precisam ser baixados de novo:

```bash
rm postgres-init/*.sh jobs/airflow/create_env.sh
git checkout -- postgres-init jobs/airflow/create_env.sh
```

Como saber se você foi afetado: `docker logs maluma_postgres 2>&1 | grep "bad interpreter"`. Se
aparecer algo, faça o passo acima e o passo 4 com `down -v`.

## 3. Atualizar o `.env`

Peça o `.env` atualizado para o Felipe, ou acrescente estas duas linhas no seu:

```dotenv
# Chave exigida pelo backend no header X-API-Key (a mesma para toda a equipe)
API_KEY=<pedir para o Felipe>
# true liga o Swagger em http://localhost:8081/q/swagger-ui
SWAGGER_ENABLED=true
```

Sem `API_KEY` o backend não sobe (o contêiner `maluma_backend` fica reiniciando). O `.env` não vai
para o git; nunca faça commit dele.

## 4. Subir os contêineres

```bash
# Só se você foi afetado pelo problema do passo 2: apaga o banco quebrado (perde os dados locais)
docker compose down -v

docker compose up -d --build
```

A primeira subida demora: o Postgres carrega as 20.000 vendas e o backend é compilado dentro do
Docker. Confira se está tudo de pé:

```bash
docker ps --format "{{.Names}}  {{.Status}}"
```

`maluma_postgres` e `maluma_backend` devem aparecer como `(healthy)`.

## 5. Conferir os dados (pgAdmin)

1. Abra [http://localhost:8084](http://localhost:8084) e entre com `PGADMIN_EMAIL`/`PGADMIN_PASSWORD`
   do `.env`.
2. Registre o servidor com host `postgres`, porta `5432` e usuário/senha do `.env`.
3. Rode `SELECT count(*) FROM historico_vendas;`. O resultado deve ser **20000** (ou mais, se a ETL
   já rodou).

## 6. Testar a API

A API fica em `http://localhost:8081/api/v1` e só responde na própria máquina. Toda chamada leva o
header `X-API-Key`.

```bash
# No Git Bash, carregue a chave do .env:
export API_KEY=$(grep '^API_KEY=' .env | cut -d= -f2 | tr -d '\r')

# Histórico geral (tabela principal), com filtros opcionais
curl -H "X-API-Key: $API_KEY" "http://localhost:8081/api/v1/vendas?page=0&size=20"
curl -H "X-API-Key: $API_KEY" "http://localhost:8081/api/v1/vendas?data_inicio=2026-10-01&data_fim=2026-10-31"
curl -H "X-API-Key: $API_KEY" "http://localhost:8081/api/v1/vendas?cpf=246.913.578-02"

# Histórico completo de um cliente, com os itens de cada pedido
curl -H "X-API-Key: $API_KEY" "http://localhost:8081/api/v1/clientes/24691357802/vendas?size=5"
```

Também dá para testar pelo Swagger em
[http://localhost:8081/q/swagger-ui](http://localhost:8081/q/swagger-ui), com
`SWAGGER_ENABLED=true` no `.env`: clique em **Authorize**, cole a chave e use **Try it out**. No
Postman ou Insomnia, adicione o header `X-API-Key`.

### Contrato resumido

| Endpoint | Retorno |
|---|---|
| `GET /api/v1/vendas?page&size&cpf&data_inicio&data_fim` | Página de vendas **sem** `itensPedido` |
| `GET /api/v1/clientes/{cpf}/vendas?page&size` | Página de vendas **com** `itensPedido` |

- **Paginação:** `page` começa em 0; `size` padrão 20, máximo 100. A resposta traz `content`,
  `page`, `size`, `totalElements` e `totalPages`.
- **Ordem:** da compra mais recente para a mais antiga.
- **CPF:** 11 dígitos, com ou sem pontuação.
- **Datas:** `yyyy-MM-dd` no fuso de São Paulo, com a `data_fim` incluída inteira. As datas da
  resposta vêm em UTC (com `Z`).
- **Campos:** a resposta usa camelCase (`clienteCpf`, `dataCompra`, `valorTotal`,
  `metodoPagamento`, `itensPedido`, `skuExterno`, `precoUnitario`...).
- **Erros:** JSON com o campo `code`, que o frontend deve comparar (não o texto):

| `code` | Status | Quando |
|---|---|---|
| `NAO_AUTENTICADO` | 401 | Sem o header `X-API-Key` ou com a chave errada |
| `CPF_INVALIDO` | 400 | CPF sem 11 dígitos |
| `DATA_INVALIDA` | 400 | Data fora do formato `yyyy-MM-dd` |
| `PERIODO_INVALIDO` | 400 | `data_inicio` depois da `data_fim` |
| `PAGINA_INVALIDA` | 400 | `page`/`size` não numérico, ou `page` negativo ou grande demais |
| `NAO_ENCONTRADO` | 404 | Rota inexistente |

Cliente sem compras devolve `200` com a página vazia, não `404`.

### Para o frontend

- Envie `X-API-Key` em todas as chamadas (guarde a chave numa variável de ambiente do frontend).
- O CORS já libera `http://localhost:3000`, `:5173` e `:4200`. Outra porta: defina `CORS_ORIGINS`
  no ambiente do backend.

## 7. Rodar a ETL no Airflow

1. Abra [http://localhost:8082](http://localhost:8082) e entre com
   `AIRFLOW_ADMIN_USER`/`AIRFLOW_ADMIN_PASSWORD` do `.env`.
2. Ative a DAG `etl_historico_vendas` (ela começa pausada).
3. **Play → Trigger DAG** carrega as vendas de **ontem** (fuso de São Paulo).
4. Para carregar um dia específico, use **Play → Trigger DAG w/ config** com, por exemplo, o dia das
   vendas do mock:
   ```json
   {"data": "2026-10-30"}
   ```
5. Acompanhe na visualização **Grid**. No log da task `carregar_bancodados` deve aparecer
   `[INPUT] 10 registos processados`. Rodar de novo o mesmo dia não duplica nada.

## 8. Mexer no backend

```bash
cd backend
./mvnw clean verify     # testes + cobertura mínima de 80% (precisa do Docker Desktop aberto)
./mvnw spotless:apply   # formata o código antes do commit
```

Estrutura (mesmo padrão do FitNear): `venda/` e `cliente/` com `controller`, `service`,
`repository` e `model`; e `infrastructure/` com o que é compartilhado (erros, paginação, CPF e
chave de API). O schema dos testes (`src/test/resources/db/schema.sql`) é uma cópia do
`postgres-init/02-init-app-tables.sh`: se mudar a tabela lá, atualize a cópia (um teste avisa se
divergir).

Depois de mudar o código, recompile o contêiner: `docker compose up -d --build backend`.

## 9. Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `relation "historico_vendas" does not exist` | Carga inicial abortada por CRLF: veja o passo 2 e suba com `down -v`. |
| `maluma_backend` reiniciando sem parar | Falta `API_KEY` no `.env`. Veja com `docker logs maluma_backend`. |
| API responde `401` | Header `X-API-Key` ausente ou com chave diferente da do `.env`. |
| Swagger dá `404` | `SWAGGER_ENABLED` não está `true` no `.env` (depois de mudar, `docker compose up -d backend`). |
| `port is already allocated` | Outra coisa na sua máquina usa a porta (8081, 8082, 8084, 5433 ou 6379). Pare o programa que está usando a porta ou mude o lado esquerdo do mapeamento no compose, só localmente. |
| `./mvnw verify` falha ao subir o Postgres | Docker Desktop fechado: os testes sobem um Postgres temporário. |
| ETL roda com sucesso mas insere 0 | Não há vendas no mock para o dia carregado: use o config `{"data": "2026-10-30"}`. |
