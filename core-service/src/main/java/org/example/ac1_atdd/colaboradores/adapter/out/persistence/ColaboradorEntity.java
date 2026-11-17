package org.example.ac1_atdd.colaboradores.adapter.out.persistence;

import jakarta.persistence.*;

/** Tabela "colaboradores". Existe so no adapter: o dominio nao conhece JPA. */
@Entity
@Table(name = "colaboradores")
public class ColaboradorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    private String cargo;

    private String area;

    @Column(nullable = false)
    private String nivel;

    public ColaboradorEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
}
