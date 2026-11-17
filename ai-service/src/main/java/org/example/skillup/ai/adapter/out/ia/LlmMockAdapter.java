package org.example.skillup.ai.adapter.out.ia;

import org.example.skillup.ai.application.port.out.LlmPort;
import org.example.skillup.ai.domain.Modelos.CursoCatalogo;
import org.example.skillup.ai.domain.Modelos.CursoRecomendado;
import org.example.skillup.ai.domain.Modelos.PerfilColaborador;
import org.example.skillup.ai.domain.Modelos.TrechoDocumento;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LLM simulado (sem OpenAI). Usa regras simples: palavras em comum entre
 * interesses + trechos recuperados e a descricao de cada curso, com bonus pelo nivel.
 * Permite rodar o sistema inteiro sem chave e sem custo.
 */
public class LlmMockAdapter implements LlmPort {

    private static final Map<String, String> NIVEL_NO_TEXTO = Map.of(
            "INICIANTE", "iniciante", "INTERMEDIARIO", "intermediario", "AVANCADO", "avancado");

    @Override
    public List<CursoRecomendado> recomendar(PerfilColaborador perfil, String interesses,
                                             List<TrechoDocumento> contexto, List<CursoCatalogo> catalogo) {
        Set<String> interesse = Texto.palavras(interesses);
        Set<String> doContexto = new HashSet<>();
        contexto.forEach(t -> doContexto.addAll(Texto.palavras(t.titulo() + " " + t.texto())));
        String nivel = NIVEL_NO_TEXTO.getOrDefault(perfil.nivel(), "iniciante");

        return catalogo.stream()
                .map(curso -> avaliar(curso, interesse, doContexto, nivel))
                .filter(a -> a.pontos > 0)
                .sorted(Comparator.comparingInt((Avaliacao a) -> a.pontos).reversed())
                .limit(3)
                .map(a -> new CursoRecomendado(a.curso.id(), a.curso.nome(), a.motivo))
                .toList();
    }

    @Override
    public String modo() {
        return "mock";
    }

    private record Avaliacao(CursoCatalogo curso, int pontos, String motivo) {}

    private Avaliacao avaliar(CursoCatalogo curso, Set<String> interesse, Set<String> contexto, String nivel) {
        String textoCurso = Texto.normalizar(curso.nome() + " " + curso.tipo() + " " + curso.descricao());
        Set<String> palavrasCurso = Texto.palavras(textoCurso);

        Set<String> comInteresse = palavrasCurso.stream().filter(interesse::contains).collect(Collectors.toSet());
        long comContexto = palavrasCurso.stream().filter(contexto::contains).count();
        boolean nivelCombina = textoCurso.contains(nivel) || textoCurso.contains("qualquer nivel");

        int pontos = comInteresse.size() * 3 + (int) Math.min(comContexto, 4) + (nivelCombina ? 3 : 0);
        StringBuilder motivo = new StringBuilder();
        if (!comInteresse.isEmpty()) {
            motivo.append("Combina com seu interesse em ").append(String.join(", ", comInteresse)).append(". ");
        }
        if (nivelCombina) {
            motivo.append("Adequado ao nivel ").append(nivel).append(" segundo a politica de trilhas.");
        } else {
            motivo.append("Citado nos documentos de capacitacao recuperados.");
        }
        return new Avaliacao(curso, pontos, motivo.toString().trim());
    }
}
