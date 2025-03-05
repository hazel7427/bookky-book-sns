package com.sns.project.service.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatReadMessage;
import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.repository.chat.ChatReadRepository;
import com.sns.project.repository.chat.ChatMessageRepository;
import com.sns.project.service.RedisService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatReadService {
    private final RedisService redisService;
    private final ChatReadRepository chatReadRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void markAllAsRead(Long userId, Long roomId) {
        ChatMessage lastMessage = chatMessageRepository.findLastMessage(roomId);
        
        if (lastMessage == null) {
            return;
        }

        Long lastReadId = chatReadRepository.findLastReadMessageId(userId, roomId)
            .orElse(0L);

        List<ChatMessage> unreadMessages = chatMessageRepository
            .findUnreadChatMessage(roomId, lastReadId);

        try {
            List<ChatReadMessage> readMessages = unreadMessages.stream()
                .map(message -> ChatReadMessage.builder()
                    .messageId(message.getId())
                    .userId(userId)
                    .roomId(roomId)
                    .readAt(LocalDateTime.now())
                    .build())
                .collect(Collectors.toList());
            
            chatReadRepository.saveAll(readMessages);
        } catch (DataIntegrityViolationException e) {
            // 이미 읽음 처리된 메시지는 무시
            log.info("Some messages were already marked as read", e);
        }

        // Redis의 각 메시지 unread count 감소
        unreadMessages.forEach(message -> {
            String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.getUnreadCountKey(message.getId());
            redisService.decrementValue(unreadCountKey);
        });
    }

    // public void decrementUnreadCount(Long messageId) {
    //     String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.getUnreadCountKey(messageId);
    //     Optional<Long> unreadCount = redisService.decrementValue(unreadCountKey);
        
    //     if (unreadCount.isPresent() && unreadCount.get() == 0) {
    //         redisService.deleteValue(unreadCountKey);
    //     }
    // }

    public void saveReadMessage(Long userId, Long messageId, Long roomId) {
        ChatReadMessage readMessage = ChatReadMessage.builder()
            .messageId(messageId)
            .userId(userId)
            .roomId(roomId)
            .readAt(LocalDateTime.now())
            .build();
        chatReadRepository.save(readMessage);
    }
        
}
