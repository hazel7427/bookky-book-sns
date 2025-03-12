package com.sns.project.service.chat;

import com.sns.project.config.EmbeddedRedisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.config.constants.RedisKeys.Chat;
import com.sns.project.service.RedisService;
import com.sns.project.service.chat.ChatReadService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
class ChatReadServiceTest {

    @Mock
    private RedisService redisService;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    

    @InjectMocks
    private ChatReadService chatReadService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ROOM_ID = 1L;
    private static final long LOCK_EXPIRATION = 5L;  // ChatReadService의 상수값과 동일하게 설정

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);


        String lockKey = RedisKeys.Chat.CHAT_LOCK_KEY.getLockKey(TEST_USER_ID, TEST_ROOM_ID);

        // ✅ Mock 정확하게 설정

        doReturn(true).when(redisService).setIfAbsent(eq(lockKey), eq("LOCKED"), eq(LOCK_EXPIRATION));

//        when(redisService.setIfAbsent(eq(lockKey), eq("LOCKED"), eq(LOCK_EXPIRATION))).thenReturn(true);
//        redisService.setIfAbsent(lockKey, "LOCKED", LOCK_EXPIRATION); // 강제 호출 (더미 값 리턴)

    }

    @Test
    void 이미_읽은_메시지_제외_테스트2() {
        // given
        String lockKey = RedisKeys.Chat.CHAT_LOCK_KEY.getLockKey(TEST_USER_ID, TEST_ROOM_ID);
//        System.out.println("Mock 설정 확인: " + redisService.setIfAbsent(lockKey, "LOCKED", LOCK_EXPIRATION));

        // when
        chatReadService.markAllAsRead(TEST_USER_ID, TEST_ROOM_ID);

        // then
        verify(redisService, times(1)).setIfAbsent(eq(lockKey), eq("LOCKED"), eq(LOCK_EXPIRATION)); // ✅ 반드시 호출되었는지 검증
    }

} 