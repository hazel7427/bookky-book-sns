package com.sns.project.service.chat;

import com.sns.project.config.constants.RedisKeys.Chat;
import org.springframework.stereotype.Service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {
    private final RedisService redisService;

    // 사용자가 채팅방에 들어오면 Redis에 저장
    public void userEnteredRoom(Long roomId, Long userId) {
        String key = Chat.CHAT_PRESENCE_USERS_KEY.getPresenceUsersKey(roomId);
        redisService.setValueWithExpirationInSet(key, userId, 10 * 60);
    }

    // 사용자가 채팅방을 나가면 Redis에서 제거
    public void userLeftRoom(Long roomId, Long userId) {
        String key = Chat.CHAT_PRESENCE_USERS_KEY.getPresenceUsersKey(roomId);
        redisService.removeFromSet(key, userId);
    }

    // 사용자가 현재 채팅방에 있는지 확인
    public boolean isUserInRoom(Long roomId, Long userId) {
        String key = Chat.CHAT_PRESENCE_USERS_KEY.getPresenceUsersKey(roomId);
        return redisService.isValueInSet(key, userId);
    }
}
