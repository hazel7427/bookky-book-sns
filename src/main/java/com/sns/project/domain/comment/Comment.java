package com.sns.project.domain.comment;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sns.project.domain.post.Post;
import com.sns.project.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String content;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<CommentLike> commentLikes = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "post_id")
  @JsonIgnore
  private Post post;

  @Builder
  public Comment(User user, Post post, String content) {
    this.user = user;
    this.post = post;
    this.content = content;
  }

}

