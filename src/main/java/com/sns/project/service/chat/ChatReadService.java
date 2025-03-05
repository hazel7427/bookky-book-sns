package com.sns.project.service.chat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatReadMessage;
import com.sns.project.repository.chat.ChatReadRepository;
import com.sns.project.service.RedisService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatReadService {
    private final RedisService redisService;
    private final ChatReadRepository chatReadRepository;


    // 메시지 읽음 처리 후 데이터베이스에 저장
    @Transactional
    public void markMessageAsRead(Long userId, Long messageId, Long roomId) {
        String UNREAD_COUNT_KEY = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.getUnreadCountKey(messageId);
        Optional<Long> unreadCount = Optional.empty();
        if(chatReadRepository.findByMessageIdAndUserId(messageId, userId) == null) {
            log.info("uneread L userId: {}, messageId: {}, roomId: {}", userId, messageId, roomId);
            unreadCount = redisService.decrementValue(UNREAD_COUNT_KEY);
            if (unreadCount.isEmpty()) {
                return;
            }
            if(unreadCount.get() == 0) {
                redisService.deleteValue(UNREAD_COUNT_KEY);
            }
            chatReadRepository.save(ChatReadMessage.builder()
                .messageId(messageId)
                .userId(userId)
                .roomId(roomId)
                .readAt(LocalDateTime.now())
                .build());
        }
    }
}
