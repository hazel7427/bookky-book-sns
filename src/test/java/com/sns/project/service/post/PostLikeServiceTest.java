package com.sns.project.service.post;

import com.sns.project.domain.post.Post;
import com.sns.project.domain.post.PostLike;
import com.sns.project.domain.user.User;
import com.sns.project.repository.post.PostLikeRepository;
import com.sns.project.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class PostLikeServiceTest {

  @Mock
  private PostLikeRepository postLikeRepository;

  @Mock
  private UserService userService;

  @Mock
  private PostService postService;

  @InjectMocks
  private PostLikeService likeService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testToggleLike_addLike() {
    Long userId = 1L;
    Long postId = 1L;
    User user = new User();
    Post post = new Post();

    when(userService.getUserById(userId)).thenReturn(user);
    when(postService.getPostById(postId)).thenReturn(post);
    when(postLikeRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty());

    likeService.toggleLike(userId, postId);

    ArgumentCaptor<PostLike> postLikeCaptor = ArgumentCaptor.forClass(PostLike.class);
    verify(postLikeRepository).save(postLikeCaptor.capture());
    assertEquals(user, postLikeCaptor.getValue().getUser());
    assertEquals(post, postLikeCaptor.getValue().getPost());
  }

  @Test
  void testToggleLike_removeLike() {
    Long userId = 1L;
    Long postId = 1L;
    User user = new User();
    Post post = new Post();
    PostLike postLike = new PostLike(user, post);

    when(userService.getUserById(userId)).thenReturn(user);
    when(postService.getPostById(postId)).thenReturn(post);
    when(postLikeRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(postLike));

    likeService.toggleLike(userId, postId);

    verify(postLikeRepository).delete(postLike);
  }
}