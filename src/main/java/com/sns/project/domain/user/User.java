package com.sns.project.domain.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sns.project.domain.follow.Follow;
import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @Column(unique = true)
  private String email; // 이메일을 아이디로 사용
  private String password;
  private String name;
  private String company;
  private String profile_image_url;


  @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Follow> followers;

  @OneToMany(mappedBy = "following", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Follow> followings;

}