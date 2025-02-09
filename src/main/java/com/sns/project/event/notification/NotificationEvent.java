package com.sns.project.event.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationEvent {
    private final Long userId;
    private final String message;
}