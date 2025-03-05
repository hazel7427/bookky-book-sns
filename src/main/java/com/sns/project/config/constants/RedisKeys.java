package com.sns.project.config.constants;

public class RedisKeys {

    public enum Chat {
        CHAT_ROOM_USERS_KEY("chat:room:users:"),
        CHAT_READ_QUEUE_KEY("chat:read:queue:"),
        CHAT_UNREAD_COUNT_KEY("chat:unread:count:"),
        CHAT_PRESENCE_USERS_KEY("chat:presence:");
        // CHAT_MESSAGES_KEY("chat:messages:"),
        // CHAT_ROOM_INFO_KEY("chat:room:info:");

        private final String key;
        Chat(String key) { this.key = key; }
        public String get() { return key; }
        
        public String getUnreadCountKey(Long messageId) {
            return this.key + messageId;
        }
        
        public String getPresenceUsersKey(Long roomId) {
            return this.key + roomId;
        }
        
        public String getMessagesKey(Long roomId) {
            return this.key + roomId;
        }
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
