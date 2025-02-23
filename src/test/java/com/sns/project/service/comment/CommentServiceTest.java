package com.sns.project.service.comment;

import com.sns.project.domain.comment.Comment;
import com.sns.project.domain.post.Post;
import com.sns.project.domain.user.User;
import com.sns.project.dto.comment.CommentRequestDto;
import com.sns.project.dto.comment.CommentResponseDto;
import com.sns.project.repository.CommentRepository;
import com.sns.project.repository.UserRepository;
import com.sns.project.repository.PostRepository;
import com.sns.project.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    private Post post;
    private Long postId;
    private Long userId;
    private Comment parentComment;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postId = 1L;
        post = Post.builder()
            .title("Test post")
            .content("Test content")
            .id(postId)
            .build();


        userId = 1L;
        user = User.builder()
            .id(userId)
            .email("test@test.com")
            .build();

        parentComment = Comment.builder()
            .content("Test comment")
            .post(post)
            .user(user)
            .build();
    }

    @Test
    void testCreateComment() {
        Long userId = 1L;
        Long postId = 1L;
        CommentRequestDto requestDto = new CommentRequestDto(postId, "Test content");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userService.getUserById(userId)).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

        CommentResponseDto responseDto = commentService.createComment(requestDto, userId);

        assertEquals("Test content", responseDto.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testCreateReply() {
        CommentRequestDto requestDto = new CommentRequestDto(1L, "Reply content");

        when(commentRepository.findById(postId)).thenReturn(Optional.of(parentComment));
        when(userService.getUserById(userId)).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

        CommentResponseDto responseDto = commentService.createReply(postId, requestDto.getContent(), userId);

        assertEquals(requestDto.getContent(), responseDto.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testGetComments() {

        when(commentRepository.findCommentByPostId(postId)).thenReturn(Collections.singletonList(parentComment));

        List<CommentResponseDto> comments = commentService.getComments(postId);

        assertEquals(1, comments.size());
        assertEquals(parentComment.getContent(), comments.get(0).getContent());
    }

    @Test
    void testGetReplies() {
        Comment reply = Comment.builder()
            .parent(parentComment)
            .user(user)
            .content("Reply content")
            .build();

        when(commentRepository.findById(parentComment.getId())).thenReturn(Optional.of(parentComment));
        when(commentRepository.findReplyCommentByParentId(parentComment.getId())).thenReturn(Collections.singletonList(reply));

        List<CommentResponseDto> replies = commentService.getReplies(parentComment.getId());

        assertEquals(1, replies.size());
        assertEquals(reply.getContent(), replies.get(0).getContent());

    }
}