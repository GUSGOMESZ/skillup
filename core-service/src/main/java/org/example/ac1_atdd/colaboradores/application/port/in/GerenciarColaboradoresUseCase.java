package org.example.ac1_atdd.colaboradores.application.port.in;

import org.example.ac1_atdd.colaboradores.domain.Colaborador;
import org.example.ac1_atdd.colaboradores.domain.Registros.Acompanhamento;
import org.example.ac1_atdd.colaboradores.domain.Registros.Matricula;

import java.time.LocalDate;
import java.util.List;

/** Porta de ENTRADA: o que o mundo de fora pode pedir ao modulo (RF01). */
public interface GerenciarColaboradoresUseCase {

    Colaborador cadastrar(String nome, String email, String cargo, String area);

    List<Colaborador> listar();

    Colaborador buscar(Long id);

    /** Grava o nivel calculado pelo servico de ML (RF02). */
    Colaborador classificar(Long id, String nivel);

    Matricula matricular(Long colaboradorId, Long cursoId, String origem);

    List<Matricula> matriculas(Long colaboradorId);

    Acompanhamento agendarAcompanhamento(Long colaboradorId, LocalDate dataPrevista, String descricao, String origem);

    List<Acompanhamento> acompanhamentos(Long colaboradorId);
}
