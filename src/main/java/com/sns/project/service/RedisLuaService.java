package com.sns.project.service;

import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisLuaService {

    private final RedisTemplate<String, String> stringRedisTemplate;

    private static final DefaultRedisScript<Long> PROCESS_UNREAD_SCRIPT;
    private static final DefaultRedisScript<Long> PROCESS_NEW_MESSAGE_SCRIPT;

    static {
        /*
         * 읽지 않은 메시지 처리 스크립트
         * KEYS[1] : lastReadMessageIdKey
         * KEYS[2] : messageZSetKey
         * KEYS[3] : unreadCountHashKey
         * return : unreadMessages(list)
         */
        PROCESS_UNREAD_SCRIPT = new DefaultRedisScript<>();
        PROCESS_UNREAD_SCRIPT.setResultType(Long.class);
        PROCESS_UNREAD_SCRIPT.setScriptText("""
            local lastReadMessageIdKey = KEYS[1]
            local messageZSetKey = KEYS[2]
            local unreadCountHashKey = KEYS[3]
            
            -- 최신 메시지 ID 가져오기
            local latestMessageId = redis.call('ZRANGE', messageZSetKey, -1, -1)[1]
            if not latestMessageId then
                return 0  -- Return 0 instead of {}
            end


            local newLastReadId = tonumber(latestMessageId)
            local currentLastReadId = tonumber(redis.call('GET', lastReadMessageIdKey) or '-1')
            if newLastReadId <= currentLastReadId then
                return 0  -- Return 0 instead of {}
            end
            
            local unreadMessages = redis.call('ZRANGEBYSCORE', messageZSetKey, currentLastReadId + 1, newLastReadId)
            for _, messageId in ipairs(unreadMessages) do
                redis.call('HINCRBY', unreadCountHashKey, messageId, -1)
            end
            
            redis.call('SET', lastReadMessageIdKey, newLastReadId)
            
            return newLastReadId
            """);

        /*
         * 새 메시지 처리 스크립트
         * KEYS[1] : participantsKey
         * KEYS[2] : connectedUsersKey
         * KEYS[3] : unreadCountHashKey
         * KEYS[4] : lastReadKeyPattern
         * ARGV[1] : roomId
         * ARGV[2] : messageId
         * ARGV[3] : senderId
         */
        PROCESS_NEW_MESSAGE_SCRIPT = new DefaultRedisScript<>();
        PROCESS_NEW_MESSAGE_SCRIPT.setResultType(Long.class);
        PROCESS_NEW_MESSAGE_SCRIPT.setScriptText("""
            local participants = redis.call('SMEMBERS', KEYS[1])
            local connectedUsers = redis.call('SMEMBERS', KEYS[2])
            local unreadCount = 0
            local roomId = ARGV[1]
            local messageId = ARGV[2]
            local senderId = ARGV[3]
            
            for _, participant in ipairs(participants) do
                local isConnected = redis.call('SISMEMBER', KEYS[2], participant)
                local isSender = (participant == senderId)
                
                if isConnected == 0 and not isSender then
                    unreadCount = unreadCount + 1
                else
                    local lastReadKey = string.gsub(KEYS[4], "{userId}", participant)
                    lastReadKey = string.gsub(lastReadKey, "{roomId}", roomId)
                    local lastReadId = redis.call('GET', lastReadKey)
                    if lastReadId ~= nil then
                        lastReadId = tonumber(lastReadId)
                    end
                    
                    if lastReadId == nil or lastReadId < tonumber(messageId) then
                        redis.call('SET', lastReadKey, messageId)
                    end
                end
            end
            redis.call('HSET', KEYS[3], messageId, unreadCount)
            return unreadCount
        """);
    }

    // 읽음 처리 메서드
    public Optional<Long> processUnreadMessages(String lastReadKey, String messageZSetKey, String unreadCountHashKey) {
        Long lastReadId =  stringRedisTemplate.execute(
            PROCESS_UNREAD_SCRIPT,
            List.of(lastReadKey, messageZSetKey, unreadCountHashKey)
        );
        return Optional.ofNullable(lastReadId);
    }

    // 새 메시지 처리 메서드
    public Long processNewMessage(
        String participantsKey, 
        String connectedUsersKey, 
        String unreadCountHashKey, 
        String lastReadKeyPattern, 
        String roomId, 
        String messageId,
        String senderId  // 작성자 ID 추가
    ) {
        return stringRedisTemplate.execute(
            PROCESS_NEW_MESSAGE_SCRIPT,
            List.of(participantsKey, connectedUsersKey, unreadCountHashKey, lastReadKeyPattern),
            roomId, messageId, senderId
        );
    }
}
