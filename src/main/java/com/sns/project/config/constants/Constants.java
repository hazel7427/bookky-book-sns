package com.sns.project.config.constants;

public class Constants {
    public enum Auth {
        CACHE_PREFIX("auth:");
        
        private final String key;
        Auth(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum PasswordReset {
        MAIL_QUEUE("mail:queue"),
        RESET_TOKEN("passwordResetTokenKey"),
        RESET_PATH("/reset-password?token=");

        private final String key;
        PasswordReset(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum User {
        CACHE_KEY("userCacheKey");

        private final String key;
        User(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum Notification {
        QUEUE_KEY("notification:queue");

        private final String key;
        Notification(String key) { this.key = key; }
        public String get() { return key; }
    }
}
