package com.sns.project.domain.user;


import com.sns.project.dto.user.request.RequestRegisterDto;

public class UserFactory {

  public static User createUser(RequestRegisterDto dto) {
    return User.builder()
        .email(dto.getEmail())
        .userId(dto.getUserId())
        .password(dto.getPassword())
        .name(dto.getName())
        .profile_image_url(dto.getProfile_image())
        .build();
  }


}

