package com.sns.project.handler.exceptionHandler.exception.notfound;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long notificationId) {
        super("Notification with ID " + notificationId + " not found");
    }
}
