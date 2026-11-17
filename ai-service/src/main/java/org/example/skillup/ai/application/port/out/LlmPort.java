package org.example.skillup.ai.application.port.out;

import org.example.skillup.ai.domain.Modelos.CursoCatalogo;
import org.example.skillup.ai.domain.Modelos.CursoRecomendado;
import org.example.skillup.ai.domain.Modelos.PerfilColaborador;
import org.example.skillup.ai.domain.Modelos.TrechoDocumento;

import java.util.List;

/** Porta de SAIDA para o modelo de linguagem. Implementacoes: OpenAI (Spring AI) ou mock. */
public interface LlmPort {

    List<CursoRecomendado> recomendar(PerfilColaborador perfil, String interesses,
                                      List<TrechoDocumento> contexto, List<CursoCatalogo> catalogo);

    /** "openai" ou "mock" - aparece no front para ficar claro qual adapter respondeu. */
    String modo();
}
