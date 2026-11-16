package org.example.ac1_atdd.colaboradores.domain;

/** Recurso inexistente (vira HTTP 404 no adapter web). */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
