package org.example.ac1_atdd.colaboradores.adapter.out.persistence;

import org.example.ac1_atdd.colaboradores.application.port.out.ColaboradorRepositoryPort;
import org.example.ac1_atdd.colaboradores.domain.Colaborador;
import org.example.ac1_atdd.colaboradores.domain.NivelCompetencia;
import org.example.ac1_atdd.colaboradores.domain.Registros.Acompanhamento;
import org.example.ac1_atdd.colaboradores.domain.Registros.Matricula;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adapter de SAIDA: implementa a porta usando JPA + Postgres e converte entidade <-> dominio. */
@Component
public class ColaboradorPersistenceAdapter implements ColaboradorRepositoryPort {

    private final ColaboradorJpa colaboradores;
    private final MatriculaJpa matriculas;
    private final AcompanhamentoJpa acompanhamentos;

    public ColaboradorPersistenceAdapter(ColaboradorJpa colaboradores, MatriculaJpa matriculas,
                                         AcompanhamentoJpa acompanhamentos) {
        this.colaboradores = colaboradores;
        this.matriculas = matriculas;
        this.acompanhamentos = acompanhamentos;
    }

    @Override
    public Colaborador salvar(Colaborador c) {
        ColaboradorEntity e = c.getId() == null
                ? new ColaboradorEntity()
                : colaboradores.findById(c.getId()).orElseGet(ColaboradorEntity::new);
        e.setNome(c.getNome());
        e.setEmail(c.getEmail());
        e.setCargo(c.getCargo());
        e.setArea(c.getArea());
        e.setNivel(c.getNivel().name());
        return paraDominio(colaboradores.save(e));
    }

    @Override
    public Optional<Colaborador> buscarPorId(Long id) {
        return colaboradores.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Colaborador> listarTodos() {
        return colaboradores.findAllByOrderByIdAsc().stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existeEmail(String email) {
        return colaboradores.existsByEmail(email);
    }

    @Override
    public Matricula salvarMatricula(Matricula m) {
        MatriculaEntity e = new MatriculaEntity();
        e.setColaboradorId(m.colaboradorId());
        e.setCursoId(m.cursoId());
        e.setCursoNome(m.cursoNome());
        e.setOrigem(m.origem());
        e.setData(m.data());
        MatriculaEntity salvo = matriculas.save(e);
        return new Matricula(salvo.getId(), salvo.getColaboradorId(), salvo.getCursoId(),
                salvo.getCursoNome(), salvo.getOrigem(), salvo.getData());
    }

    @Override
    public List<Matricula> matriculasDo(Long colaboradorId) {
        return matriculas.findByColaboradorIdOrderByIdDesc(colaboradorId).stream()
                .map(e -> new Matricula(e.getId(), e.getColaboradorId(), e.getCursoId(),
                        e.getCursoNome(), e.getOrigem(), e.getData()))
                .toList();
    }

    @Override
    public boolean jaMatriculado(Long colaboradorId, Long cursoId) {
        return matriculas.existsByColaboradorIdAndCursoId(colaboradorId, cursoId);
    }

    @Override
    public Acompanhamento salvarAcompanhamento(Acompanhamento a) {
        AcompanhamentoEntity e = new AcompanhamentoEntity();
        e.setColaboradorId(a.colaboradorId());
        e.setDataPrevista(a.dataPrevista());
        e.setDescricao(a.descricao());
        e.setOrigem(a.origem());
        AcompanhamentoEntity salvo = acompanhamentos.save(e);
        return new Acompanhamento(salvo.getId(), salvo.getColaboradorId(), salvo.getDataPrevista(),
                salvo.getDescricao(), salvo.getOrigem());
    }

    @Override
    public List<Acompanhamento> acompanhamentosDo(Long colaboradorId) {
        return acompanhamentos.findByColaboradorIdOrderByDataPrevistaAsc(colaboradorId).stream()
                .map(e -> new Acompanhamento(e.getId(), e.getColaboradorId(), e.getDataPrevista(),
                        e.getDescricao(), e.getOrigem()))
                .toList();
    }

    private Colaborador paraDominio(ColaboradorEntity e) {
        return new Colaborador(e.getId(), e.getNome(), e.getEmail(), e.getCargo(), e.getArea(),
                NivelCompetencia.valueOf(e.getNivel()));
    }
}
