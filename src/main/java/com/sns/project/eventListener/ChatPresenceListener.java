package com.sns.project.eventListener;

import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.sns.project.service.chat.ChatPresenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatPresenceListener {
    private final ChatPresenceService chatPresenceService;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        Long roomId = extractRoomId(event);
        Long userId = extractUserId(event);
        log.error("roomId: {}", roomId);
        log.error("userId: {}", userId);
        if (roomId != null && userId != null) {
            chatPresenceService.userEnteredRoom(roomId, userId);
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        Long roomId = extractRoomId(event);
        Long userId = extractUserId(event);
        if (roomId != null && userId != null) {
            chatPresenceService.userLeftRoom(roomId, userId);
        }
    }

    private Long extractRoomId(SessionSubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        if (destination != null && destination.startsWith("/topic/chat/")) {
            return Long.parseLong(destination.replace("/topic/chat/", ""));
        }
        return null;
    }

    private Long extractUserId(SessionSubscribeEvent event) {
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        return getUserIdFromSession(sessionId); // 세션 ID를 통해 userId 매핑 (세션 저장 필요)
    }

    private Long extractRoomId(SessionUnsubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        if (destination != null && destination.startsWith("/topic/chat/")) {
            return Long.parseLong(destination.replace("/topic/chat/", ""));
        }
        return null;
    }

    private Long extractUserId(SessionUnsubscribeEvent event) {
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        return getUserIdFromSession(sessionId);
    }

    private Long getUserIdFromSession(String sessionId) {
        // 실제 사용자 ID를 WebSocket 세션에서 가져오는 로직 구현 필요
        return 1L; // 예제 (실제 로직 필요)
    }
}
