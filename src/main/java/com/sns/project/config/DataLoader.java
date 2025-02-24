package com.sns.project.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sns.project.dto.comment.CommentRequestDto;
import com.sns.project.dto.comment.CommentResponseDto;
import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.service.NotificationService;
import com.sns.project.service.RedisService;
import com.sns.project.service.comment.CommentService;
import com.sns.project.service.following.FollowingService;
import com.sns.project.service.post.PostService;
import com.sns.project.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserService userService;
    private final RedisService redisService;
    private final NotificationService notificationService;
    private final PostService postService;
    private final CommentService commentService;
    private final FollowingService followingService;
    private final Random random = new Random();
    
    @Override
    public void run(String... args) {
        initializeUsers();
        initializeUserTokens();
        saveNotifications();
        savePosts(1L);
        savePosts(2L);
        savePosts(3L);
        savePosts(4L);
        follow();
    }

    private void initializeUsers() {
        List<String> emails = List.of("homeyoyyya@gmail.com", "2@gmail.com", "3@gmail.com", "4@gmail.com");
        emails.forEach(this::saveUser);
    }

    private void initializeUserTokens() {
        List<Long> userIds = List.of(1L, 2L, 3L);
        List<String> tokens = List.of("testToken1", "testToken2", "testToken3");
        for (int i = 0; i < userIds.size(); i++) {
            saveUserToken(userIds.get(i), tokens.get(i));
        }
    }

    private void saveUserToken(Long userId, String token) {
        redisService.setValueWithExpiration(token, String.valueOf(userId), 10000 * 60);
    }

    private void saveUser(String email) {
        RequestRegisterDto requestRegisterDto = createRegisterDto(email);
        userService.register(requestRegisterDto);
    }

    private RequestRegisterDto createRegisterDto(String email) {
        RequestRegisterDto dto = new RequestRegisterDto();
        dto.setEmail(email);
        dto.setPassword("1234");
        dto.setName("test");
        return dto;
    }

    private void saveNotifications() {
        Long senderId = 3L;
        List<Long> receiverIds = List.of(1L, 2L);
        for (int i = 0; i < 30; i++) {
            notificationService.sendNotification("test notification" + i, senderId, receiverIds);
        }
    }

    private void savePosts(Long userId) {
        List<MultipartFile> images = new ArrayList<>();
        createPosts(images, userId);
    }

    private void createPosts(List<MultipartFile> images, Long userId) {
        for (int i = 0; i < 10; i++) {
            Long postId = postService.createPost("title" + i, "content" + i, images, userId);
            for(int j = 0; j < random.nextInt(5); j++) {
                createComments(postId);
            }
            // for(int j = 0; j < random.nextInt(5); j++) {
                // likePost(postId, userId);
            // }
        }
    }

    private void createComments(Long postId) {
        for (int i = 0; i < 5; i++) {
            CommentResponseDto parent = commentService.createComment(new CommentRequestDto(postId, "content" + i), 1L);
            createReplies(parent.getId());
        }
    }

    private void createReplies(Long parentId) {
        for (int j = 0; j < 2; j++) {
            commentService.createReply(parentId, "reply content" + j, 1L);
        }
    }

    /*
     * 1번 유저의 팔로잉 : 2, 3, 4,
     * 2번 유저의 팔로잉 : 1,
     * 3번 유저의 팔로잉 : 1,
     * 4번 유저의 팔로잉 : 1,
     */
    private void follow() {
        // 1번 유저가 2, 3, 4번 유저를 팔로우
        followingService.followUser(1L, 2L);
        followingService.followUser(1L, 3L);
        followingService.followUser(1L, 4L);

        // 1번 유저 팔로워 2, 3 ,4 (3명)
        followingService.followUser(2L, 1L);
        followingService.followUser(3L, 1L);
        followingService.followUser(4L, 1L);
        
    }
}

