package com.sns.project.service.chat;

import com.sns.project.config.constants.RedisKeys.Chat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.sns.project.config.constants.RedisKeys;
import com.sns.project.service.RedisLuaService;
import com.sns.project.service.redis.StringRedisService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatReadService {
    @Qualifier("chatRedisTemplate")  // 변경된 빈 이름 지정
    private final StringRedisService stringRedisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisLuaService redisLuaService;
    


        
    /*
     * 유저의 읽지 않은 메시지를 조회하고 읽음 처리합니다.
     */
    public boolean markAllAsRead(Long userId, Long roomId) {
        Set<Long> unreadMessages = fetchUnreadMessages(roomId, getLastReadMessageId(userId, roomId));

        log.info("User {} read {} messages in room {}", userId, unreadMessages.size(), roomId);
        unreadMessages.stream().forEach(System.out::println);

        processUnreadMessages(userId, roomId, unreadMessages);
        return true;
    }

    private Long getLastReadMessageId(Long userId, Long roomId) {
        String key = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
        return stringRedisService.getValue(key)
            .map(Long::parseLong)
            .orElse(-1L);
    }

    private Set<Long> fetchUnreadMessages(Long roomId, Long lastReadMessageId) {
        String messagesKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        return stringRedisService.getZSetRange(messagesKey, lastReadMessageId + 1, Double.POSITIVE_INFINITY).stream()
            .map(Long::parseLong)
            .filter(messageId -> lastReadMessageId < messageId)
            .collect(Collectors.toSet());
    }




    private void processUnreadMessages(Long userId, Long roomId, Set<Long> unreadMessages) {
        if (unreadMessages.isEmpty()) return;
        
        Long maxMessageId = Collections.max(unreadMessages);
        String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
        String messageZSetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();

        Long processedCount = redisLuaService.processUnreadMessages(
            lastReadKey,
            messageZSetKey,
            unreadCountKey,
            String.valueOf(maxMessageId)
        );

        // 처리된 메시지들에 대해 배치 큐에 추가 및 알림
        unreadMessages.forEach(messageId -> {
//            addToBatchQueue(messageId);
            notifyUnreadCount(messageId, getUnreadCount(messageId));
        });
    }

    private Long getUnreadCount(Long messageId) {
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
        return Long.valueOf(stringRedisService.getHashValue(unreadCountKey, messageId.toString()));
    }

    private void addToBatchQueue(Long messageId) {
        String batchKey = Chat.CHAT_MESSAGE_BATCH_SET_KEY.getMessageBatchQueueKey();
        stringRedisService.addToSet(batchKey, messageId.toString());
    }

    private void notifyUnreadCount(Long messageId, Long count) {
        messagingTemplate.convertAndSend("/topic/unread/" + messageId, count);
        log.info("Message {} unread count updated to {}", messageId, count);
    }


}

