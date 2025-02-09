package com.sns.project.event.notification;

import com.sns.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    @Async
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Handling notification event for user: {}", event.getUserId());
        notificationService.createNotification(event.getUserId(), event.getMessage());
    }
} 