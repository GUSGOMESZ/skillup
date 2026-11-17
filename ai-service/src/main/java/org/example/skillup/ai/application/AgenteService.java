package org.example.skillup.ai.application;

import org.example.skillup.ai.application.port.out.CorePort;
import org.example.skillup.ai.domain.AcaoProposta;
import org.example.skillup.ai.domain.AcessoNegadoException;
import org.example.skillup.ai.domain.Agente;
import org.example.skillup.ai.domain.Ferramenta;
import org.example.skillup.ai.domain.Modelos.CursoRecomendado;
import org.example.skillup.ai.domain.Modelos.PerfilColaborador;
import org.example.skillup.ai.domain.Modelos.ResultadoRecomendacao;
import org.example.skillup.ai.domain.NaoEncontradoException;
import org.example.skillup.ai.domain.StatusAcao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RF04 - Agentes de IA coordenam orientacao e acompanhamento COM AUTORIZACAO do usuario.
 * O agente so PROPOE acoes; nada acontece no core ate o usuario aprovar.
 * RNF03 - cada agente so usa as ferramentas liberadas para ele.
 */
@Service
public class AgenteService {

    private final RecomendarCursosService recomendacao;
    private final CorePort core;
    private final Map<Long, AcaoProposta> acoes = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    public AgenteService(RecomendarCursosService recomendacao, CorePort core) {
        this.recomendacao = recomendacao;
        this.core = core;
    }

    /** O agente analisa o colaborador e devolve as acoes que ele sugere (todas PENDENTES). */
    public List<AcaoProposta> planejar(Agente agente, Long colaboradorId, String interesses) {
        List<AcaoProposta> propostas = new ArrayList<>();
        if (agente == Agente.ORIENTADOR) {
            ResultadoRecomendacao r = recomendacao.recomendar(colaboradorId, interesses);
            if (!r.recomendacoes().isEmpty()) {
                CursoRecomendado melhor = r.recomendacoes().get(0);
                propostas.add(propor(agente, Ferramenta.MATRICULAR_EM_CURSO, colaboradorId,
                        "Matricular " + r.perfil().nome() + " em \"" + melhor.nome() + "\". Motivo: " + melhor.motivo(),
                        Map.of("cursoId", String.valueOf(melhor.cursoId()), "cursoNome", melhor.nome())));
            }
        } else if (agente == Agente.ACOMPANHAMENTO) {
            PerfilColaborador perfil = core.perfil(colaboradorId);
            String data = LocalDate.now().plusDays(7).toString();
            String assunto = (interesses == null || interesses.isBlank()) ? "evolucao nos cursos" : interesses.trim();
            propostas.add(propor(agente, Ferramenta.AGENDAR_ACOMPANHAMENTO, colaboradorId,
                    "Agendar conversa de acompanhamento com " + perfil.nome() + " em " + data + " sobre: " + assunto,
                    Map.of("dataPrevista", data, "descricao", "Acompanhamento (" + perfil.nivel() + "): " + assunto)));
        }
        return propostas;
    }

    /** Registra uma acao proposta. Bloqueia ferramentas fora da permissao do agente. */
    public AcaoProposta propor(Agente agente, Ferramenta ferramenta, Long colaboradorId,
                               String descricao, Map<String, String> parametros) {
        verificarPermissao(agente, ferramenta);
        AcaoProposta acao = new AcaoProposta(sequencia.incrementAndGet(), agente, ferramenta,
                colaboradorId, descricao, parametros);
        acoes.put(acao.getId(), acao);
        return acao;
    }

    public List<AcaoProposta> listar(Long colaboradorId) {
        return acoes.values().stream()
                .filter(a -> colaboradorId == null || a.getColaboradorId().equals(colaboradorId))
                .sorted(Comparator.comparing(AcaoProposta::getId).reversed())
                .toList();
    }

    /** Usuario autorizou: so agora a ferramenta e executada no core-service. */
    public AcaoProposta aprovar(Long acaoId) {
        AcaoProposta acao = buscar(acaoId);
        if (acao.getStatus() != StatusAcao.PENDENTE) {
            throw new IllegalStateException("Acao " + acaoId + " ja foi " + acao.getStatus());
        }
        verificarPermissao(acao.getAgente(), acao.getFerramenta());
        String resultado;
        try {
            resultado = switch (acao.getFerramenta()) {
                case MATRICULAR_EM_CURSO -> core.matricular(acao.getColaboradorId(),
                        Long.valueOf(acao.getParametros().get("cursoId")));
                case AGENDAR_ACOMPANHAMENTO -> core.agendarAcompanhamento(acao.getColaboradorId(),
                        acao.getParametros().get("dataPrevista"), acao.getParametros().get("descricao"));
            };
        } catch (RuntimeException erro) {
            acao.concluir(StatusAcao.FALHOU, erro.getMessage());
            return acao;
        }
        acao.concluir(StatusAcao.EXECUTADA, resultado);
        return acao;
    }

    public AcaoProposta rejeitar(Long acaoId) {
        AcaoProposta acao = buscar(acaoId);
        acao.concluir(StatusAcao.REJEITADA, "Rejeitada pelo usuario");
        return acao;
    }

    private AcaoProposta buscar(Long acaoId) {
        AcaoProposta acao = acoes.get(acaoId);
        if (acao == null) {
            throw new NaoEncontradoException("Acao " + acaoId + " nao encontrada");
        }
        return acao;
    }

    private void verificarPermissao(Agente agente, Ferramenta ferramenta) {
        if (!agente.podeUsar(ferramenta)) {
            throw new AcessoNegadoException("Agente " + agente + " nao tem permissao para usar " + ferramenta
                    + ". Permitidas: " + agente.getFerramentasPermitidas());
        }
    }
}
