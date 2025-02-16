package com.sns.project.dto.notification.response;

import java.util.List;
import java.util.stream.Collectors;

import com.sns.project.domain.notification.Notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ResponseNotificationListDto {
    
    private int page;
    private int size;
    private List<ResponseNotificationDto> notifications;
    private int totalPages;

    public ResponseNotificationListDto(List<Notification> notifications, int page, int size, int totalPages) {
        this.notifications = notifications.stream()
            .map(ResponseNotificationDto::new)
            .collect(Collectors.toList());
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }
}
