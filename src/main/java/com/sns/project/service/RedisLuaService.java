package com.sns.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
public class RedisLuaService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisLuaService(@Qualifier("chatRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private static final DefaultRedisScript<Long> PROCESS_UNREAD_SCRIPT;
    private static final DefaultRedisScript<Long> PROCESS_NEW_MESSAGE_SCRIPT;

    static {
        try {
            // Lua 스크립트 파일 로드
            PROCESS_UNREAD_SCRIPT = new DefaultRedisScript<>(loadScript("lua/process_unread.lua"), Long.class);
            PROCESS_NEW_MESSAGE_SCRIPT = new DefaultRedisScript<>(loadScript("lua/process_new_message.lua"), Long.class);
        } catch (IOException e) {
            throw new RuntimeException("Lua 스크립트 로드 실패", e);
        }
    }

    // Lua 스크립트 파일 읽기
    private static String loadScript(String path) throws IOException {
        try (var inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }


    // 📌 읽음 처리 메서드 (유저가 메시지를 읽었을 때 호출)
    public Optional<Long> processUnreadMessages(String lastReadKey, String messageZSetKey, String unreadCountHashKey) {
        Long lastReadId = redisTemplate.execute(
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
        return redisTemplate.execute(
            PROCESS_NEW_MESSAGE_SCRIPT,
            List.of(participantsKey, connectedUsersKey, unreadCountHashKey, lastReadKeyPattern, messageZSetKey),
            roomId, messageId, senderId
        );
    }
}
