package org.example.ac1_atdd.colaboradores.application;

import org.example.ac1_atdd.colaboradores.application.port.in.GerenciarColaboradoresUseCase;
import org.example.ac1_atdd.colaboradores.application.port.in.GerenciarCursosUseCase;
import org.example.ac1_atdd.colaboradores.application.port.out.ColaboradorRepositoryPort;
import org.example.ac1_atdd.colaboradores.application.port.out.CursoCatalogoPort;
import org.example.ac1_atdd.colaboradores.domain.Colaborador;
import org.example.ac1_atdd.colaboradores.domain.NaoEncontradoException;
import org.example.ac1_atdd.colaboradores.domain.NivelCompetencia;
import org.example.ac1_atdd.colaboradores.domain.RegraDeNegocioException;
import org.example.ac1_atdd.colaboradores.domain.Registros.Acompanhamento;
import org.example.ac1_atdd.colaboradores.domain.Registros.CursoInfo;
import org.example.ac1_atdd.colaboradores.domain.Registros.Matricula;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso (camada de aplicacao). Implementa as portas de entrada e
 * usa apenas as portas de saida, nunca o JPA diretamente.
 */
@Service
public class ColaboradorService implements GerenciarColaboradoresUseCase, GerenciarCursosUseCase {

    private final ColaboradorRepositoryPort repositorio;
    private final CursoCatalogoPort catalogo;

    public ColaboradorService(ColaboradorRepositoryPort repositorio, CursoCatalogoPort catalogo) {
        this.repositorio = repositorio;
        this.catalogo = catalogo;
    }

    @Override
    public Colaborador cadastrar(String nome, String email, String cargo, String area) {
        Colaborador novo = Colaborador.novo(nome, email, cargo, area);
        if (repositorio.existeEmail(novo.getEmail())) {
            throw new RegraDeNegocioException("Ja existe colaborador com este e-mail");
        }
        return repositorio.salvar(novo);
    }

    @Override
    public List<Colaborador> listar() {
        return repositorio.listarTodos();
    }

    @Override
    public Colaborador buscar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new NaoEncontradoException("Colaborador " + id + " nao encontrado"));
    }

    @Override
    public Colaborador classificar(Long id, String nivel) {
        Colaborador colaborador = buscar(id);
        colaborador.classificar(NivelCompetencia.de(nivel));
        return repositorio.salvar(colaborador);
    }

    @Override
    public Matricula matricular(Long colaboradorId, Long cursoId, String origem) {
        buscar(colaboradorId);
        CursoInfo curso = catalogo.buscar(cursoId)
                .orElseThrow(() -> new NaoEncontradoException("Curso " + cursoId + " nao encontrado"));
        if (repositorio.jaMatriculado(colaboradorId, cursoId)) {
            throw new RegraDeNegocioException("Colaborador ja esta matriculado em " + curso.nome());
        }
        String origemFinal = (origem == null || origem.isBlank()) ? "MANUAL" : origem.toUpperCase();
        return repositorio.salvarMatricula(
                new Matricula(null, colaboradorId, cursoId, curso.nome(), origemFinal, LocalDate.now()));
    }

    @Override
    public List<Matricula> matriculas(Long colaboradorId) {
        buscar(colaboradorId);
        return repositorio.matriculasDo(colaboradorId);
    }

    @Override
    public Acompanhamento agendarAcompanhamento(Long colaboradorId, LocalDate dataPrevista,
                                                String descricao, String origem) {
        buscar(colaboradorId);
        if (dataPrevista == null || dataPrevista.isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("Data do acompanhamento precisa ser hoje ou no futuro");
        }
        String origemFinal = (origem == null || origem.isBlank()) ? "MANUAL" : origem.toUpperCase();
        return repositorio.salvarAcompanhamento(
                new Acompanhamento(null, colaboradorId, dataPrevista, descricao, origemFinal));
    }

    @Override
    public List<Acompanhamento> acompanhamentos(Long colaboradorId) {
        buscar(colaboradorId);
        return repositorio.acompanhamentosDo(colaboradorId);
    }

    @Override
    public CursoInfo cadastrarCurso(String nome, String tipo, String descricao) {
        if (nome == null || nome.isBlank() || tipo == null || tipo.isBlank()) {
            throw new RegraDeNegocioException("Nome e tipo do curso sao obrigatorios");
        }
        return catalogo.salvar(nome.trim(), tipo.trim().toUpperCase(), descricao);
    }

    @Override
    public List<CursoInfo> listarCursos() {
        return catalogo.listar();
    }
}
