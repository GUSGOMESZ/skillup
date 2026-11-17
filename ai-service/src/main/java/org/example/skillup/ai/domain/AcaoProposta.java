package org.example.skillup.ai.domain;

import java.time.LocalDateTime;
import java.util.Map;

/** Acao que um agente quer executar. Fica PENDENTE ate o usuario aprovar ou rejeitar. */
public class AcaoProposta {

    private final Long id;
    private final Agente agente;
    private final Ferramenta ferramenta;
    private final Long colaboradorId;
    private final String descricao;
    private final Map<String, String> parametros;
    private final LocalDateTime criadaEm = LocalDateTime.now();
    private StatusAcao status = StatusAcao.PENDENTE;
    private String resultado;

    public AcaoProposta(Long id, Agente agente, Ferramenta ferramenta, Long colaboradorId,
                        String descricao, Map<String, String> parametros) {
        this.id = id;
        this.agente = agente;
        this.ferramenta = ferramenta;
        this.colaboradorId = colaboradorId;
        this.descricao = descricao;
        this.parametros = parametros;
    }

    public void concluir(StatusAcao novoStatus, String resultado) {
        if (status != StatusAcao.PENDENTE) {
            throw new IllegalStateException("Acao " + id + " ja foi " + status);
        }
        this.status = novoStatus;
        this.resultado = resultado;
    }

    public Long getId() { return id; }
    public Agente getAgente() { return agente; }
    public Ferramenta getFerramenta() { return ferramenta; }
    public Long getColaboradorId() { return colaboradorId; }
    public String getDescricao() { return descricao; }
    public Map<String, String> getParametros() { return parametros; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public StatusAcao getStatus() { return status; }
    public String getResultado() { return resultado; }
}
