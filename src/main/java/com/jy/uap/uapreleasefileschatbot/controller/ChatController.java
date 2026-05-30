package com.jy.uap.uapreleasefileschatbot.controller;

import com.jy.uap.uapreleasefileschatbot.dto.UiChatRequest;
import com.jy.uap.uapreleasefileschatbot.dto.UiChatResponse;
import com.jy.uap.uapreleasefileschatbot.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public UiChatResponse chat(
            @RequestBody UiChatRequest chatRequest) {
        String chatId = chatRequest.getChatId();
        log.info("chatId: {}", chatId);
        if (chatId == null || chatId.equalsIgnoreCase("unknown") || chatId.isEmpty()) {
            chatId = UUID.randomUUID().toString();
        }
        return chatService.chat(chatId, chatRequest.getQuestion());
    }
}
