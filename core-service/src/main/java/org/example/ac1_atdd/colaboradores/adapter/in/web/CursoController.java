package org.example.ac1_atdd.colaboradores.adapter.in.web;

import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.CursoRequest;
import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.CursoResponse;
import org.example.ac1_atdd.colaboradores.application.port.in.GerenciarCursosUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adapter de ENTRADA para o catalogo de cursos: /api/core/cursos (RF01). */
@RestController
@RequestMapping("/cursos")
public class CursoController {

    private final GerenciarCursosUseCase useCase;

    public CursoController(GerenciarCursosUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<CursoResponse> listar() {
        return useCase.listarCursos().stream().map(CursoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CursoResponse cadastrar(@RequestBody CursoRequest req) {
        return CursoResponse.de(useCase.cadastrarCurso(req.nome(), req.tipo(), req.descricao()));
    }
}
