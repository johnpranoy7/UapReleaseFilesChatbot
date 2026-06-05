package com.jy.uap.uapreleasefileschatbot.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.jy.uap.uapreleasefileschatbot.dto.UiChatResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(
            @Qualifier("openAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public UiChatResponse chat(String conversationId, String question) {
        log.info("Processing chat question for conversation {}", conversationId);

        Map<String, Object> promptParams = Map.of(
                "currentDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        UiChatResponse response = chatClient.prompt()
                .system(system -> system.params(promptParams))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .call()
                .entity(UiChatResponse.class);
        return response;
    }

}
