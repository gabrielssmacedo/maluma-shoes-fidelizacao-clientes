import json
from pathlib import Path
import uuid
from datetime import datetime, timedelta, timezone
import os
import json
import psycopg2
from psycopg2.extras import execute_values
import logging
from dotenv import load_dotenv


logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    datefmt='[%Y-%m-%d %H:%M:%S]' # Define o formato exato aqui
)
logger = logging.getLogger(__name__)


def get_mock_api() -> dict:
    vendas_api = None
    logger.info(f"[API] Consultando API do Sistema de Vendas")
    diretorio_atual = os.path.dirname(os.path.abspath(__file__))
    caminho_arquivo = os.path.join(diretorio_atual, 'dados_mock', 'dados_api_vendas.json')
    try:
        with open(caminho_arquivo, 'r', encoding='utf-8') as f:
            vendas_api = json.load(f)
    except Exception as e:
        logger.error(f"[API] Erro ao consultar API: {e}")
    return vendas_api




def extrair_e_transformar(**kwargs):
    # O logical_date garante que se rodar um job de dias atrás, ele filtra a data correta
    data_execucao = datetime.now().astimezone()
    ontem = (data_execucao - timedelta(days=2)).date()

    logger.info(f"[INICIO] Iniciando a ETL do histórico vendas do dia {ontem}")

    vendas_api = get_mock_api()

    if vendas_api is None:
        logger.warning("[COLETA] Nenhuma venda filtrada para insercao nesta data.")
        return []

    logger.info(f"[COLETA] Vendas retornadas {len(vendas_api)}")

    vendas_processadas = []

    for venda in vendas_api:
        data_venda = datetime.fromisoformat(venda['data_compra'].replace('Z', '+00:00'))
        
        # Filtra apenas vendas do dia anterior
        if data_venda.date() == ontem:
            # Gera o ID da tabela principal em Python para associá-lo aos itens
            compra_id = str(uuid.uuid4())
            
            itens_pedido = []
            for item in venda['itens_pedido']:
                itens_pedido.append({
                    "id": str(uuid.uuid4()),
                    "compra_id": compra_id,
                    "sku_externo": str(item["sku_externo"]),
                    "nome_produto": str(item["nome_produto"]),
                    "categoria": str(item["categoria"]),
                    "tamanho": int(item["tamanho"]),
                    "cor": str(item["cor"]),
                    "preco_unitario": float(item["preco_unitario"]),
                    "quantidade": int(item["quantidade"])
                })

            vendas_processadas.append({
                "id": compra_id,
                "pedido_externo_id": str(venda["pedido_externo_id"]),
                "cliente_cpf": str(venda["cliente_cpf"]),
                "data_compra": data_venda.isoformat(),
                "valor_total": float(venda["valor_total"]),
                "metodo_pagamento": str(venda["metodo_pagamento"]),
                "itens_pedido": itens_pedido
            })

    logger.info(f"[FORMATACAO] Dados formatados")
            
    # Retornar o valor injeta automaticamente a lista no XCom para a próxima task
    return vendas_processadas




def carregar_bancodados(**kwargs):
    ti = kwargs['ti']
    vendas = ti.xcom_pull(task_ids='extrair_e_transformar')

    if not vendas:
        logger.warning("[COLETA] Nenhuma venda filtrada para insercao nesta data.")
        return

    # Extrai as credenciais diretamente das variáveis de ambiente injetadas pelo .env
    db_user = os.getenv('POSTGRES_USER')
    db_pass = os.getenv('POSTGRES_PASSWORD')
    db_name = os.getenv('POSTGRES_DB')

    # O placeholder %s único é exigido pelo execute_values
    query = """
        INSERT INTO historico_vendas 
        (id, pedido_externo_id, cliente_cpf, data_compra, valor_total, metodo_pagamento, itens_pedido)
        VALUES %s
        ON CONFLICT (pedido_externo_id) DO NOTHING;
    """

    # Transforma a lista de dicionários numa lista de tuplas para a inserção em lote
    dados_formatados = [
        (
            venda['id'],
            venda['pedido_externo_id'],
            venda['cliente_cpf'],
            venda['data_compra'],
            venda['valor_total'],
            venda['metodo_pagamento'],
            json.dumps(venda['itens_pedido'])
        )
        for venda in vendas
    ]

    conn = None
    cursor = None
    try:
        # Estabelece a ligação direta através da biblioteca psycopg2
        conn = psycopg2.connect(
            host="postgres", # Resolve para o nome do contentor na rede Docker
            port=5432,
            database=db_name,
            user=db_user,
            password=db_pass,
            client_encoding='utf8'
        )
        logger.info(f"[BANCO] {db_name} conectado com sucesso")
        cursor = conn.cursor()

        # execute_values compila todas as tuplas num único comando SQL INSERT
        execute_values(cursor, query, dados_formatados)
        conn.commit()
        logger.info(f"[INPUT] {len(dados_formatados)} registos processados em lote com sucesso.")
    except Exception as e:
        # Verifica se a conexão foi estabelecida antes de tentar o rollback
        if conn is not None:
            conn.rollback()
        logger.error(f"[INPUT] Erro ao inserir registos: {repr(e)}")
        raise e
        
    finally:
        # Verifica se as instâncias existem antes de tentar fechá-las
        if cursor is not None:
            cursor.close()
        if conn is not None:
            conn.close()
            logger.info(f"[BANCO] {db_name} conexao encerrada")
        logger.info(f"[FIM] Finalizando ETL do historico de vendas")


'''if __name__ == "__main__":
    # Carrega as variáveis do .env localizado na raiz do projeto
    caminho_env_raiz = Path(__file__).resolve().parents[4] / '.env'
    load_dotenv(dotenv_path=caminho_env_raiz)
    dados = extrair_e_transformar()

    class MockTI:
        def xcom_pull(self, task_ids):
            return dados 

    carregar_bancodados(ti=MockTI())'''