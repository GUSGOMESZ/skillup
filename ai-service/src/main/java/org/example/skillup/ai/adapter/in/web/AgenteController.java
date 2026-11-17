package org.example.skillup.ai.adapter.in.web;

import org.example.skillup.ai.application.AgenteService;
import org.example.skillup.ai.domain.AcaoProposta;
import org.example.skillup.ai.domain.Agente;
import org.example.skillup.ai.domain.Ferramenta;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** RF04 - agentes que propoem acoes; o usuario aprova ou rejeita. */
@RestController
@RequestMapping("/agentes")
public class AgenteController {

    public record PedidoPlano(Long colaboradorId, String interesses) {}

    public record AgenteInfo(String nome, String descricao, List<Ferramenta> ferramentasPermitidas) {}

    private final AgenteService service;

    public AgenteController(AgenteService service) {
        this.service = service;
    }

    @GetMapping
    public List<AgenteInfo> agentes() {
        return Arrays.stream(Agente.values())
                .map(a -> new AgenteInfo(a.name(), a.getDescricao(), List.copyOf(a.getFerramentasPermitidas())))
                .toList();
    }

    /** O agente analisa o colaborador e propoe acoes (ficam PENDENTES). */
    @PostMapping("/{agente}/planejar")
    public List<AcaoProposta> planejar(@PathVariable Agente agente, @RequestBody PedidoPlano pedido) {
        return service.planejar(agente, pedido.colaboradorId(), pedido.interesses());
    }

    /** Demo do RNF03: forca um agente a pedir uma ferramenta. Fora da permissao -> 403. */
    @PostMapping("/{agente}/ferramentas/{ferramenta}")
    public AcaoProposta usarFerramenta(@PathVariable Agente agente, @PathVariable Ferramenta ferramenta,
                                       @RequestParam Long colaboradorId) {
        return service.propor(agente, ferramenta, colaboradorId,
                "Teste de permissao: " + agente + " pedindo " + ferramenta, Map.of());
    }

    @GetMapping("/acoes")
    public List<AcaoProposta> acoes(@RequestParam(required = false) Long colaboradorId) {
        return service.listar(colaboradorId);
    }

    @PostMapping("/acoes/{id}/aprovar")
    public AcaoProposta aprovar(@PathVariable Long id) {
        return service.aprovar(id);
    }

    @PostMapping("/acoes/{id}/rejeitar")
    public AcaoProposta rejeitar(@PathVariable Long id) {
        return service.rejeitar(id);
    }
}
