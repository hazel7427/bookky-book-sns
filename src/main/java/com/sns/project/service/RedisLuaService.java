package com.sns.project.service;

import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisLuaService {

    private final RedisTemplate<String, String> stringRedisTemplate;

    private static final DefaultRedisScript<Long> PROCESS_UNREAD_SCRIPT;
    private static final DefaultRedisScript<Long> PROCESS_NEW_MESSAGE_SCRIPT;

    static {
        // 📌 읽지 않은 메시지 처리 스크립트 (마지막 읽은 메시지 ID 업데이트)
        PROCESS_UNREAD_SCRIPT = new DefaultRedisScript<>();
        PROCESS_UNREAD_SCRIPT.setResultType(Long.class);
        PROCESS_UNREAD_SCRIPT.setScriptText("""
            local lastReadMessageKey = KEYS[1]
            local messageZSetKey = KEYS[2]
            local unreadCountHashKey = KEYS[3]
            
            -- 최신 메시지 ID 가져오기
            local latestMessageId = redis.call('ZRANGE', messageZSetKey, -1, -1)[1]
            if not latestMessageId then
                return 0
            end

            local currentLastReadId = tonumber(redis.call('GET', lastReadMessageKey) or '-1')
            local newLastReadId = tonumber(latestMessageId)

            if newLastReadId <= currentLastReadId then
                return 0
            end
            
            -- 읽지 않은 메시지 목록 가져오기 & unreadCount 감소
            local unreadMessages = redis.call('ZRANGEBYSCORE', messageZSetKey, currentLastReadId + 1, newLastReadId)
            for _, messageId in ipairs(unreadMessages) do
                redis.call('HINCRBY', unreadCountHashKey, messageId, -1)
            end
            
            redis.call('SET', lastReadMessageKey, newLastReadId)
            return newLastReadId
        """);

        // 📌 새 메시지 처리 스크립트 (읽음 처리 + unreadCount 계산)
        PROCESS_NEW_MESSAGE_SCRIPT = new DefaultRedisScript<>();
        PROCESS_NEW_MESSAGE_SCRIPT.setResultType(Long.class);
        PROCESS_NEW_MESSAGE_SCRIPT.setScriptText("""
            local participants = redis.call('SMEMBERS', KEYS[1])
            local connectedUsersKey = KEYS[2]
            local unreadCountHashKey = KEYS[3]
            local lastReadKeyPattern = KEYS[4]
            local messageZSetKey = KEYS[5]

            local roomId = ARGV[1]
            local messageId = tonumber(ARGV[2])
            local senderId = ARGV[3]

            local unreadCount = 0

            for _, participantId in ipairs(participants) do
                local isConnected = redis.call('SISMEMBER', connectedUsersKey, participantId)
                local isSender = (participantId == senderId)
                
                if isConnected == 0 and not isSender then
                    unreadCount = unreadCount + 1
                else
                    local lastReadMessageKey = string.gsub(lastReadKeyPattern, "{userId}", participantId)
                    lastReadMessageKey = string.gsub(lastReadMessageKey, "{roomId}", roomId)
                    local lastReadId = tonumber(redis.call('GET', lastReadMessageKey) or "-1")

                    -- 새로운 메시지를 읽음 처리
                    if lastReadId < messageId then
                        redis.call('SET', lastReadMessageKey, messageId)
                        
                        -- lastReadId ~ messageId 사이의 unreadCount 감소 (batch 처리)
                        local unreadMessages = redis.call('ZRANGEBYSCORE', messageZSetKey, lastReadId + 1, messageId - 1) or {}
                        for _, mid in ipairs(unreadMessages) do
                            redis.call('HINCRBY', unreadCountHashKey, mid, -1)
                        end
                    end
                end
            end
            redis.call('HSET', unreadCountHashKey, messageId, unreadCount)
            return unreadCount
        """);
    }

    // 📌 읽음 처리 메서드 (유저가 메시지를 읽었을 때 호출)
    public Optional<Long> processUnreadMessages(String lastReadKey, String messageZSetKey, String unreadCountHashKey) {
        Long lastReadId = stringRedisTemplate.execute(
            PROCESS_UNREAD_SCRIPT,
            List.of(lastReadKey, messageZSetKey, unreadCountHashKey)
        );
        return Optional.ofNullable(lastReadId);
    }

    // 📌 새 메시지 처리 메서드 (새로운 메시지가 왔을 때 호출)
    public Long processNewMessage(
        String participantsKey,
        String connectedUsersKey,
        String unreadCountHashKey,
        String lastReadKeyPattern,
        String messageZSetKey,

        String roomId,
        String messageId,
        String senderId
    ) {
        return stringRedisTemplate.execute(
            PROCESS_NEW_MESSAGE_SCRIPT,
            List.of(participantsKey, connectedUsersKey, unreadCountHashKey, lastReadKeyPattern, messageZSetKey),
            roomId, messageId, senderId
        );
    }
}
