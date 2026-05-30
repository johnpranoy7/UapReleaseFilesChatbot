package com.jy.uap.uapreleasefileschatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ChatbotConfig {

    private static final String SYSTEM_PROMPT = """
            You are a chatbot answering questions about UFOs and UAPs using the retrieved document context.
            Use the retrieved context as your primary source. If the context contains relevant information,
            answer the question even when the text is partial or imperfect. Only when the retrieved context
            has no relevant information at all, respond with:
            message: "I do not know. You are not getting any pulsating answers here.", confidence: 0.0, source: ""

            Respond with JSON only using exactly these fields:
            - message: your answer to the user
            - confidence: a number from 0.0 to 1.0 indicating how confident you are
            - source: comma-separated filenames from the retrieved context (use the fileName metadata field)
            """;

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel chatModel, PgVectorStore vectorStore, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(10)
                                        .similarityThreshold(0.0)
                                        .build())
                                .build(),
                        new SimpleLoggerAdvisor())
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {

        JdbcChatMemoryRepository repository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new PostgresChatMemoryRepositoryDialect())  // PostgreSQL specific
                .build();

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(8)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
