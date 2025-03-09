package com.sns.project.service.chat;

import com.sns.project.config.constants.RedisKeys.Chat;

import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatUnreadCount;
import com.sns.project.repository.chat.ChatUnReadRepository;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatReadBatchService {
    private final RedisService redisService;
    private final ChatUnReadRepository chatUnReadRepository;

    @Scheduled(fixedRate = 10000) // ✅ 10초마다 실행 (이전 실행이 끝난 후 10초 후 실행)
    @Transactional
    public void batchUpdateUnreadCounts() {
        String chatReadQueueKey = RedisKeys.Chat.CHAT_MESSAGE_BATCH_SET_KEY.getMessageBatchQueueKey();

        // ✅ Redis에서 배치할 메시지 가져오기 (최대 100개씩 처리하여 부하 방지)
        Set<Long> messageIds = redisService.popMultipleFromSet(chatReadQueueKey, 100, Long.class);

        if (messageIds == null || messageIds.isEmpty()) {
            return; // ✅ 처리할 데이터가 없으면 종료
        }

        for (Long messageId : messageIds) {
            String key = Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
            String field = String.valueOf(messageId);
            int unreadCountOfMessage = redisService.getValueFromHash(key, field, Integer.class);

            if (chatUnReadRepository.existsById(messageId)) {
                chatUnReadRepository.updateUnreadCount(messageId, unreadCountOfMessage);
                continue;
            }

            ChatUnreadCount unreadCount = ChatUnreadCount.builder()
                .messageId(messageId)
                .unreadCount(unreadCountOfMessage)
                .build();
            chatUnReadRepository.save(unreadCount);
        }
    }

}
