package org.example.skillup.ai.application.port.out;

import org.example.skillup.ai.domain.Modelos.CursoCatalogo;
import org.example.skillup.ai.domain.Modelos.PerfilColaborador;

import java.util.List;

/** Porta de SAIDA para o core-service (chamadas REST sincronas). */
public interface CorePort {

    PerfilColaborador perfil(Long colaboradorId);

    List<CursoCatalogo> cursos();

    String matricular(Long colaboradorId, Long cursoId);

    String agendarAcompanhamento(Long colaboradorId, String dataPrevista, String descricao);
}
