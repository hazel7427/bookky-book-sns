package com.sns.project.controller.chat;

import com.sns.project.handler.exceptionHandler.exception.unauthorized.UnauthorizedException;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.sns.project.dto.chat.request.ChatMessageRequest;
import com.sns.project.dto.chat.response.ChatMessageResponse;
import com.sns.project.service.chat.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable Long roomId,
        @Payload ChatMessageRequest chatMessageRequest, 
        StompHeaderAccessor headerAccessor) {
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (senderId == null) {
            throw new UnauthorizedException("User not authenticated in WebSocket session");
        }

        ChatMessageResponse response = chatService.saveMessage(senderId, chatMessageRequest.getMessage(), roomId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }
    
}
