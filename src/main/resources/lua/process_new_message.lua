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
