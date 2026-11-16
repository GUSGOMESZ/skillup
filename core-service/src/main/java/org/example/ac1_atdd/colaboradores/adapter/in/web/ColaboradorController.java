package org.example.ac1_atdd.colaboradores.adapter.in.web;

import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.AcompanhamentoRequest;
import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.AcompanhamentoResponse;
import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.ColaboradorRequest;
import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.ColaboradorResponse;
import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.MatriculaRequest;
import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.MatriculaResponse;
import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.NivelRequest;
import org.example.ac1_atdd.colaboradores.application.port.in.GerenciarColaboradoresUseCase;
import org.example.ac1_atdd.colaboradores.domain.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Adapter de ENTRADA (REST). Chamado pelo front via gateway: /api/core/colaboradores
 * O controller so traduz HTTP <-> caso de uso; a regra fica no ColaboradorService.
 */
@RestController
@RequestMapping("/colaboradores")
public class ColaboradorController {

    private final GerenciarColaboradoresUseCase useCase;

    public ColaboradorController(GerenciarColaboradoresUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<ColaboradorResponse> listar() {
        return useCase.listar().stream().map(ColaboradorResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ColaboradorResponse buscar(@PathVariable Long id) {
        return ColaboradorResponse.de(useCase.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ColaboradorResponse cadastrar(@RequestBody ColaboradorRequest req) {
        return ColaboradorResponse.de(useCase.cadastrar(req.nome(), req.email(), req.cargo(), req.area()));
    }

    /** RF02: o front chama o ML e grava aqui o nivel retornado. */
    @PutMapping("/{id}/nivel")
    public ColaboradorResponse classificar(@PathVariable Long id, @RequestBody NivelRequest req) {
        return ColaboradorResponse.de(useCase.classificar(id, req.nivel()));
    }

    @GetMapping("/{id}/matriculas")
    public List<MatriculaResponse> matriculas(@PathVariable Long id) {
        return useCase.matriculas(id).stream().map(MatriculaResponse::de).toList();
    }

    /** Usado pelo front (origem MANUAL) e pelo agente de IA depois da aprovacao (origem AGENTE). */
    @PostMapping("/{id}/matriculas")
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse matricular(@PathVariable Long id, @RequestBody MatriculaRequest req) {
        return MatriculaResponse.de(useCase.matricular(id, req.cursoId(), req.origem()));
    }

    @GetMapping("/{id}/acompanhamentos")
    public List<AcompanhamentoResponse> acompanhamentos(@PathVariable Long id) {
        return useCase.acompanhamentos(id).stream().map(AcompanhamentoResponse::de).toList();
    }

    @PostMapping("/{id}/acompanhamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public AcompanhamentoResponse agendar(@PathVariable Long id, @RequestBody AcompanhamentoRequest req) {
        LocalDate data;
        try {
            data = LocalDate.parse(req.dataPrevista());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new RegraDeNegocioException("dataPrevista deve estar no formato AAAA-MM-DD");
        }
        return AcompanhamentoResponse.de(useCase.agendarAcompanhamento(id, data, req.descricao(), req.origem()));
    }
}
