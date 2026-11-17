package org.example.skillup.ai.adapter.out.ia;

import org.example.skillup.ai.application.port.out.BuscaDocumentosPort;
import org.example.skillup.ai.domain.Modelos.TrechoDocumento;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Recuperacao SEM IA (modo mock): ranqueia trechos pelas palavras em comum com a consulta. */
public class BuscaPorPalavraChaveAdapter implements BuscaDocumentosPort {

    private final DocumentosInstitucionais documentos;

    public BuscaPorPalavraChaveAdapter(DocumentosInstitucionais documentos) {
        this.documentos = documentos;
    }

    @Override
    public List<TrechoDocumento> buscar(String consulta, int quantidade) {
        Set<String> termos = Texto.palavras(consulta);
        return documentos.trechos().stream()
                .sorted(Comparator.comparingLong((TrechoDocumento t) -> pontuar(t, termos)).reversed())
                .limit(quantidade)
                .toList();
    }

    private long pontuar(TrechoDocumento trecho, Set<String> termos) {
        Set<String> doTrecho = Texto.palavras(trecho.titulo() + " " + trecho.texto());
        return termos.stream().filter(doTrecho::contains).count();
    }
}
