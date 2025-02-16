package com.sns.project.worker.notification;

import com.sns.project.service.NotificationCrudService;
import com.sns.project.service.NotificationService;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sns.project.config.constants.Constants.NotificationConstants;
import com.sns.project.domain.notification.NotificationContent;
import com.sns.project.domain.notification.Notification;
import com.sns.project.domain.user.User;
import com.sns.project.dto.notification.response.ResponseNotificationDto;
import com.sns.project.dto.notification.workerDto.BatchProcessedNotificationDto;
import com.sns.project.dto.notification.workerDto.RawNotificationDto;
import com.sns.project.repository.NotificationRepository;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationWorker {

    private final RedisService redisService;
    private final NotificationRepository notificationRepository;
    private final NotificationCrudService notificationCrudService;
    private final SimpMessagingTemplate messagingTemplate; // ✅ Inject WebSocket Template

    public void enqueue(BatchProcessedNotificationDto batchDto) {
        redisService.pushToQueue(NotificationConstants.NOTIFICATION_QUEUE_KEY, batchDto);
    }
    public void processBatches() {
        while (true) {
            try {
                BatchProcessedNotificationDto batchDto = redisService.popFromQueue(
                        NotificationConstants.NOTIFICATION_QUEUE_KEY,
                        BatchProcessedNotificationDto.class
                );

                if (batchDto != null) {
                    work(batchDto);
                }
            } catch (Exception e) {
                log.error("Error processing notification batch", e);
            }
        }
    }

    public void work(BatchProcessedNotificationDto batchDto) {
        NotificationContent content = notificationCrudService.findContentById(batchDto.getContentId());
        List<Notification> notifications = batchDto.getRecipientIds().stream()
            .map(recipientId -> makeNotification(content, recipientId))
            .toList();
        notificationRepository.saveAll(notifications);
        log.info("Stored {} notifications in DB", notifications.size());
        sendWebSocketNotifications(notifications);
    }

    private Notification makeNotification(NotificationContent content, Long recipientId) {
        return Notification.builder()
                .notificationContent(content)
                .receiver(User.builder().id(recipientId).build())
                .build();
    }
    private void sendWebSocketNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            String destination = "/topic/notifications/" + notification.getReceiver().getId();
            ResponseNotificationDto responseDto = new ResponseNotificationDto(notification);
            messagingTemplate.convertAndSend(destination, responseDto);
        }

        log.info("Sent WebSocket notifications to {} recipients", notifications.size());
    }
}
