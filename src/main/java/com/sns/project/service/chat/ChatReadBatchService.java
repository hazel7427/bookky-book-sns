package com.sns.project.service.chat;

import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatUnreadCount;
import com.sns.project.repository.chat.ChatUnReadRepository;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatReadBatchService {
    private final RedisService redisService;
    private final ChatUnReadRepository chatUnReadRepository;
    
    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void batchUpdateUnreadCounts() {

        String chatReadQueueKey = RedisKeys.Chat.CHAT_READ_QUEUE.get();
        Set<Long> messageIds = redisService.getValuesFromSet(chatReadQueueKey, Long.class);

        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        for (Long messageId : messageIds) {
            ChatUnreadCount chatUnreadCount = chatUnReadRepository.findByMessageId(messageId);
            if (chatUnreadCount != null) {
                chatUnreadCount.setUnreadCount(chatUnreadCount.getUnreadCount() - 1);
                chatUnReadRepository.save(chatUnreadCount);
            }
        }

        // 배치 반영 후 큐 비우기
        redisService.deleteValue(chatReadQueueKey);
    }
}
