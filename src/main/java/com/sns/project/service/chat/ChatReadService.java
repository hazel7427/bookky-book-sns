package com.sns.project.service.chat;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatReadService {
    private final RedisService redisService;
    private final ChatPresenceService chatPresenceService;


    public void markMessageAsRead(Long userId, Long messageId) {
        String unreadKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_KEY.get() + messageId;
        Long unreadCount = redisService.decrementValue(unreadKey);

        String chatReadQueueKey = RedisKeys.Chat.CHAT_READ_QUEUE.get();
        redisService.pushToQueue(chatReadQueueKey, messageId);

        if (unreadCount != null && unreadCount <= 0) {
            redisService.deleteValue(unreadKey);
        }
    }
}
