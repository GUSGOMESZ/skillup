package org.example.ac1_atdd.colaboradores.application.port.in;

import org.example.ac1_atdd.colaboradores.domain.Registros.CursoInfo;

import java.util.List;

/** Porta de ENTRADA para o catalogo de cursos (RF01). */
public interface GerenciarCursosUseCase {

    CursoInfo cadastrarCurso(String nome, String tipo, String descricao);

    List<CursoInfo> listarCursos();
}
