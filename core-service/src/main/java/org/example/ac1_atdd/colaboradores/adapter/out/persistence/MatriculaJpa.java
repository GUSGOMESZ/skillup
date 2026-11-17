package org.example.ac1_atdd.colaboradores.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaJpa extends JpaRepository<MatriculaEntity, Long> {
    List<MatriculaEntity> findByColaboradorIdOrderByIdDesc(Long colaboradorId);

    boolean existsByColaboradorIdAndCursoId(Long colaboradorId, Long cursoId);
}
