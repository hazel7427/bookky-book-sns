package com.sns.project.controller;

import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.dto.user.request.LoginRequestDto;
import com.sns.project.dto.user.request.RequestPasswordResetDto;
import com.sns.project.dto.user.request.ResetPasswordDto;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


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


  @PostMapping("/login")
  public ApiResult<String> login(@RequestBody LoginRequestDto request) {
    String token = userService.authenticate(request.getEmail(), request.getPassword());
    return ApiResult.success(token);
  }

  @PostMapping("/logout")
  public void logout() {
  }


  /*
   * 비밀번호 재설정 요청
   * 유저 이메일을 받아 비밀번호 재생성 페이지 링크를 포함하는 이메일을 보낸다.
   * 비밀번호 재생성 링크는 "주소도메인/reset-password?token=~"
   * 
   * @param request 비밀번호 재설정 요청 데이터(email)
   * @return 비밀번호 재설정 요청 결과
   */
  @PostMapping("/request-reset-password")
  public ApiResult<String> requestPasswordReset(@RequestBody RequestPasswordResetDto request) {
    userService.requestPasswordReset(request.getEmail());
    return ApiResult.success("비밀번호 재생성 링크를 보냈습니다.");
  }

  /*
   * 새로운 비밀번호 저장
   * 파라미터로 온 토큰의 유효성을 검사하고 비밀번호를 재설정한다.
   * 
   * @param token 토큰
   * @param request 비밀번호 재설정 요청 데이터
   * @return 비밀번호 재설정 결과
   */
  @PostMapping("/reset-password")
  public ApiResult<String> resetPassword(@RequestParam String token, 
                                            @RequestBody ResetPasswordDto request) {
    userService.resetPassword(token, request.getNewPassword());
    return ApiResult.success("비밀번호가 성공적으로 재설정되었습니다.");
  }
}
