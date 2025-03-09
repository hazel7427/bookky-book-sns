package com.sns.project.service.chat;

import com.sns.project.config.constants.RedisKeys.Chat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.sns.project.config.constants.RedisKeys;
import com.sns.project.service.RedisService;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatReadService {

    private final RedisService redisService;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final int MAX_RETRY = 1;
    private static final long LOCK_EXPIRATION = 5;

    public void markAllAsRead(Long userId, Long roomId) {
        String lockKey = RedisKeys.Chat.CHAT_LOCK_KEY.getLockKey(userId, roomId);
        
        if (!tryAcquireLock(lockKey)) {
            return;
        }

        try {
            executeWithRetry(() -> processMarkAllAsRead(userId, roomId));
        } finally {
            redisService.deleteKey(lockKey);
        }
    }

    private boolean tryAcquireLock(String lockKey) {
         boolean acquired = redisService.setIfAbsent(lockKey, "LOCKED", LOCK_EXPIRATION);
         if (!acquired) {
             log.warn("Lock acquisition failed - markAllAsRead() already in progress");
         }
         return acquired;
//        return true;
    }

    private void executeWithRetry(Supplier<Boolean> task) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                if (task.get()) return;
            } catch (Exception e) {
                log.warn("Task failed, retrying... {}/{}", retryCount + 1, MAX_RETRY);
                e.printStackTrace();
            }
            retryCount++;
        }
        throw new RuntimeException("Task failed after max retries");
    }

    /*
     * 유저의 읽지 않은 메시지를 조회하고 읽음 처리합니다.
     */
    private boolean processMarkAllAsRead(Long userId, Long roomId) {
        Set<Long> unreadMessages = getUnreadMessages(userId, roomId);
        if (unreadMessages.isEmpty()) {
            return true;
        }

        processUnreadMessages(userId, roomId, unreadMessages);
        return true;
    }

    private Set<Long> getUnreadMessages(Long userId, Long roomId) {
        String lastReadMessageId = getLastReadMessageId(userId, roomId);
        Set<Long> messages = fetchUnreadMessages(roomId, lastReadMessageId);
        return filterNewUnreadMessages(messages, userId);
    }

    private String getLastReadMessageId(Long userId, Long roomId) {
        String key = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
        return redisService.getValue(key, String.class).orElse("0");
    }

    private Set<Long> fetchUnreadMessages(Long roomId, String lastMessageId) {
        String messagesKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        long lastRead = Long.parseLong(lastMessageId);
        return redisService.getValuesFromZSet(messagesKey, lastRead + 1, Double.POSITIVE_INFINITY, Long.class);
    }

    private Set<Long> filterNewUnreadMessages(Set<Long> messages, Long userId) {
        if (messages == null || messages.isEmpty()) {
            return Set.of();
        }

        // 이미 읽은 메시지는 제외하고 새로운 메시지만 필터링
        Set<Long> newUnreadMessages = messages.stream()
            .filter(messageId -> !redisService.isMemberOfSet(
                Chat.CHAT_READ_USERS_SET_KEY.getReadUserKey(messageId.toString()), 
                userId
            ))
            .collect(Collectors.toSet());

        return newUnreadMessages;
    }

    private void processUnreadMessages(Long userId, Long roomId, Set<Long> unreadMessages) {
        Long newLastMessageId = Long.parseLong(getLastReadMessageId(userId, roomId));
        
        for (Long messageId : unreadMessages) {
            markMessageAsRead(userId, messageId);
            newLastMessageId = Math.max(newLastMessageId, messageId);
        }

        updateLastReadMessage(userId, roomId, newLastMessageId);
    }

    private void markMessageAsRead(Long userId, Long messageId) {
        String messageReadUsers = Chat.CHAT_READ_USERS_SET_KEY.getReadUserKey(messageId.toString());
        String unreadCountKey = Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
        
        redisService.addToSet(messageReadUsers, userId);
        Long count = redisService.incrementHash(unreadCountKey, messageId.toString(), -1);
        
        addToBatchQueue(messageId);
        notifyUnreadCount(messageId, count);
    }

    private void addToBatchQueue(Long messageId) {
        String batchKey = Chat.CHAT_MESSAGE_BATCH_SET_KEY.getMessageBatchQueueKey();
        redisService.addToSet(batchKey, messageId);
    }

    private void notifyUnreadCount(Long messageId, Long count) {
        messagingTemplate.convertAndSend("/topic/unread/" + messageId, count);
        log.info("Message {} unread count updated to {}", messageId, count);
    }

    private void updateLastReadMessage(Long userId, Long roomId, Long messageId) {
        String key = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
        redisService.setValue(key, messageId);
    }
}

