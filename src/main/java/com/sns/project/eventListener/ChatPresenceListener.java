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
        log.info("User {} entered room {}", userId, roomId);
        if (roomId != null && userId != null) {
            chatPresenceService.userEnteredRoom(roomId, userId);
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        Long roomId = extractRoomId(event);
        Long userId = extractUserId(event);
        log.info("User {} left room {}", userId, roomId);
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
        String userId = (String) event.getUser().getName();
        return Long.parseLong(userId);
    }

    private Long extractRoomId(SessionUnsubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        if (destination != null && destination.startsWith("/topic/chat/")) {
            return Long.parseLong(destination.replace("/topic/chat/", ""));
        }
        return null;
    }

    private Long extractUserId(SessionUnsubscribeEvent event) {
        String userId = (String) event.getUser().getName();
        return Long.parseLong(userId);
    }

}
