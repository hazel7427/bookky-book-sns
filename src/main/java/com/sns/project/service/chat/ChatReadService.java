package com.sns.project.service.chat;

import com.sns.project.repository.chat.ChatMessageRepository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.sns.project.config.constants.RedisKeys;
import com.sns.project.service.RedisLuaService;
import com.sns.project.service.redis.StringRedisService;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatReadService {
    @Qualifier("chatRedisTemplate")  // 변경된 빈 이름 지정
    private final StringRedisService stringRedisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisLuaService redisLuaService;
    private final ChatMessageRepository chatMessageRepository;

    // private void ensureRedisDataExists(Long userId, Long roomId) {
    //     String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
    //     String messageZSetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
    //     String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();

    //     // last_read_id 복구
    //     if (!stringRedisService.exists(lastReadKey)) {
    //         Long lastReadMessageId = chatReadRepository.findLastReadMessageId(userId, roomId).orElse(-1L);
    //         stringRedisService.setValue(lastReadKey, String.valueOf(lastReadMessageId), Duration.ofHours(6));
    //     }
// 
    //     // messageZSet 복구
    //     if (!stringRedisService.exists(messageZSetKey)) {
    //         List<ChatMessage> messages = chatMessageRepository.findMessagesForRoom(roomId);
    //         messages.forEach(msg -> stringRedisService.addToZSet(messageZSetKey, msg.getId().toString(), msg.getId()));
    //     }

    //     // unreadCountHash 복구
    //     if (!stringRedisService.exists(unreadCountKey)) {
    //         List<Object[]> unreadCounts = chatMessageRepository.findUnreadCountsForRoom(roomId);
    //         unreadCounts.forEach(entry -> {
    //             Long messageId = (Long) entry[0];
    //             Long unreadCount = (Long) entry[1];
    //             stringRedisService.setHashValue(unreadCountKey, messageId.toString(), unreadCount.toString());
    //         });
    //     }
    // }


        
    /*
     * 유저의 읽지 않은 메시지를 조회하고 읽음 처리합니다.
     */
     public void markAllAsRead(Long userId, Long roomId) {
        
        String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
        String messageZSetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();

        long curLastReadId = Long.parseLong(stringRedisService.getValue(lastReadKey).orElse("-1"));
        Optional<Long> newLastReadId = redisLuaService.processUnreadMessages(
            lastReadKey,
            messageZSetKey,
            unreadCountKey
        );
        // 읽음 처리할 메시지 없으면 종료
        if(newLastReadId.isEmpty()) {
            return;
        }

        for(long i = curLastReadId + 1; i <= newLastReadId.get(); i++) {
            String messageId = String.valueOf(i);
            Optional<String> count = stringRedisService.getHashValue(unreadCountKey, messageId);
            if(count.isPresent()) {
                notifyUnreadCount(Long.parseLong(messageId), Long.parseLong(count.get()));
            }
        }

    }



    private void notifyUnreadCount(Long messageId, Long count) {
        messagingTemplate.convertAndSend("/topic/unread/" + messageId, count);
        log.info("Message {} unread count updated to {}", messageId, count);
    }


}

