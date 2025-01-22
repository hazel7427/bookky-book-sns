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
public class User  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @Column(unique = true)          // 로그인 ㅇㅏ이디
  @JsonIgnore
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