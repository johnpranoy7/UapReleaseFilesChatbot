package com.jy.uap.uapreleasefileschatbot.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public class UiChatResponse {
    private String message;
    private float confidence;
    private String source;
}
