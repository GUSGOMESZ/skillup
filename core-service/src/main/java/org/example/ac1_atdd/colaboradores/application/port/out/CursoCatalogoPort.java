package org.example.ac1_atdd.colaboradores.application.port.out;

import org.example.ac1_atdd.colaboradores.domain.Registros.CursoInfo;

import java.util.List;
import java.util.Optional;

/** Porta de SAIDA para o catalogo de cursos (tabela compartilhada com o modulo ranking). */
public interface CursoCatalogoPort {

    Optional<CursoInfo> buscar(Long id);

    List<CursoInfo> listar();

    CursoInfo salvar(String nome, String tipo, String descricao);
}
