package org.example.skillup.ai.adapter.out.ia;

import org.example.skillup.ai.domain.Modelos.TrechoDocumento;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Carrega os documentos institucionais (src/main/resources/docs/*.md) e quebra
 * cada um em trechos por secao "## ". Esses trechos sao a base de conhecimento do RAG.
 */
@Component
public class DocumentosInstitucionais {

    private final List<TrechoDocumento> trechos;

    public DocumentosInstitucionais() {
        this.trechos = carregar();
    }

    public List<TrechoDocumento> trechos() {
        return trechos;
    }

    private static List<TrechoDocumento> carregar() {
        List<TrechoDocumento> lista = new ArrayList<>();
        try {
            Resource[] arquivos = new PathMatchingResourcePatternResolver().getResources("classpath*:docs/*.md");
            for (Resource arquivo : arquivos) {
                String fonte = arquivo.getFilename();
                String conteudo = new String(arquivo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                for (String secao : conteudo.split("\\n(?=## )")) {
                    String limpo = secao.strip();
                    if (!limpo.startsWith("## ")) {
                        continue; // ignora o titulo "# ..." do arquivo
                    }
                    int quebra = limpo.indexOf('\n');
                    String titulo = (quebra > 0 ? limpo.substring(3, quebra) : limpo.substring(3)).strip();
                    String texto = quebra > 0 ? limpo.substring(quebra + 1).strip() : "";
                    lista.add(new TrechoDocumento(fonte, titulo, texto));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return List.copyOf(lista);
    }
}
