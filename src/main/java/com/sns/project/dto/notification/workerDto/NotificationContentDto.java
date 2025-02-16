package com.sns.project.dto.notification.workerDto;

import com.sns.project.domain.notification.NotificationContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationContentDto {
    private Long id;
    private String message;  // ✅ Notification message
}
