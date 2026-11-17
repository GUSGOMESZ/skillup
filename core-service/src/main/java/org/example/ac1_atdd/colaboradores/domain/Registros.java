package org.example.ac1_atdd.colaboradores.domain;

import java.time.LocalDate;

/** Objetos de dominio simples (records) usados pelo modulo colaboradores. */
public final class Registros {

    private Registros() {}

    /** Curso visto pelo dominio, sem depender da entidade JPA. */
    public record CursoInfo(Long id, String nome, String tipo, String descricao) {}

    /** Matricula de um colaborador em um curso. origem = MANUAL ou AGENTE. */
    public record Matricula(Long id, Long colaboradorId, Long cursoId, String cursoNome,
                            String origem, LocalDate data) {}

    /** Acompanhamento agendado (normalmente proposto pelo agente de IA, RF04). */
    public record Acompanhamento(Long id, Long colaboradorId, LocalDate dataPrevista,
                                 String descricao, String origem) {}
}
