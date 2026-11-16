"""
ml-service (FastAPI) - RF02 e RF05

- POST /predict           -> classificacao SINCRONA (REST): responde na hora
- POST /training/jobs     -> pedido de treino ASSINCRONO: publica no RabbitMQ e responde 202
- GET  /training/jobs     -> acompanhamento dos treinos (RF05)
- GET  /fila              -> mensagens paradas na fila (usado na demo do RNF02)
"""
import json
import time
import uuid
from contextlib import asynccontextmanager

import joblib
import numpy as np
import pika
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from common import (DATA_DIR, MODEL_PATH, QUEUE_REQUESTS, agora, conectar_rabbit,
                    ler_job, listar_jobs, salvar_job)
from training import NIVEIS, treinar

_cache = {"mtime": None, "pacote": None}


def carregar_modelo():
    """Carrega o modelo do disco e recarrega sozinho quando o worker grava uma versao nova."""
    mtime = MODEL_PATH.stat().st_mtime
    if _cache["mtime"] != mtime:
        _cache["pacote"] = joblib.load(MODEL_PATH)
        _cache["mtime"] = mtime
    return _cache["pacote"]


@asynccontextmanager
async def lifespan(_app: FastAPI):
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    if not MODEL_PATH.exists():
        print("Nenhum modelo encontrado, treinando versao inicial...", flush=True)
        treinar("inicial", 42)
    yield


app = FastAPI(title="SkillUp ML Service", lifespan=lifespan)


class Perfil(BaseModel):
    meses_experiencia: float = Field(ge=0, le=600)
    cursos_concluidos: float = Field(ge=0, le=500)
    media_avaliacoes: float = Field(ge=0, le=10)
    horas_estudo_semana: float = Field(ge=0, le=80)


class PedidoTreino(BaseModel):
    seed: int = 42


@app.get("/health")
def health():
    return {"status": "UP", "modelo_carregado": MODEL_PATH.exists()}


@app.post("/predict")
def predict(perfil: Perfil):
    inicio = time.perf_counter()
    pacote = carregar_modelo()
    entrada = np.array([[perfil.meses_experiencia, perfil.cursos_concluidos,
                         perfil.media_avaliacoes, perfil.horas_estudo_semana]])
    probabilidades = pacote["modelo"].predict_proba(entrada)[0]
    indice = int(np.argmax(probabilidades))
    return {
        "nivel": NIVEIS[indice],
        "probabilidades": {NIVEIS[i]: round(float(p), 3) for i, p in enumerate(probabilidades)},
        "modelo": pacote["metricas"],
        "tempo_ms": round((time.perf_counter() - inicio) * 1000, 1),
    }


@app.post("/training/jobs", status_code=202)
def solicitar_treino(pedido: PedidoTreino = PedidoTreino()):
    """Producer: so registra o pedido e publica na fila. Quem treina e o worker."""
    job = {"job_id": uuid.uuid4().hex[:8], "seed": pedido.seed, "status": "NA_FILA",
           "criado_em": agora(), "atualizado_em": agora()}
    salvar_job(job)
    try:
        conexao = conectar_rabbit(tentativas=1)
        canal = conexao.channel()
        canal.queue_declare(queue=QUEUE_REQUESTS, durable=True)
        canal.basic_publish(
            exchange="",                      # exchange padrao (direct)
            routing_key=QUEUE_REQUESTS,       # vai para a fila com este nome
            body=json.dumps({"job_id": job["job_id"], "seed": pedido.seed}),
            properties=pika.BasicProperties(delivery_mode=2),  # mensagem persistente
        )
        conexao.close()
    except Exception as erro:  # RabbitMQ fora do ar
        job.update(status="ERRO_PUBLICACAO", erro=str(erro), atualizado_em=agora())
        salvar_job(job)
        return JSONResponse(status_code=503, content=job)
    return job


@app.get("/training/jobs")
def listar_treinos():
    return listar_jobs()


@app.get("/training/jobs/{job_id}")
def status_treino(job_id: str):
    job = ler_job(job_id)
    if job is None:
        raise HTTPException(status_code=404, detail="Treino nao encontrado")
    return job


@app.get("/fila")
def situacao_fila():
    """Quantas mensagens estao esperando e quantos consumidores (workers) estao ligados."""
    try:
        conexao = conectar_rabbit(tentativas=1)
        fila = conexao.channel().queue_declare(queue=QUEUE_REQUESTS, durable=True, passive=True)
        resposta = {"fila": QUEUE_REQUESTS, "mensagens_esperando": fila.method.message_count,
                    "workers_conectados": fila.method.consumer_count}
        conexao.close()
        return resposta
    except Exception as erro:
        return JSONResponse(status_code=503, content={"erro": str(erro)})
