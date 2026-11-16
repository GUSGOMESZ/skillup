package org.example.ac1_atdd.colaboradores.domain;

/** Violacao de regra de negocio (vira HTTP 400 no adapter web). */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
