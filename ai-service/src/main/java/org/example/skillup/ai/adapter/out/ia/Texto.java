package org.example.skillup.ai.adapter.out.ia;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Utilitario para comparar textos sem acento e sem palavras vazias. */
final class Texto {

    private static final Set<String> VAZIAS = Set.of("para", "como", "com", "uma", "que", "dos", "das", "nos",
            "nas", "pelo", "pela", "sobre", "nivel", "colaborador", "cargo", "area", "interesses", "curso", "cursos");

    private Texto() {}

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }

    static Set<String> palavras(String texto) {
        return Arrays.stream(normalizar(texto).split("[^a-z0-9]+"))
                .filter(p -> p.length() > 2 && !VAZIAS.contains(p))
                .collect(Collectors.toSet());
    }
}
