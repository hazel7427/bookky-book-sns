package com.sns.project.dto.chat.response;

import java.time.LocalDateTime;

import com.sns.project.domain.chat.ChatMessage;

import lombok.Data;

@Data
public class ChatMessageResponse {
    private Long id;
    private String message;
    private LocalDateTime sentAt;

    public ChatMessageResponse(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
    }
}
