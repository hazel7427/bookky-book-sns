package com.sns.project.config.constants;

public class RedisKeys {

    public enum Chat {
        CHAT_ROOM_USERS_KEY("chat:room:users:"),
        CHAT_READ_QUEUE("chat:readQueue"),
        CHAT_UNREAD_COUNT_KEY("chat:unreadCount:"),
        CHAT_PRESENCE_KEY("chat:presence:");

        private final String key;
        Chat(String key) { this.key = key; }
        public String get() { return key; }
    }
    
    public enum Auth {
        CACHE_PREFIX("auth:");
        
        private final String key;
        Auth(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum PasswordReset {
        MAIL_QUEUE("mail:queue"),
        RESET_TOKEN("password-reset:token:");

        private final String key;
        PasswordReset(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum User {
        CACHE_KEY("user:cache");

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
