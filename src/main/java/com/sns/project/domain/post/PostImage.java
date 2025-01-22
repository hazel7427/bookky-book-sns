package com.sns.project.domain.post;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "post_image")
public class PostImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer post_order; // 게시물 순서

  private String img_url;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

}