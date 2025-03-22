package com.sns.project.chat;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RedisTemplate<String, String> redisTemplate;

    // ✅ Store WebSocket sessions per roomId
    private final Map<Integer, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(
        @Qualifier("chatRedisTemplate")
        RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("✅ WebSocket connected: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject json = new JSONObject(message.getPayload());
        String type = json.getString("type");

        if ("JOIN".equals(type)) {
            int roomId = json.getInt("roomId");

            // Ensure room exists
            roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
            roomSessions.get(roomId).add(session);
            log.info("✅ User joined roomId: {}", roomId);

        } else if ("MESSAGE".equals(type)) {
            int roomId = json.getInt("roomId");
            String msg = json.getString("message");

            // ✅ Store message in Redis Stream (room-specific)
            String streamKey = "chat-stream:" + roomId;
            redisTemplate.opsForStream().add(streamKey, Collections.singletonMap("message", msg));
            log.info("📤 Message saved in Redis Stream ({}): {}", streamKey, msg);

            // ✅ Send message only to users in the same room
            broadcastToRoom(roomId, msg);
        }
    }

    // ✅ Send message to all users in the correct room
    public void broadcastToRoom(int roomId, String message) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) return;

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }

        log.info("📩 Message sent to room {}: {}", roomId, message);
    }
}
