package com.sns.project.controller;

import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // 새로운 사용자 회원가입
  @PostMapping("/register")
  public void register(@RequestBody RequestRegisterDto request) {
    userService.register(request);
  }
}
