package org.example.ac1_atdd.colaboradores.adapter.out.persistence;

import org.example.ac1_atdd.colaboradores.application.port.out.CursoCatalogoPort;
import org.example.ac1_atdd.colaboradores.domain.Registros.CursoInfo;
import org.example.ac1_atdd.shared.domain.Curso;
import org.example.ac1_atdd.shared.repository.CursoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adapter de SAIDA para o catalogo: reaproveita a tabela "cursos" do modulo shared. */
@Component
public class CursoCatalogoAdapter implements CursoCatalogoPort {

    private final CursoRepository cursos;

    public CursoCatalogoAdapter(CursoRepository cursos) {
        this.cursos = cursos;
    }

    @Override
    public Optional<CursoInfo> buscar(Long id) {
        return cursos.findById(id).map(this::paraInfo);
    }

    @Override
    public List<CursoInfo> listar() {
        return cursos.findAll().stream().map(this::paraInfo).toList();
    }

    @Override
    public CursoInfo salvar(String nome, String tipo, String descricao) {
        return paraInfo(cursos.save(new Curso(nome, tipo, descricao)));
    }

    private CursoInfo paraInfo(Curso c) {
        return new CursoInfo(c.getId(), c.getNome(), c.getTipo(), c.getDescricao());
    }
}
