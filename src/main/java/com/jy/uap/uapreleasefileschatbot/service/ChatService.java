package com.jy.uap.uapreleasefileschatbot.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.jy.uap.uapreleasefileschatbot.dto.UiChatResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatService(
            @Qualifier("openAiChatClient") ChatClient chatClient,
            ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    public UiChatResponse chat(String conversationId, String question) {
        log.info("Processing chat question for conversation {}", conversationId);

        Map<String, Object> promptParams = Map.of(
                "currentDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        UiChatResponse response = chatClient.prompt()
                .messages(safeConversationHistory(conversationId))
                .system(system -> system.params(promptParams))
                .user(question)
                .call()
                .entity(UiChatResponse.class);

        chatMemory.add(conversationId, new UserMessage(question));
        chatMemory.add(conversationId, new AssistantMessage(response.getMessage()));

        return response;
    }

    private List<Message> safeConversationHistory(String conversationId) {
        return chatMemory.get(conversationId).stream()
                .filter(message -> message instanceof UserMessage
                        || (message instanceof AssistantMessage assistant && !assistant.hasToolCalls()))
                .toList();
    }

}
