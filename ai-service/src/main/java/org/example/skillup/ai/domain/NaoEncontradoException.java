package org.example.skillup.ai.domain;

public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
