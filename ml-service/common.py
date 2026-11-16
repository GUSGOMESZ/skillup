"""Configuracoes e utilitarios compartilhados pela API (app.py) e pelo worker (worker.py)."""
import json
import os
import time
from datetime import datetime, timezone
from pathlib import Path

import pika

DATA_DIR = Path(os.getenv("DATA_DIR", "/data"))
JOBS_DIR = DATA_DIR / "jobs"
MODEL_PATH = DATA_DIR / "modelo.joblib"
DATASET_PATH = DATA_DIR / "dataset.csv"

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
QUEUE_REQUESTS = "ml.training.requests"   # pedidos de treino (API publica, worker consome)
QUEUE_RESULTS = "ml.training.results"     # eventos de resultado (worker publica)


def agora() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def conectar_rabbit(tentativas: int = 1, espera: float = 3.0) -> pika.BlockingConnection:
    """Abre conexao com o RabbitMQ, tentando de novo enquanto ele sobe."""
    ultimo_erro = None
    for _ in range(max(1, tentativas)):
        try:
            return pika.BlockingConnection(
                pika.ConnectionParameters(host=RABBITMQ_HOST, heartbeat=600, blocked_connection_timeout=300)
            )
        except pika.exceptions.AMQPConnectionError as erro:
            ultimo_erro = erro
            time.sleep(espera)
    raise ultimo_erro


def salvar_job(job: dict) -> None:
    """Grava o status do treino em /data/jobs/<id>.json (escrita atomica)."""
    JOBS_DIR.mkdir(parents=True, exist_ok=True)
    destino = JOBS_DIR / f"{job['job_id']}.json"
    temporario = destino.with_suffix(".tmp")
    temporario.write_text(json.dumps(job, ensure_ascii=False, indent=2), encoding="utf-8")
    os.replace(temporario, destino)


def ler_job(job_id: str):
    arquivo = JOBS_DIR / f"{job_id}.json"
    if not arquivo.exists():
        return None
    return json.loads(arquivo.read_text(encoding="utf-8"))


def listar_jobs() -> list:
    if not JOBS_DIR.exists():
        return []
    jobs = [json.loads(p.read_text(encoding="utf-8")) for p in JOBS_DIR.glob("*.json")]
    return sorted(jobs, key=lambda j: j.get("criado_em", ""), reverse=True)
