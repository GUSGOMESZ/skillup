package org.example.skillup.ai.adapter.out.ia;

import org.example.skillup.ai.application.port.out.LlmPort;
import org.example.skillup.ai.domain.Modelos.CursoCatalogo;
import org.example.skillup.ai.domain.Modelos.CursoRecomendado;
import org.example.skillup.ai.domain.Modelos.PerfilColaborador;
import org.example.skillup.ai.domain.Modelos.TrechoDocumento;
import org.springframework.ai.chat.model.ChatModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * LLM real via Spring AI (ChatModel da OpenAI) - a parte "G" do RAG.
 * O prompt recebe SO os trechos recuperados e o catalogo; IDs inventados sao descartados.
 * Se a OpenAI falhar (sem credito, sem internet), usa o mock para nao quebrar a tela.
 */
public class LlmOpenAiAdapter implements LlmPort {

    private static final Pattern LINHA = Pattern.compile("^\\D*(\\d+)\\s*\\|\\s*(.+)$");

    private final ChatModel chat;
    private final LlmMockAdapter reserva = new LlmMockAdapter();
    private volatile String modo = "openai";

    public LlmOpenAiAdapter(ChatModel chat) {
        this.chat = chat;
    }

    @Override
    public List<CursoRecomendado> recomendar(PerfilColaborador perfil, String interesses,
                                             List<TrechoDocumento> contexto, List<CursoCatalogo> catalogo) {
        String prompt = montarPrompt(perfil, interesses, contexto, catalogo);
        try {
            String resposta = chat.call(prompt);
            List<CursoRecomendado> recomendacoes = interpretar(resposta, catalogo);
            modo = "openai";
            return recomendacoes.isEmpty() ? reserva.recomendar(perfil, interesses, contexto, catalogo) : recomendacoes;
        } catch (RuntimeException erro) {
            System.out.println("OpenAI indisponivel: " + erro.getMessage());
            modo = "mock (falha na OpenAI)";
            return reserva.recomendar(perfil, interesses, contexto, catalogo);
        }
    }

    @Override
    public String modo() {
        return modo;
    }

    private String montarPrompt(PerfilColaborador perfil, String interesses,
                                List<TrechoDocumento> contexto, List<CursoCatalogo> catalogo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Voce e um orientador de educacao corporativa. Recomende ate 3 cursos para o colaborador. ")
          .append("Use SOMENTE as regras dos documentos institucionais e os cursos do catalogo abaixo. ")
          .append("Responda apenas com linhas no formato: ID|motivo curto em portugues (max 25 palavras).\n\n");
        sb.append("COLABORADOR: ").append(perfil.nome()).append(", cargo ").append(perfil.cargo())
          .append(", area ").append(perfil.area()).append(", nivel ").append(perfil.nivel()).append('\n');
        sb.append("INTERESSES: ").append(interesses.isBlank() ? "nao informado" : interesses).append("\n\n");
        sb.append("DOCUMENTOS INSTITUCIONAIS RECUPERADOS:\n");
        for (int i = 0; i < contexto.size(); i++) {
            TrechoDocumento t = contexto.get(i);
            sb.append('[').append(i + 1).append("] ").append(t.titulo()).append(": ").append(t.texto()).append('\n');
        }
        sb.append("\nCATALOGO:\n");
        for (CursoCatalogo c : catalogo) {
            sb.append(c.id()).append(" - ").append(c.nome()).append(" (").append(c.tipo()).append("): ")
              .append(c.descricao()).append('\n');
        }
        return sb.toString();
    }

    private List<CursoRecomendado> interpretar(String resposta, List<CursoCatalogo> catalogo) {
        Map<Long, CursoCatalogo> porId = catalogo.stream()
                .collect(Collectors.toMap(CursoCatalogo::id, Function.identity()));
        List<CursoRecomendado> lista = new ArrayList<>();
        if (resposta == null) {
            return lista;
        }
        for (String linha : resposta.split("\\R")) {
            Matcher m = LINHA.matcher(linha.strip());
            if (m.matches()) {
                CursoCatalogo curso = porId.get(Long.valueOf(m.group(1)));
                boolean repetido = lista.stream().anyMatch(r -> curso != null && r.cursoId().equals(curso.id()));
                if (curso != null && !repetido && lista.size() < 3) {
                    lista.add(new CursoRecomendado(curso.id(), curso.nome(), m.group(2).strip()));
                }
            }
        }
        return lista;
    }
}
