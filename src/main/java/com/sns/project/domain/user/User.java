package com.sns.project.domain.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sns.project.domain.follow.Follow;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;
import lombok.*;

import java.util.List;
import lombok.ToString.Exclude;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"followers", "followings"})
@EqualsAndHashCode
@Builder
public class User  implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @Column(unique = true)
  private String email; // 이메일을 아이디로 사용
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