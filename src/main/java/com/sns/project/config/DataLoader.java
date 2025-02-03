package com.sns.project.config;

import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
  private final UserService userService;

  public void run(String... args) {
    saveUser("homeyoyyya@gmail.com");
    saveUser("2@gmail.com");
    saveUser("3@gmail.com");
  }

  private void saveUser(String email) {
    RequestRegisterDto requestRegisterDto = new RequestRegisterDto();
    requestRegisterDto.setEmail(email);
    requestRegisterDto.setPassword("1234");
    requestRegisterDto.setName("test");
    userService.register(requestRegisterDto);
  }
}

