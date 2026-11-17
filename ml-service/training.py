"""Treino do modelo de classificacao de competencia (Regressao Logistica, scikit-learn)."""
import csv

import joblib
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

from common import DATA_DIR, DATASET_PATH, MODEL_PATH, agora

FEATURES = ["meses_experiencia", "cursos_concluidos", "media_avaliacoes", "horas_estudo_semana"]
NIVEIS = ["INICIANTE", "INTERMEDIARIO", "AVANCADO"]


def gerar_dataset(n: int = 1500, seed: int = 42):
    """Dataset sintetico: quanto mais experiencia, cursos, nota e estudo, maior o nivel."""
    rng = np.random.default_rng(seed)
    meses = rng.integers(0, 121, n)
    cursos = rng.integers(0, 31, n)
    media = np.round(rng.uniform(0, 10, n), 1)
    horas = rng.integers(0, 21, n)
    X = np.column_stack([meses, cursos, media, horas]).astype(float)

    pontuacao = 0.02 * meses + 0.08 * cursos + 0.25 * media + 0.08 * horas + rng.normal(0, 0.4, n)
    corte1, corte2 = np.quantile(pontuacao, [0.4, 0.75])
    y = np.where(pontuacao < corte1, 0, np.where(pontuacao < corte2, 1, 2))

    DATA_DIR.mkdir(parents=True, exist_ok=True)
    with open(DATASET_PATH, "w", newline="", encoding="utf-8") as f:
        escritor = csv.writer(f)
        escritor.writerow(FEATURES + ["nivel"])
        for linha, rotulo in zip(X, y):
            escritor.writerow(list(linha) + [NIVEIS[rotulo]])
    return X, y


def treinar(job_id: str = "inicial", seed: int = 42) -> dict:
    """Treina, avalia e salva o modelo. Retorna as metricas."""
    X, y = gerar_dataset(seed=seed)
    X_treino, X_teste, y_treino, y_teste = train_test_split(
        X, y, test_size=0.2, stratify=y, random_state=seed
    )
    modelo = make_pipeline(StandardScaler(), LogisticRegression(max_iter=1000))
    modelo.fit(X_treino, y_treino)

    previsto = modelo.predict(X_teste)
    metricas = {
        "versao": job_id,
        "seed": seed,
        "acuracia": round(float(accuracy_score(y_teste, previsto)), 4),
        "f1_macro": round(float(f1_score(y_teste, previsto, average="macro")), 4),
        "amostras": int(len(X)),
        "treinado_em": agora(),
    }

    temporario = MODEL_PATH.with_suffix(".tmp")
    joblib.dump({"modelo": modelo, "metricas": metricas}, temporario)
    temporario.replace(MODEL_PATH)
    return metricas
