package com.sns.project.service.comment;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
//
//    @Mock
//    private CommentRepository commentRepository;
//
//    @Mock
//    private PostRepository postRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private UserService userService;
//
//    @InjectMocks
//    private CommentService commentService;
//
//    private Post post;
//    private Long postId;
//    private Long userId;
//    private Comment parentComment;
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        postId = 1L;
//        post = Post.builder()
//            .title("Test post")
//            .content("Test content")
//            .id(postId)
//            .build();
//
//
//        userId = 1L;
//        user = User.builder()
//            .id(userId)
//            .email("test@test.com")
//            .build();
//
//
//        parentComment = Comment.builder()
//            .id(1L)
//            .content("Test comment")
//            .post(post)
//            .user(user)
//            .build();
//    }
//
//    @Test
//    void testCreateComment() {
//        Long userId = 1L;
//        Long postId = 1L;
//        CommentRequestDto requestDto = new CommentRequestDto(postId, "Test content");
//
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//        when(userService.getUserById(userId)).thenReturn(user);
//        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);
//
//        CommentResponseDto responseDto = commentService.createComment(requestDto, userId);
//
//        assertEquals("Test content", responseDto.getContent());
//        verify(commentRepository, times(1)).save(any(Comment.class));
//    }
//
//    @Test
//    void testCreateReply() {
//
//        when(commentRepository.findById(parentComment.getId())).thenReturn(Optional.of(parentComment));
//        when(userService.getUserById(userId)).thenReturn(user);
////        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);
//
//        CommentResponseDto responseDto = commentService.createReply(parentComment.getId(), "content", userId);
////
////        assertEquals("content", responseDto.getContent());
////        verify(commentRepository, times(1)).save(any(Comment.class));
//    }
//
//    @Test
//    void testGetComments() {
//
//        when(commentRepository.findCommentByPostId(postId)).thenReturn(Collections.singletonList(parentComment));
//
//        List<CommentResponseDto> comments = commentService.getComments(postId);
//
//        assertEquals(1, comments.size());
//        assertEquals(parentComment.getContent(), comments.get(0).getContent());
//    }
//
//    @Test
//    void testGetReplies() {
//        Comment reply = Comment.builder()
//            .parent(parentComment)
//            .user(user)
//            .content("Reply content")
//            .build();
//
//        when(commentRepository.findById(parentComment.getId())).thenReturn(Optional.of(parentComment));
//        when(commentRepository.findReplyCommentByParentId(parentComment.getId())).thenReturn(Collections.singletonList(reply));
//
//        List<CommentResponseDto> replies = commentService.getReplies(parentComment.getId());
//
//        assertEquals(1, replies.size());
//        assertEquals(reply.getContent(), replies.get(0).getContent());
//
//    }
}