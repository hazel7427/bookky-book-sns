package com.sns.project.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sns.project.domain.chat.ChatReadMessage;

public interface ChatReadRepository extends JpaRepository<ChatReadMessage, Long> {
    @Query("SELECT cr FROM ChatReadMessage cr WHERE cr.messageId = :messageId AND cr.userId = :userId")
    ChatReadMessage findByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
}
