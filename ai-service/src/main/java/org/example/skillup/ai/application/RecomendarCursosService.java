package org.example.skillup.ai.application;

import org.example.skillup.ai.application.port.out.BuscaDocumentosPort;
import org.example.skillup.ai.application.port.out.CorePort;
import org.example.skillup.ai.application.port.out.LlmPort;
import org.example.skillup.ai.domain.Modelos.CursoCatalogo;
import org.example.skillup.ai.domain.Modelos.CursoRecomendado;
import org.example.skillup.ai.domain.Modelos.PerfilColaborador;
import org.example.skillup.ai.domain.Modelos.ResultadoRecomendacao;
import org.example.skillup.ai.domain.Modelos.TrechoDocumento;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RF03 - Caso de uso de recomendacao com RAG:
 * 1) busca o perfil no core (nivel vem do ML);
 * 2) RECUPERA os trechos mais relevantes dos documentos institucionais;
 * 3) GERA a recomendacao com o LLM usando so esses trechos + o catalogo.
 */
@Service
public class RecomendarCursosService {

    private static final int TRECHOS = 3;

    private final CorePort core;
    private final BuscaDocumentosPort busca;
    private final LlmPort llm;

    public RecomendarCursosService(CorePort core, BuscaDocumentosPort busca, LlmPort llm) {
        this.core = core;
        this.busca = busca;
        this.llm = llm;
    }

    public ResultadoRecomendacao recomendar(Long colaboradorId, String interesses) {
        PerfilColaborador perfil = core.perfil(colaboradorId);
        String textoInteresses = interesses == null ? "" : interesses.trim();

        String consulta = "Colaborador de nivel " + perfil.nivel() + ", cargo " + perfil.cargo()
                + ", area " + perfil.area() + ". Interesses: " + textoInteresses;
        List<TrechoDocumento> trechos = busca.buscar(consulta, TRECHOS);

        List<CursoCatalogo> catalogo = core.cursos();
        List<CursoRecomendado> recomendacoes = llm.recomendar(perfil, textoInteresses, trechos, catalogo);

        return new ResultadoRecomendacao(perfil, consulta, trechos, recomendacoes, llm.modo());
    }
}
