package com.sns.project.controller.chat;

import com.sns.project.handler.exceptionHandler.exception.unauthorized.UnauthorizedException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.sns.project.controller.chat.dto.request.ChatMessageRequest;
import com.sns.project.controller.chat.dto.response.ChatMessageResponse;
import com.sns.project.service.chat.ChatService;

import java.security.Principal;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessageRequest chatMessageRequest, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated in WebSocket session");
        }
        
        Long senderId = Long.parseLong(principal.getName());
        Long roomId = chatMessageRequest.getRoomId();
        ChatMessageResponse response = chatService.saveMessage(senderId, chatMessageRequest.getMessage(), roomId);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }
    
}
