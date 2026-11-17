package org.example.skillup.ai.config;

import org.example.skillup.ai.adapter.out.ia.BuscaPorPalavraChaveAdapter;
import org.example.skillup.ai.adapter.out.ia.BuscaVetorialAdapter;
import org.example.skillup.ai.adapter.out.ia.DocumentosInstitucionais;
import org.example.skillup.ai.adapter.out.ia.LlmMockAdapter;
import org.example.skillup.ai.adapter.out.ia.LlmOpenAiAdapter;
import org.example.skillup.ai.application.port.out.BuscaDocumentosPort;
import org.example.skillup.ai.application.port.out.LlmPort;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Arquitetura hexagonal na pratica: aqui se escolhe QUAL adapter liga em cada porta.
 * Com OPENAI_API_KEY -> Spring AI (OpenAI). Sem chave -> adapters mock. O resto do codigo nao muda.
 */
@Configuration
public class IaConfig {

    private final boolean temChave;

    public IaConfig(@Value("${spring.ai.openai.api-key:sem-chave}") String chave) {
        this.temChave = chave != null && !chave.isBlank() && !chave.equals("sem-chave");
        System.out.println(temChave ? "IA: usando OpenAI via Spring AI" : "IA: sem OPENAI_API_KEY, usando modo mock");
    }

    @Bean
    public LlmPort llmPort(ChatModel chatModel) {
        return temChave ? new LlmOpenAiAdapter(chatModel) : new LlmMockAdapter();
    }

    @Bean
    public BuscaDocumentosPort buscaDocumentosPort(EmbeddingModel embeddingModel, DocumentosInstitucionais documentos) {
        return temChave ? new BuscaVetorialAdapter(embeddingModel, documentos) : new BuscaPorPalavraChaveAdapter(documentos);
    }
}
