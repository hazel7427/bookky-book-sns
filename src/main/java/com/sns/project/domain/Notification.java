package com.sns.project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; 
    private String message;
    
    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public void markAsRead() {
        this.isRead = true;
    }
} 