package com.sns.project.service.following;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sns.project.domain.follow.Follow;
import com.sns.project.domain.user.User;
import com.sns.project.dto.following.FollowingsResponse;
import com.sns.project.dto.following.FollowUserResponse;
import com.sns.project.dto.following.FollowersResponse;
import com.sns.project.service.user.UserService;
import com.sns.project.repository.FollowRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowingService {

    private static final Logger logger = LoggerFactory.getLogger(FollowingService.class);

    private final UserService userService;
    private final FollowRepository followRepository;


    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        User follower = userService.getUserById(followerId);
        User following = userService.getUserById(followingId);        

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.deleteByFollowerAndFollowing(follower, following);
            logger.info("User {} unfollowed user {}", followerId, followingId);
        } else {
            logAndThrowWarning("unfollow", followerId, followingId);
        }
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if(followerId.equals(followingId)){
            throw new IllegalArgumentException("User cannot follow themselves");
        }
        User follower = userService.getUserById(followerId);
        User following = userService.getUserById(followingId);

        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            Follow follow = new Follow(follower, following);
            followRepository.save(follow);
            logger.info("User {} followed user {}", followerId, followingId);
        } else {
            logAndThrowWarning("follow", followerId, followingId);
        }
    }

 

    private void logAndThrowWarning(String action, Long followerId, Long followingId) {
        String message = String.format("Attempt to %s an existing relationship: User %d -> User %d", action, followerId, followingId);
        logger.warn(message);
        throw new IllegalArgumentException("Follow relationship " + (action.equals("follow") ? "already exists" : "does not exist"));
    }

    public FollowingsResponse getAllFollowings(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new FollowingsResponse(
            followRepository.findAllByFollowerId(userId, pageable).stream()
                .map(Follow::getFollowing)
                .map(FollowUserResponse::new)
                .collect(Collectors.toList())
        );
    }

    public FollowersResponse getAllFollowers(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new FollowersResponse(
            followRepository.findAllByFollowingId(userId, pageable).stream()
                .map(Follow::getFollower)
                .map(FollowUserResponse::new)
                .collect(Collectors.toList())
        );
    }
}