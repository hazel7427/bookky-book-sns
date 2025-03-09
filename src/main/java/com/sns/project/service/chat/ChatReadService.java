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

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatReadService {

    private final RedisService redisService;
    private static final int MAX_RETRY = 1; // 최대 재시도 횟수
    private static final long LOCK_EXPIRATION = 5; // 락 만료 시간 (초)
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 특정 사용자가 채팅방에서 읽지 않은 메시지를 모두 읽음 처리
     */
    public void markAllAsRead(Long userId, Long roomId) {
        String lockKey = RedisKeys.Chat.CHAT_LOCK_KEY.getLockKey(userId, roomId);
        
        // SETNX로 분산락 획득 시도
        boolean acquired = redisService.setIfAbsent(lockKey, "LOCKED", LOCK_EXPIRATION);
//        boolean acquired = ;
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
//    @Transactional
    private boolean processMarkAllAsRead(Long userId, Long roomId) {
        String lastReadMessageId = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(
            userId, roomId);
        String unreadBatchQueueKey = RedisKeys.Chat.CHAT_MESSAGE_BATCH_SET_KEY.getMessageBatchQueueKey();
        String messagesKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);

        String lastMessageId = redisService.getValue(lastReadMessageId, String.class).orElse("0");
        long lastRead = Long.parseLong(lastMessageId);

        Set<Long> unreadMessages = redisService.getValuesFromZSet(messagesKey,
            lastRead + 1, Double.POSITIVE_INFINITY, Long.class);
        if (unreadMessages == null || unreadMessages.isEmpty()) {
            return true; // 읽을 메시지가 없으면 성공 처리
        }

        // ✅ `message:read_users`에서 읽음 확인 후 필터링 (이미 읽은 메시지는 제외)
        List<String> newUnreadMessages = unreadMessages.stream()
            .map(String::valueOf)
            .filter(messageId -> !redisService.isMemberOfSet(Chat.CHAT_READ_USERS_SET_KEY.getReadUserKey(messageId), userId)).toList();

        System.out.println("읽지않은 메시지");
        newUnreadMessages.forEach(System.out::println);

        if (newUnreadMessages.isEmpty()) {
            return true; // 이미 모두 읽음 처리된 상태
        }



        // 4️⃣ `unread_count` 감소 및 `SET`에 읽음 기록 추가
        Long newLastMessageId = Long.parseLong(lastMessageId);
        log.info("채팅방에 입장한 유저의 안읽음 메시지들에 대해 읽음 처리합니다. 유저 ID:{}", userId);
        for (String messageId : newUnreadMessages) {
            log.info("읽음 처리할 메시지 ID: {}", messageId);
            // 유저가 해당 메시지를 이미 읽었는 지 검사 후 안읽음 수 감소처리
            String messageReadUsers = Chat.CHAT_READ_USERS_SET_KEY.getReadUserKey(messageId);
            String unreadCountKey = Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
            redisService.addToSet(messageReadUsers, userId);

            // decrease unread count of message and put a message id in batch set.
            Long count = redisService.incrementHash(unreadCountKey, String.valueOf(messageId), -1);
            String unreadBatchKey = Chat.CHAT_MESSAGE_BATCH_SET_KEY.getMessageBatchQueueKey();
            redisService.addToSet(unreadBatchKey, messageId);

            // send unread event to client
            messagingTemplate.convertAndSend("/topic/unread/" + messageId, count);
            log.info("읽음 처리된 메시지의 최종 안읽음 수 = {}", count);

            // update last read message id
            newLastMessageId = Math.max(newLastMessageId, Long.parseLong(messageId));
        }


        // 6️⃣ 사용자의 마지막 읽은 메시지 업데이트
        redisService.setValue(lastReadMessageId, newLastMessageId);

        return true;

    }
}
