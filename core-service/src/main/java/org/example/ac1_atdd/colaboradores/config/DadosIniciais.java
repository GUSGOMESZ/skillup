package org.example.ac1_atdd.colaboradores.config;

import org.example.ac1_atdd.colaboradores.application.port.in.GerenciarColaboradoresUseCase;
import org.example.ac1_atdd.colaboradores.application.port.in.GerenciarCursosUseCase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Popula o banco na primeira subida para a demo ja comecar com dados. */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final GerenciarCursosUseCase cursos;
    private final GerenciarColaboradoresUseCase colaboradores;

    public DadosIniciais(GerenciarCursosUseCase cursos, GerenciarColaboradoresUseCase colaboradores) {
        this.cursos = cursos;
        this.colaboradores = colaboradores;
    }

    @Override
    public void run(String... args) {
        if (cursos.listarCursos().isEmpty()) {
            cursos.cadastrarCurso("Logica de Programacao com Java", "PROGRAMACAO",
                    "Curso introdutorio para iniciantes: variaveis, condicionais, lacos e primeiros programas em Java.");
            cursos.cadastrarCurso("SQL Essencial", "DADOS",
                    "Consultas SELECT, JOIN e agregacoes em bancos relacionais. Indicado para iniciantes.");
            cursos.cadastrarCurso("APIs REST com Spring Boot", "PROGRAMACAO",
                    "Construcao de APIs REST, validacao, tratamento de erros e documentacao. Nivel intermediario.");
            cursos.cadastrarCurso("Docker na Pratica", "DEVOPS",
                    "Imagens, containers, volumes e docker compose para ambientes de desenvolvimento. Nivel intermediario.");
            cursos.cadastrarCurso("Mensageria com RabbitMQ", "ARQUITETURA",
                    "Filas, exchanges, routing keys e comunicacao assincrona entre servicos. Nivel avancado.");
            cursos.cadastrarCurso("Arquitetura de Microsservicos", "ARQUITETURA",
                    "Gateway, decomposicao por dominio, comunicacao sincrona e assincrona. Nivel avancado.");
            cursos.cadastrarCurso("Introducao a Machine Learning com Python", "DADOS",
                    "Regressao logistica, treino, validacao e metricas com scikit-learn. Nivel intermediario.");
            cursos.cadastrarCurso("Comunicacao e Lideranca", "COMPORTAMENTAL",
                    "Feedback, comunicacao com a equipe e conducao de reunioes. Indicado para qualquer nivel.");
        }
        if (colaboradores.listar().isEmpty()) {
            colaboradores.cadastrar("Ana Souza", "ana.souza@empresa.com", "Desenvolvedora Junior", "Tecnologia");
            colaboradores.cadastrar("Bruno Lima", "bruno.lima@empresa.com", "Analista de Dados", "Dados");
            colaboradores.cadastrar("Carla Mendes", "carla.mendes@empresa.com", "Tech Lead", "Tecnologia");
        }
    }
}
