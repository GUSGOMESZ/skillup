package org.example.skillup.ai.adapter.out.ia;

import org.example.skillup.ai.application.port.out.BuscaDocumentosPort;
import org.example.skillup.ai.domain.Modelos.TrechoDocumento;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.Comparator;
import java.util.List;

/**
 * Recuperacao SEMANTICA (modo OpenAI): cada trecho vira um vetor (embedding) e a consulta
 * tambem; os trechos mais proximos pela similaridade de cosseno entram no prompt.
 * Os vetores ficam em memoria (vector store simples). Se a OpenAI falhar, cai na busca por palavra-chave.
 */
public class BuscaVetorialAdapter implements BuscaDocumentosPort {

    private final EmbeddingModel embeddings;
    private final DocumentosInstitucionais documentos;
    private final BuscaPorPalavraChaveAdapter reserva;
    private volatile List<float[]> vetores;

    public BuscaVetorialAdapter(EmbeddingModel embeddings, DocumentosInstitucionais documentos) {
        this.embeddings = embeddings;
        this.documentos = documentos;
        this.reserva = new BuscaPorPalavraChaveAdapter(documentos);
    }

    @Override
    public List<TrechoDocumento> buscar(String consulta, int quantidade) {
        try {
            List<TrechoDocumento> trechos = documentos.trechos();
            List<float[]> base = vetoresDosTrechos(trechos);
            float[] alvo = embeddings.embed(consulta);
            return java.util.stream.IntStream.range(0, trechos.size()).boxed()
                    .sorted(Comparator.comparingDouble((Integer i) -> cosseno(alvo, base.get(i))).reversed())
                    .limit(quantidade)
                    .map(trechos::get)
                    .toList();
        } catch (RuntimeException erro) {
            System.out.println("Busca vetorial indisponivel (" + erro.getMessage() + "), usando palavra-chave");
            return reserva.buscar(consulta, quantidade);
        }
    }

    /** Calcula os embeddings dos documentos uma unica vez (na primeira consulta). */
    private synchronized List<float[]> vetoresDosTrechos(List<TrechoDocumento> trechos) {
        if (vetores == null) {
            vetores = embeddings.embed(trechos.stream().map(t -> t.titulo() + "\n" + t.texto()).toList());
        }
        return vetores;
    }

    private static double cosseno(float[] a, float[] b) {
        double produto = 0, normaA = 0, normaB = 0;
        for (int i = 0; i < a.length; i++) {
            produto += a[i] * b[i];
            normaA += a[i] * a[i];
            normaB += b[i] * b[i];
        }
        return produto / (Math.sqrt(normaA) * Math.sqrt(normaB) + 1e-9);
    }
}
