package org.example.skillup.ai.adapter.in.web;

import org.example.skillup.ai.application.RecomendarCursosService;
import org.example.skillup.ai.application.port.out.LlmPort;
import org.example.skillup.ai.domain.Modelos.ResultadoRecomendacao;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** RF03 - POST /api/ai/recomendacoes (via gateway). */
@RestController
public class RecomendacaoController {

    public record PedidoRecomendacao(Long colaboradorId, String interesses) {}

    private final RecomendarCursosService service;
    private final LlmPort llm;

    public RecomendacaoController(RecomendarCursosService service, LlmPort llm) {
        this.service = service;
        this.llm = llm;
    }

    @PostMapping("/recomendacoes")
    public ResultadoRecomendacao recomendar(@RequestBody PedidoRecomendacao pedido) {
        return service.recomendar(pedido.colaboradorId(), pedido.interesses());
    }

    /** Mostra no front se a IA esta usando OpenAI ou mock. */
    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("modo", llm.modo());
    }
}
