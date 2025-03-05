package com.sns.project.service.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.domain.chat.ChatReadMessage;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import com.sns.project.handler.exceptionHandler.exception.notfound.ChatRoomNotFoundException;
import com.sns.project.controller.chat.dto.response.ChatMessageResponse;
import com.sns.project.repository.chat.ChatMessageRepository;
import com.sns.project.repository.chat.ChatRoomRepository;
import com.sns.project.repository.chat.ChatParticipantRepository;
import com.sns.project.service.RedisService;
import com.sns.project.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final RedisService redisService;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReadService chatReadService;

    public ChatMessageResponse saveMessage(Long senderId, String message, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        User sender = userService.getUserById(senderId);

        ChatMessage chatMessage = new ChatMessage(chatRoom, sender, message);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("Chat message saved successfully. MessageId: {}, RoomId: {}", savedMessage.getId(), roomId);

        chatReadService.saveReadMessage(senderId, savedMessage.getId(), roomId);
        
        String connectedUsersKey = RedisKeys.Chat.CONNECTED_USERS.getConnectedKey(roomId);
        Set<Long> connectedUsers = redisService.getValuesFromSet(connectedUsersKey, Long.class);
        
        Long participantCount = chatParticipantRepository.countByChatRoomId(roomId);
        Long initialUnreadCount = participantCount - 1;
        if (connectedUsers != null && !connectedUsers.isEmpty()) {
            initialUnreadCount -= connectedUsers.stream()
                    .filter(userId -> !userId.equals(senderId))
                    .count();
        }

        String unreadKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.getUnreadCountKey(savedMessage.getId());
        redisService.setValue(unreadKey, initialUnreadCount);
        
        if (connectedUsers != null && !connectedUsers.isEmpty()) {
            connectedUsers.stream()
                    .filter(userId -> !userId.equals(senderId))
                    .forEach(userId -> chatReadService.saveReadMessage(userId, savedMessage.getId(), roomId));
        }

        return new ChatMessageResponse(savedMessage, initialUnreadCount);
    }

    public List<ChatMessageResponse> getChatHistory(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdWithUser(roomId);
        return messages.stream().map(msg -> {
            String unreadKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.get() + msg.getId();
            Long unreadCount = redisService.getValue(unreadKey, Long.class).orElse(0L);
            return new ChatMessageResponse(msg, unreadCount);
        }).collect(Collectors.toList());
    }
}
