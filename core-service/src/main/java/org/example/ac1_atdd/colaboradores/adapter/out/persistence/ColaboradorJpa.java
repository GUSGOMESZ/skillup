package org.example.ac1_atdd.colaboradores.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColaboradorJpa extends JpaRepository<ColaboradorEntity, Long> {
    boolean existsByEmail(String email);

    List<ColaboradorEntity> findAllByOrderByIdAsc();
}
