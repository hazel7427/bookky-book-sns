package com.sns.project.domain.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sns.project.domain.post.Post;
import com.sns.project.domain.user.User;
import com.sns.project.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "댓글 내용은 필수입니다")
  private String content;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<CommentLike> commentLikes = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  @JsonIgnore
  private Post post;

  @Builder
  public Comment(User user, Post post, String content) {
    this.user = user;
    this.post = post;
    this.content = content;
  }

}

