package com.sns.project.service.chat;

import org.springframework.stereotype.Service;

import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import com.sns.project.dto.chat.request.ChatMessageRequest;
import com.sns.project.dto.chat.response.ChatMessageResponse;
import com.sns.project.repository.chat.ChatMessageRepository;
import com.sns.project.repository.chat.ChatRoomRepository;
import com.sns.project.service.user.UserService;
import com.sns.project.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;

    public ChatMessageResponse saveMessage(Long senderId, String message, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        User sender = userService.getUserById(senderId);

        ChatMessage chatMessage = new ChatMessage(chatRoom, sender, message);
        chatMessageRepository.save(chatMessage);

        return new ChatMessageResponse(chatMessage);
    }
}
