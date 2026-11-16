package org.example.ac1_atdd.colaboradores.application.port.out;

import org.example.ac1_atdd.colaboradores.domain.Colaborador;
import org.example.ac1_atdd.colaboradores.domain.Registros.Acompanhamento;
import org.example.ac1_atdd.colaboradores.domain.Registros.Matricula;

import java.util.List;
import java.util.Optional;

/**
 * Porta de SAIDA: o que o modulo precisa de um banco de dados.
 * Quem implementa e o adapter JPA; o servico nao sabe que existe Postgres.
 */
public interface ColaboradorRepositoryPort {

    Colaborador salvar(Colaborador colaborador);

    Optional<Colaborador> buscarPorId(Long id);

    List<Colaborador> listarTodos();

    boolean existeEmail(String email);

    Matricula salvarMatricula(Matricula matricula);

    List<Matricula> matriculasDo(Long colaboradorId);

    boolean jaMatriculado(Long colaboradorId, Long cursoId);

    Acompanhamento salvarAcompanhamento(Acompanhamento acompanhamento);

    List<Acompanhamento> acompanhamentosDo(Long colaboradorId);
}
