package com.jy.uap.uapreleasefileschatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ChatbotConfig {

    private static final String SYSTEM_PROMPT = """
            Today is {currentDate}. Resolve ALL relative dates from this value only — never guess years from memory.
            "Last year" = calendar year before {currentDate}'s year. "May 10 last year" = May 10 of that year. "Today" → omit APOD date.

            UAP questions → searchUapReleaseDocuments.
            NASA pictures → getNasaApod (omit date for today, else pass resolved YYYY-MM-DD).
            No year given (March 2, Mar 2) → use {currentDate}'s year. Today → omit date. Pass date as written or YYYY-MM-DD.

            JSON only: message, confidence (0.0-1.0), imageUrl (from APOD or null).
            If tool result starts with "NASA APOD error:", tell the user that NASA APOD is having an issue (use the tool message). confidence 0.0, imageUrl null. Do not use the pulsating fallback.
            If nothing useful and no tool error: message "I do not know. You are not getting any pulsating answers here.", confidence 0.0, imageUrl null.
            """;

    @Bean
    public ToolCallAdvisor toolCallAdvisor() {
        return ToolCallAdvisor.builder()
                .advisorOrder(Ordered.HIGHEST_PRECEDENCE + 300)
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(
            OpenAiChatModel chatModel,
            AppTools appTools,
            ToolCallAdvisor toolCallAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultTools(appTools)
                .defaultAdvisors(toolCallAdvisor, new SimpleLoggerAdvisor())
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {

        JdbcChatMemoryRepository repository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new PostgresChatMemoryRepositoryDialect())
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
