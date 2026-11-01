# Maluma Shoes - Sistema de Fidelização de Clientes

Este repositório contém a infraestrutura, backend, frontend e pipelines de dados do sistema de fidelização da Maluma Shoes. O projeto utiliza Docker para orquestrar o banco de dados PostgreSQL, backend em Java/Quarkus e Apache Airflow para rotinas de ETL.

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
└── docker-compose.yaml    # Arquivo de orquestração da infraestrutura Docker
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
