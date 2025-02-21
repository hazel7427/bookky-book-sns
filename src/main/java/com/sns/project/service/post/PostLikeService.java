package com.sns.project.service.post;

import com.sns.project.domain.post.Post;
import com.sns.project.domain.post.PostLike;
import com.sns.project.domain.user.User;
import com.sns.project.repository.PostLikeRepository;
import com.sns.project.service.user.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserService userService;
    private final PostService postService;

    @Transactional
    public void toggleLike(Long userId, Long postId) {
        User user = userService.getUserById(userId);
        Post post = postService.getPostById(postId);
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            removeLike(existingLike.get());
        } else {
            addLike(user, post);
        }
    }

    private void removeLike(PostLike postLike) {
        // 이미 좋아요가 눌려 있다면 삭제 (좋아요 취소)
        postLikeRepository.delete(postLike);
    }

    private void addLike(User user, Post post) {
        try {
            // 좋아요 추가
            postLikeRepository.save(new PostLike(user, post));
        } catch (DataIntegrityViolationException e) {
            // 동시성 문제 방지: UNIQUE CONSTRAINT 충돌 발생 시 무시
            System.out.println("Duplicate like request ignored");
        }
    }
} 