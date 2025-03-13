package com.sns.project.service.chat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sns.project.repository.chat.ChatMessageRepository;
import com.sns.project.service.redis.StringRedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatBatchService {
    private final StringRedisService stringRedisService;
    private final ChatMessageRepository chatMessageRepository;

    @Scheduled(fixedRate = 60000)  // 1분마다 실행
    public void syncMessagesToDatabase() {
        

    }    
}
