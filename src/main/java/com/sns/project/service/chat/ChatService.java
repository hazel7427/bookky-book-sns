package com.sns.project.service.chat;


import com.sns.project.config.constants.RedisKeys.Chat;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import com.sns.project.handler.exceptionHandler.exception.notfound.ChatRoomNotFoundException;
import com.sns.project.controller.chat.dto.response.ChatMessageResponse;
import com.sns.project.repository.chat.ChatMessageRepository;
import com.sns.project.repository.chat.ChatRoomRepository;
import com.sns.project.repository.chat.ChatParticipantRepository;
import com.sns.project.service.RedisService;
import com.sns.project.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final RedisService redisService;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReadService chatReadService;

    @Transactional
    public ChatMessageResponse saveMessage(Long senderId, String message, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        User sender = userService.getUserById(senderId);

        // 1. Save message and cache in Redis
        ChatMessage savedMessage = saveAndCacheMessage(chatRoom, sender, message, roomId);

        // 2. Process read status for connected users
        AtomicInteger readCount = processReadStatus(senderId, roomId, savedMessage);

        // 3. Update unread count
        int unreadCount = updateUnreadCount(roomId, savedMessage.getId(),  readCount.get());
        String unreadBatchKey = Chat.CHAT_MESSAGE_BATCH_SET_KEY.getMessageBatchQueueKey();
        redisService.addToSet(unreadBatchKey, savedMessage.getId());

        return new ChatMessageResponse(savedMessage, unreadCount);
    }

    private ChatMessage saveAndCacheMessage(ChatRoom chatRoom, User sender, String message, Long roomId) {
        ChatMessage chatMessage = new ChatMessage(chatRoom, sender, message);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        String messageKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        redisService.addToZSet(messageKey, savedMessage.getId(),
            savedMessage.getSentAt().toEpochSecond(ZoneOffset.UTC));

        log.info("Chat message saved successfully. MessageId: {}, RoomId: {}", savedMessage.getId(), roomId);
        return savedMessage;
    }

    private AtomicInteger processReadStatus(Long senderId, Long roomId, ChatMessage savedMessage) {
        AtomicInteger readCount = new AtomicInteger(0);
        String connectedUsersKey = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId);

        Set<Long> connectedUserIds = redisService.getValuesFromSet(connectedUsersKey, Long.class);
        connectedUserIds.add(senderId); // 유저가 보내고 바로 나가는 경우 대비해 송신자 아이디 추가
        connectedUserIds
            .forEach(userId -> markMessageAsRead(userId, roomId, savedMessage.getId(), readCount));


        return readCount;
    }

    private void markMessageAsRead(Long userId, Long roomId, Long messageId, AtomicInteger readCount) {

        log.info("새로운 메시지를 읽음 처리합니다 user ID:"+ userId + " roomId: " + roomId + " message ID: " + messageId);

        String readUsersKey = RedisKeys.Chat.CHAT_READ_USERS_SET_KEY.getReadUserKey(String.valueOf(messageId));
        redisService.addToSet(readUsersKey, userId);

        String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId) ;
        redisService.setValue(lastReadKey, messageId);

        readCount.incrementAndGet();
    }

    private int updateUnreadCount(Long roomId, Long messageId, int readCount) {
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
        String participantsKey = RedisKeys.Chat.CHAT_ROOM_PARTICIPANTS_SET_KEY.getParticipants(roomId);
        Set<Long> participants = redisService.getValuesFromSet(participantsKey, Long.class);
        int unreadCount = participants.size() - readCount;
        redisService.setHashValue(unreadCountKey, String.valueOf(messageId), unreadCount);
        return unreadCount;
    }

    @Transactional
    public List<ChatMessageResponse> getChatHistory(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdWithUser(roomId);
        return messages.stream().map(msg -> {
            String unreadKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
            int unreadCount = redisService.getValueFromHash(unreadKey, String.valueOf(msg.getId()), Integer.class);
            return new ChatMessageResponse(msg, unreadCount);
        }).collect(Collectors.toList());
    }
}
