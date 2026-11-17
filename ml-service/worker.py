"""
ml-worker - Consumer do RabbitMQ (RF05 / RNF02)

Fica escutando a fila ml.training.requests. Para cada mensagem: marca o treino
como PROCESSANDO, treina o modelo, grava o resultado e so entao confirma (ACK).
Se o worker estiver parado, as mensagens ficam guardadas na fila.
"""
import json
import os
import time

import pika

from common import (QUEUE_REQUESTS, QUEUE_RESULTS, agora, conectar_rabbit,
                    ler_job, salvar_job)
from training import treinar

# Atraso proposital para dar tempo de ver o status mudar no front durante a demo
ATRASO_SEGUNDOS = int(os.getenv("TREINO_DELAY_SEG", "8"))


def ao_receber(canal, metodo, _propriedades, corpo):
    pedido = json.loads(corpo)
    job_id = pedido["job_id"]
    job = ler_job(job_id) or {"job_id": job_id, "seed": pedido.get("seed", 42), "criado_em": agora()}
    print(f"Recebido treino {job_id}", flush=True)

    job.update(status="PROCESSANDO", iniciado_em=agora(), atualizado_em=agora())
    salvar_job(job)
    try:
        time.sleep(ATRASO_SEGUNDOS)
        metricas = treinar(job_id=job_id, seed=int(pedido.get("seed", 42)))
        job.update(status="CONCLUIDO", metricas=metricas, concluido_em=agora(), atualizado_em=agora())
    except Exception as erro:
        job.update(status="FALHOU", erro=str(erro), atualizado_em=agora())
    salvar_job(job)

    canal.basic_publish(
        exchange="", routing_key=QUEUE_RESULTS,
        body=json.dumps({"job_id": job_id, "status": job["status"]}),
        properties=pika.BasicProperties(delivery_mode=2),
    )
    # ACK manual: so depois de gravar o resultado. Se cair antes, o RabbitMQ reentrega.
    canal.basic_ack(delivery_tag=metodo.delivery_tag)
    print(f"Treino {job_id}: {job['status']}", flush=True)


def main():
    while True:
        try:
            conexao = conectar_rabbit(tentativas=20)
            canal = conexao.channel()
            canal.queue_declare(queue=QUEUE_REQUESTS, durable=True)
            canal.queue_declare(queue=QUEUE_RESULTS, durable=True)
            canal.basic_qos(prefetch_count=1)  # um treino por vez
            canal.basic_consume(queue=QUEUE_REQUESTS, on_message_callback=ao_receber)
            print("ml-worker pronto, aguardando pedidos de treino...", flush=True)
            canal.start_consuming()
        except pika.exceptions.AMQPConnectionError:
            print("RabbitMQ indisponivel, tentando de novo em 5s...", flush=True)
            time.sleep(5)


if __name__ == "__main__":
    main()
