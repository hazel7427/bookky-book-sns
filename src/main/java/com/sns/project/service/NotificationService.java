package com.sns.project.service;

import com.sns.project.domain.Notification;
import com.sns.project.dto.notification.RequestNotificationDto;
import com.sns.project.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void createNotification(Long userId, String message) {
        Notification notification = new Notification(userId, message);
        notificationRepository.save(notification);

        // WebSocket을 통해 해당 사용자에게 실시간 전송
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public void createNotification(RequestNotificationDto requestNotificationDto) {
        Long userId = requestNotificationDto.getUserId();
        String message = requestNotificationDto.getMessage();
        Notification notification = new Notification(userId, message);
        notificationRepository.save(notification);

        // WebSocket을 통해 해당 사용자에게 실시간 전송
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}
