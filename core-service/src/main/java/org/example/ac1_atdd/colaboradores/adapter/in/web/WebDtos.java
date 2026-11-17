package org.example.ac1_atdd.colaboradores.adapter.in.web;

import org.example.ac1_atdd.colaboradores.domain.Colaborador;
import org.example.ac1_atdd.colaboradores.domain.Registros.Acompanhamento;
import org.example.ac1_atdd.colaboradores.domain.Registros.CursoInfo;
import org.example.ac1_atdd.colaboradores.domain.Registros.Matricula;

/** Formatos JSON de entrada e saida da API REST do modulo colaboradores. */
public final class WebDtos {

    private WebDtos() {}

    public record ColaboradorRequest(String nome, String email, String cargo, String area) {}

    public record NivelRequest(String nivel) {}

    public record MatriculaRequest(Long cursoId, String origem) {}

    public record AcompanhamentoRequest(String dataPrevista, String descricao, String origem) {}

    public record CursoRequest(String nome, String tipo, String descricao) {}

    /** RNF03: o e-mail sai mascarado. */
    public record ColaboradorResponse(Long id, String nome, String email, String cargo, String area, String nivel) {
        static ColaboradorResponse de(Colaborador c) {
            return new ColaboradorResponse(c.getId(), c.getNome(), c.emailMascarado(), c.getCargo(),
                    c.getArea(), c.getNivel().name());
        }
    }

    public record MatriculaResponse(Long id, Long colaboradorId, Long cursoId, String cursoNome,
                                    String origem, String data) {
        static MatriculaResponse de(Matricula m) {
            return new MatriculaResponse(m.id(), m.colaboradorId(), m.cursoId(), m.cursoNome(),
                    m.origem(), String.valueOf(m.data()));
        }
    }

    public record AcompanhamentoResponse(Long id, Long colaboradorId, String dataPrevista,
                                         String descricao, String origem) {
        static AcompanhamentoResponse de(Acompanhamento a) {
            return new AcompanhamentoResponse(a.id(), a.colaboradorId(), String.valueOf(a.dataPrevista()),
                    a.descricao(), a.origem());
        }
    }

    public record CursoResponse(Long id, String nome, String tipo, String descricao) {
        static CursoResponse de(CursoInfo c) {
            return new CursoResponse(c.id(), c.nome(), c.tipo(), c.descricao());
        }
    }

    public record Erro(String erro) {}
}
