package com.sns.project.config.constants;

public class Constants {
    public static class AuthConstants {
        public static final String CACHE_KEY_PREFIX = "auth:";
        // 바꿔야함 나중에 .. ..
        public static final int CACHE_DURATION_MINUTES = 30000000;
    }

    public static class PasswordResetConstants {
        public static final String MAIL_QUEUE_KEY = "mail:queue";
        public static final String RESET_TOKEN_KEY = "passwordResetTokenKey";
        public static final int RESET_EXPIRATION_MINUTES = 30;
        public static final String RESET_PATH = "/reset-password?token=";
    }

    public static class UserConstants {
        public static final String USER_CACHE_KEY = "userCacheKey";
    }

    public static class NotificationConstants {
        public static final String NOTIFICATION_QUEUE_KEY = "notification:queue";
    }
}
