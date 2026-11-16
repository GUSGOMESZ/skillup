package org.example.ac1_atdd.colaboradores.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcompanhamentoJpa extends JpaRepository<AcompanhamentoEntity, Long> {
    List<AcompanhamentoEntity> findByColaboradorIdOrderByDataPrevistaAsc(Long colaboradorId);
}
