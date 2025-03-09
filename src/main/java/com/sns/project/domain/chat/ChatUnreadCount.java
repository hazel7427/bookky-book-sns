package com.sns.project.domain.chat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_unread_counts")
public class ChatUnreadCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "unread_count", nullable = false)
    private int unreadCount;

    public void decrementUnreadCount() {
        this.unreadCount = Math.max(0, this.unreadCount - 1);
    }
}
