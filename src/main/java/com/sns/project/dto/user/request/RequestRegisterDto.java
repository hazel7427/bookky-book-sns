package com.sns.project.dto.user.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestRegisterDto {
  private String email;
  private String name;
  private String password;
  private String profile_image;
}
