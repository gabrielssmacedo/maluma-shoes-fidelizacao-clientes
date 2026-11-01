from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta

# Importação absoluta a partir da raiz da pasta "dags"
from historico_vendas.etl_historico_vendas import extrair_e_transformar, carregar_bancodados

default_args = {
    'owner': 'maluma_adm',
    'depends_on_past': False,
    'retries': 3,
    'retry_delay': timedelta(minutes=1),
}

with DAG(
    'etl_historico_vendas',
    default_args=default_args,
    description='Extrai vendas da API do Sistema de Vendas e injeta no DB',
    schedule_interval='0 3 * * *', # Executa todos os dias às 03:00 AM
    start_date=datetime(2026, 1, 1),
    catchup=False,
    tags=['vendas', 'api'],
) as dag:

    task_extracao = PythonOperator(
        task_id='extrair_e_transformar',
        python_callable=extrair_e_transformar,
    )

    task_carga = PythonOperator(
        task_id='carregar_bancodados',
        python_callable=carregar_bancodados,
    )

    # Ordem cronológica
    task_extracao >> task_carga