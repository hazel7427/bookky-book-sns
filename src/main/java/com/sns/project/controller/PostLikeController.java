package com.sns.project.controller;

import com.sns.project.aspect.userAuth.UserContext;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.service.post.PostLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
@Tag(name = "Like", description = "포스트 좋아요 관련 API")
public class PostLikeController {

    private final PostLikeService likeService;

    @Operation(summary = "좋아요 토글", description = "포스트에 좋아요를 누르거나 취소합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 포스트를 찾을 수 없음")
    })
    @PostMapping("/toggle")
    public ApiResult<String> toggleLike(@RequestParam Long postId) {
        Long userId = UserContext.getUserId();
        likeService.toggleLike(userId, postId);
        return ApiResult.success("좋아요 토글 성공");
    }
} 