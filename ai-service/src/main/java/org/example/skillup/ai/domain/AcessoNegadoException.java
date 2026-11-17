package org.example.skillup.ai.domain;

/** Agente tentou usar uma ferramenta que nao tem permissao (vira HTTP 403). */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
