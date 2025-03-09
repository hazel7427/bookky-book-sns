package com.sns.project.controller.chat.dto.response;

import java.util.List;

import lombok.Getter;


@Getter
public class ChatHistoryResponse {  
    private List<ChatMessageResponse> chatHistory;

    public ChatHistoryResponse(List<ChatMessageResponse> chatHistory) {
        this.chatHistory = chatHistory;
    }
}
