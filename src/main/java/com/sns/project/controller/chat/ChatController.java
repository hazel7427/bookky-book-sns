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

    @MessageMapping("/sendMessage")
    public void sendMessage(
        @Payload ChatMessageRequest chatMessageRequest, 
        StompHeaderAccessor headerAccessor) {
        System.out.println("✅ [DEBUG] WebSocket 메시지 수신: " + chatMessageRequest.getMessage());

        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (senderId == null) {
            throw new UnauthorizedException("User not authenticated in WebSocket session");
        }

        Long roomId = chatMessageRequest.getRoomId();
        ChatMessageResponse response = chatService.saveMessage(senderId, chatMessageRequest.getMessage(), roomId);
        System.out.println(response);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }
    
}
