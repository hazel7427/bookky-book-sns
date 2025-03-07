package com.sns.project.service.chat;

import com.sns.project.config.constants.RedisKeys.Chat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.stereotype.Service;
import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.service.RedisService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatReadService {
    private final RedisService redisService;
    private static final int MAX_RETRY = 1; // 최대 재시도 횟수
    private static final long LOCK_EXPIRATION = 5; // 락 만료 시간 (초)

    /**
     * 특정 사용자가 채팅방에서 읽지 않은 메시지를 모두 읽음 처리
     */
    public void markAllAsRead(Long userId, Long roomId) {
        String lockKey = RedisKeys.Chat.CHAT_LOCK_KEY.getLockKey(userId, roomId);
        
        // SETNX로 분산락 획득 시도
        boolean acquired = redisService.setIfAbsent(lockKey, "LOCKED", LOCK_EXPIRATION);
        if (!acquired) {
            log.warn("Lock acquisition failed - markAllAsRead() already in progress for userId={}, roomId={}", userId, roomId);
            return;
        }

        try {
            int retryCount = 0;
            while (retryCount < MAX_RETRY) {
                try {
                    if (processMarkAllAsRead(userId, roomId)) {
                        return; // 성공하면 종료
                    }
                } catch (Exception e) {
                    log.warn("markAllAsRead() failed, retrying... {}/{}", retryCount + 1, MAX_RETRY);
                    e.printStackTrace();
                }
                retryCount++;
            }
            throw new RuntimeException("markAllAsRead() failed after max retries.");
        } finally {
            // 락 해제
            redisService.deleteKey(lockKey);
        }
    }

    /**
     * 읽음 처리 실행 (트랜잭션 적용)
     */
    @Transactional
    private boolean processMarkAllAsRead(Long userId, Long roomId) {
        String lastReadTimestampKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
//        String readQueueKey = RedisKeys.Chat.CHAT_MESSAGE_BATCH_QUEUE_KEY.getMessageBatchQueueKey();
        String messagesKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);

        String lastMessageId = redisService.getValue(lastReadTimestampKey, String.class).orElse("0");
        long lastRead = Long.parseLong(lastMessageId);

        Set<Long> unreadMessages = redisService.getValuesFromZSet(messagesKey,
            lastRead + 1, Double.POSITIVE_INFINITY, Long.class);
        if (unreadMessages == null || unreadMessages.isEmpty()) {
            return true; // 읽을 메시지가 없으면 성공 처리
        }

        // ✅ `message:read_users`에서 읽음 확인 후 필터링 (이미 읽은 메시지는 제외)
        List<String> newUnreadMessages = unreadMessages.stream()
            .map(String::valueOf)
            .filter(messageId -> !redisService.isMemberOfSet(Chat.CHAT_READ_USERS_KEY.getReadUserKey(messageId), userId)).toList();

        System.out.println("읽지않은 메시지");
        newUnreadMessages.forEach(System.out::println);

        if (newUnreadMessages.isEmpty()) {
            return true; // 이미 모두 읽음 처리된 상태
        }



        // 4️⃣ `unread_count` 감소 및 `SET`에 읽음 기록 추가
        for (String messageId : newUnreadMessages) {
            String messageReadUsers = Chat.CHAT_READ_USERS_KEY.getReadUserKey(messageId);
            String unreadCountKey = Chat.CHAT_UNREAD_COUNT_KEY.getUnreadCountKey();
            System.out.println("message ID: "+messageId+"-> 유저가 읽음( user id: "+userId+")");
            redisService.addToSet(messageReadUsers, userId);
            redisService.incrementHash(unreadCountKey, String.valueOf(messageId), -1);
        }


        // 6️⃣ 사용자의 마지막 읽은 메시지 업데이트
        redisService.setValue(lastReadTimestampKey, System.currentTimeMillis());

        return true;

    }
}
