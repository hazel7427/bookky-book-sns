package com.sns.project.controller;

import com.sns.project.aspect.userAuth.AuthRequired;
import com.sns.project.dto.post.RequestPostDto;
import com.sns.project.dto.post.RequestPostUpdateDto;
import com.sns.project.service.post.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "게시물 API", description = "게시물 CRUD API")
public class PostController {

  private final PostService postService;


  @Operation(summary = "게시물 생성", description = "새로운 게시물을 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  @AuthRequired
  public ResponseEntity<String> createPost(
          @RequestBody @Parameter(description = "게시물 생성 정보") RequestPostDto requestPostDto) {
    // postService.createPost(requestPostDto);
    return ResponseEntity.ok("게시물이 성공적으로 생성되었습니다.");
  }

  @Operation(summary = "게시물 수정", description = "기존 게시물을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PutMapping("/{postId}")
  public ResponseEntity<String> updatePost(
          @PathVariable @Parameter(description = "게시물 ID") Long postId,
          @RequestBody @Parameter(description = "게시물 수정 정보") RequestPostUpdateDto requestPostUpdateDto) {
    // postService.updatePost(postId, requestPostUpdateDto);
    return ResponseEntity.ok("게시물이 성공적으로 수정되었습니다.");
  }

  @Operation(summary = "게시물 삭제", description = "게시물을 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @DeleteMapping("/{postId}")
  public ResponseEntity<String> deletePost(
          @PathVariable @Parameter(description = "게시물 ID") Long postId) {
    // postService.deletePost(postId);
    return ResponseEntity.ok("게시물이 성공적으로 삭제되었습니다.");
  }
}
