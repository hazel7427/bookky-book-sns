package com.sns.project.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import com.sns.project.domain.chat.ChatUnreadCount;

public interface ChatUnReadRepository extends JpaRepository<ChatUnreadCount, Long> {

    ChatUnreadCount findByMessageId(Long messageId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatUnreadCount c SET c.unreadCount = :unreadCount WHERE c.messageId = :messageId")
    void updateUnreadCount(Long messageId, int unreadCount);
    
}
