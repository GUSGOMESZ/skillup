package org.example.skillup.ai.domain;

/** Ciclo de vida de uma acao proposta: nada e executado sem aprovacao do usuario (RF04). */
public enum StatusAcao {
    PENDENTE,
    EXECUTADA,
    REJEITADA,
    FALHOU
}
