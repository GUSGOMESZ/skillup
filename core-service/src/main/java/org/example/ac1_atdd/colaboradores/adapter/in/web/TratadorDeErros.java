package org.example.ac1_atdd.colaboradores.adapter.in.web;

import org.example.ac1_atdd.colaboradores.adapter.in.web.WebDtos.Erro;
import org.example.ac1_atdd.colaboradores.domain.NaoEncontradoException;
import org.example.ac1_atdd.colaboradores.domain.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte excecoes do dominio em respostas HTTP com mensagem legivel. */
@RestControllerAdvice(basePackages = "org.example.ac1_atdd.colaboradores")
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Erro> regra(RegraDeNegocioException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Erro(e.getMessage()));
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(NaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Erro(e.getMessage()));
    }
}
