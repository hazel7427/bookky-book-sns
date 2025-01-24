package com.sns.project.domain.user;


import com.sns.project.dto.user.request.RequestRegisterDto;

public class UserFactory {

  public static User createUser(RequestRegisterDto dto) {
    return User.builder()
        .email(dto.getEmail())
        .password(dto.getPassword()) // 비밀번호는 이후 암호화 필요
        .name(dto.getName())
        .profile_image_url(dto.getProfile_image())
        .build();
  }


}

