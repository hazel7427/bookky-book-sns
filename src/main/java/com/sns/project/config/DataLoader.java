package com.sns.project.config;

import java.util.List;

import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.service.NotificationService;
import com.sns.project.service.RedisService;
import com.sns.project.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
  private final UserService userService;
  private final RedisService redisService;
  private final NotificationService notificationService;
  
    public void run(String... args) {
      saveUser("homeyoyyya@gmail.com");
      saveUser("2@gmail.com");
      saveUser("3@gmail.com");
    
      saveUserToken(1L, "testToken1");
      saveUserToken(2L, "testToken2");
      saveUserToken(3L, "testToken3");

      saveNotification();
    }
  
    private void saveUserToken(Long userId, String token) {
      redisService.setValueWithExpiration(token, String.valueOf(userId), 10000*60);
    }
  
    private void saveUser(String email) {
      RequestRegisterDto requestRegisterDto = new RequestRegisterDto();
      requestRegisterDto.setEmail(email);
      requestRegisterDto.setPassword("1234");
      requestRegisterDto.setName("test");
      userService.register(requestRegisterDto);
      
    }
  
    /*
    알림 저장
    송신자: 유저3
    수신자: 유저1, 유저2
     */
    private void saveNotification() {
      for (int i = 0; i < 30; i++) {
        notificationService.sendNotification("test notification"+i, 3L, List.of(1L, 2L));
      }
  }
}

