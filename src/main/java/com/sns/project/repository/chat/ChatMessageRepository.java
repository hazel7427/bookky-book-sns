package com.sns.project.repository.chat;

import com.sns.project.domain.chat.ChatMessage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomId(Long chatRoomId);
}
