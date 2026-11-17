package org.example.ac1_atdd.colaboradores.domain;

import java.text.Normalizer;

/** Nivel de competencia do colaborador, definido pela classificacao de ML (RF02). */
public enum NivelCompetencia {
    NAO_CLASSIFICADO,
    INICIANTE,
    INTERMEDIARIO,
    AVANCADO;

    /** Aceita "intermediário", "Intermediario", "INTERMEDIARIO"... */
    public static NivelCompetencia de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("Nivel nao informado");
        }
        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replace(' ', '_');
        try {
            return NivelCompetencia.valueOf(normalizado);
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("Nivel invalido: " + valor);
        }
    }
}
