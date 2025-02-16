package com.sns.project.dto.notification.workerDto;

import java.util.List;

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
public class RawNotificationDto {
    private Long senderId;
    private List<Long> recipientIds;
    private String message;
    private Long contentId;
}


