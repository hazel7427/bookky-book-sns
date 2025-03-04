package com.sns.project.service.chat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import com.sns.project.dto.chat.request.ChatMessageRequest;
import com.sns.project.dto.chat.response.ChatMessageResponse;
import com.sns.project.repository.chat.ChatMessageRepository;
import com.sns.project.repository.chat.ChatRoomRepository;
import com.sns.project.service.RedisService;
import com.sns.project.service.user.UserService;
import com.sns.project.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final RedisService redisService;


    public ChatMessageResponse saveMessage(Long senderId, String message, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        User sender = userService.getUserById(senderId);

        // 나중에 배치로 처리 필요
        ChatMessage chatMessage = new ChatMessage(chatRoom, sender, message);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        System.out.println("message is saved");

        String roomUsersKey = RedisKeys.Chat.CHAT_ROOM_USERS_KEY.get() + roomId;

        Set<Long> recipients = redisService.getValuesFromSet(roomUsersKey, Long.class);

        if (recipients == null) {
            throw new RuntimeException("Recipients not found");
        }

        String presenceKey = RedisKeys.Chat.CHAT_PRESENCE_KEY.get() + roomId;
        Set<Long> presence = redisService.getValuesFromSet(presenceKey, Long.class);
        int unreadCountOfMessage = recipients.size() - presence.size();
        log.error("unreadCountOfMessage: {}", unreadCountOfMessage);
        
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.get() + chatMessage.getId();
        redisService.setValueWithExpiration(unreadCountKey, unreadCountOfMessage, 86400L); 
        return new ChatMessageResponse(savedMessage, unreadCountOfMessage);
    }

    public List<ChatMessageResponse> getChatHistory(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId);
        return messages.stream().map(msg -> {
            String unreadKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.get() + msg.getId();
            int unreadCount = redisService.getValue(unreadKey, Integer.class).orElse(0);
            return new ChatMessageResponse(msg, unreadCount);
        }).collect(Collectors.toList());
    }




}
