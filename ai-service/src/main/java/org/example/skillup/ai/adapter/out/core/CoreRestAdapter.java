package org.example.skillup.ai.adapter.out.core;

import org.example.skillup.ai.application.port.out.CorePort;
import org.example.skillup.ai.domain.Modelos.CursoCatalogo;
import org.example.skillup.ai.domain.Modelos.PerfilColaborador;
import org.example.skillup.ai.domain.NaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** Adapter de SAIDA: fala com o core-service por REST (comunicacao sincrona). */
@Component
public class CoreRestAdapter implements CorePort {

    /** So os campos que o servico de IA precisa. O e-mail do core e ignorado (RNF03). */
    public record ColaboradorCore(Long id, String nome, String cargo, String area, String nivel) {}

    public record RespostaCriacao(Long id, String cursoNome, String dataPrevista) {}

    private final RestClient http;

    public CoreRestAdapter(RestClient.Builder builder, @Value("${skillup.core-url}") String coreUrl) {
        this.http = builder.baseUrl(coreUrl).build();
    }

    @Override
    public PerfilColaborador perfil(Long colaboradorId) {
        try {
            ColaboradorCore c = http.get().uri("/colaboradores/{id}", colaboradorId)
                    .retrieve().body(ColaboradorCore.class);
            return new PerfilColaborador(c.id(), c.nome(), c.cargo(), c.area(), c.nivel());
        } catch (HttpClientErrorException.NotFound e) {
            throw new NaoEncontradoException("Colaborador " + colaboradorId + " nao encontrado no core");
        }
    }

    @Override
    public List<CursoCatalogo> cursos() {
        return http.get().uri("/cursos").retrieve().body(new ParameterizedTypeReference<List<CursoCatalogo>>() {});
    }

    @Override
    public String matricular(Long colaboradorId, Long cursoId) {
        try {
            RespostaCriacao r = http.post().uri("/colaboradores/{id}/matriculas", colaboradorId)
                    .body(Map.of("cursoId", cursoId, "origem", "AGENTE"))
                    .retrieve().body(RespostaCriacao.class);
            return "Matricula " + r.id() + " criada em " + r.cursoNome();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Core recusou a matricula: " + e.getResponseBodyAsString());
        }
    }

    @Override
    public String agendarAcompanhamento(Long colaboradorId, String dataPrevista, String descricao) {
        try {
            RespostaCriacao r = http.post().uri("/colaboradores/{id}/acompanhamentos", colaboradorId)
                    .body(Map.of("dataPrevista", dataPrevista, "descricao", descricao, "origem", "AGENTE"))
                    .retrieve().body(RespostaCriacao.class);
            return "Acompanhamento " + r.id() + " agendado para " + r.dataPrevista();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Core recusou o agendamento: " + e.getResponseBodyAsString());
        }
    }
}
