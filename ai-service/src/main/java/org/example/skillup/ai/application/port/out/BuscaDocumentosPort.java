package org.example.skillup.ai.application.port.out;

import org.example.skillup.ai.domain.Modelos.TrechoDocumento;

import java.util.List;

/** Porta de SAIDA para a recuperacao (o "R" do RAG). */
public interface BuscaDocumentosPort {

    List<TrechoDocumento> buscar(String consulta, int quantidade);
}
