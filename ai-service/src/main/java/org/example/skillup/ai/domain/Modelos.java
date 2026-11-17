package org.example.skillup.ai.domain;

import java.util.List;

/** Objetos de dominio simples do servico de IA. */
public final class Modelos {

    private Modelos() {}

    /** Trecho de um documento institucional recuperado pelo RAG. */
    public record TrechoDocumento(String fonte, String titulo, String texto) {}

    /** Curso do catalogo, vindo do core-service. */
    public record CursoCatalogo(Long id, String nome, String tipo, String descricao) {}

    /** RNF03: o agente so enxerga o perfil profissional, nunca e-mail ou dados pessoais. */
    public record PerfilColaborador(Long id, String nome, String cargo, String area, String nivel) {}

    public record CursoRecomendado(Long cursoId, String nome, String motivo) {}

    /** Resposta completa do RF03: o que foi buscado, o que foi recuperado e o que foi recomendado. */
    public record ResultadoRecomendacao(PerfilColaborador perfil, String consulta, List<TrechoDocumento> trechos,
                                        List<CursoRecomendado> recomendacoes, String modo) {}
}
