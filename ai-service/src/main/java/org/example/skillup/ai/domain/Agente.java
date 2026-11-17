package org.example.skillup.ai.domain;

import java.util.Set;

/**
 * Agentes de IA e as ferramentas que cada um pode usar (RNF03: controle de acesso dos agentes).
 * O Orientador so matricula; o Acompanhamento so agenda conversas.
 */
public enum Agente {
    ORIENTADOR("Orienta a trilha de cursos", Set.of(Ferramenta.MATRICULAR_EM_CURSO)),
    ACOMPANHAMENTO("Acompanha a evolucao do colaborador", Set.of(Ferramenta.AGENDAR_ACOMPANHAMENTO));

    private final String descricao;
    private final Set<Ferramenta> ferramentasPermitidas;

    Agente(String descricao, Set<Ferramenta> ferramentasPermitidas) {
        this.descricao = descricao;
        this.ferramentasPermitidas = ferramentasPermitidas;
    }

    public boolean podeUsar(Ferramenta ferramenta) {
        return ferramentasPermitidas.contains(ferramenta);
    }

    public String getDescricao() { return descricao; }
    public Set<Ferramenta> getFerramentasPermitidas() { return ferramentasPermitidas; }
}
