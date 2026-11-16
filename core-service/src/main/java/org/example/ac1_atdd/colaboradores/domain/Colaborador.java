package org.example.ac1_atdd.colaboradores.domain;

/**
 * Entidade de dominio (Java puro, sem JPA nem Spring).
 * Na arquitetura hexagonal o dominio nao conhece banco nem framework.
 */
public class Colaborador {

    private final Long id;
    private final String nome;
    private final String email;
    private final String cargo;
    private final String area;
    private NivelCompetencia nivel;

    public Colaborador(Long id, String nome, String email, String cargo, String area, NivelCompetencia nivel) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.area = area;
        this.nivel = nivel == null ? NivelCompetencia.NAO_CLASSIFICADO : nivel;
    }

    /** Fabrica para um colaborador novo, com as validacoes de cadastro. */
    public static Colaborador novo(String nome, String email, String cargo, String area) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome e obrigatorio");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new RegraDeNegocioException("E-mail invalido");
        }
        return new Colaborador(null, nome.trim(), email.trim().toLowerCase(),
                cargo == null ? "" : cargo.trim(), area == null ? "" : area.trim(),
                NivelCompetencia.NAO_CLASSIFICADO);
    }

    public void classificar(NivelCompetencia novoNivel) {
        if (novoNivel == null || novoNivel == NivelCompetencia.NAO_CLASSIFICADO) {
            throw new RegraDeNegocioException("Classificacao precisa ser INICIANTE, INTERMEDIARIO ou AVANCADO");
        }
        this.nivel = novoNivel;
    }

    /** RNF03: dado pessoal mascarado nas listagens. joao.silva@empresa.com -> j***@empresa.com */
    public String emailMascarado() {
        int arroba = email.indexOf('@');
        if (arroba <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(arroba);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCargo() { return cargo; }
    public String getArea() { return area; }
    public NivelCompetencia getNivel() { return nivel; }
}
