package com.sns.project.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sns.project.domain.chat.ChatUnreadCount;

public interface ChatUnReadRepository extends JpaRepository<ChatUnreadCount, Long> {

    ChatUnreadCount findByMessageId(Long messageId);
    
}
