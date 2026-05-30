package com.jy.uap.uapreleasefileschatbot.service;

import com.jy.uap.uapreleasefileschatbot.dto.UiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(@Qualifier("openAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Sends the user's question through RAG-backed {@link ChatClient}. The configured
     * {@code QuestionAnswerAdvisor} embeds the question, retrieves similar document
     * chunks from pgvector, and grounds the model response on that context.
     */
    public UiChatResponse chat(String conversationId, String question) {
        log.info("Processing chat question for conversation {}", conversationId);

        return chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .call()
                .entity(UiChatResponse.class);
    }

}
