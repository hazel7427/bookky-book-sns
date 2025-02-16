package com.sns.project.controller;

import com.sns.project.aspect.userAuth.AuthRequired;
import com.sns.project.dto.post.request.RequestPostUpdateDto;
import com.sns.project.dto.post.response.ResponsePostDto;
import com.sns.project.handler.exceptionHandler.response.ApiResult;

import com.sns.project.service.post.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "게시물 API", description = "게시물 CRUD API")
@SecurityRequirement(name = "Bearer Authentication")
public class PostController {

  private final PostService postService;




  @Operation(summary = "게시물 생성", description = "새로운 게시물을 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @AuthRequired
  @SecurityRequirement(name = "Bearer Authentication")
  public ApiResult<ResponsePostDto> createPost(
          @RequestPart("title") @NotNull(message = "제목은 필수입니다") String title,
          @RequestPart("content") @NotNull(message = "내용은 필수입니다") String content,
          @RequestPart(value = "images", required = false) List<MultipartFile> images) {

    Long postId = postService.createPost(title, content, images);
    return ApiResult.success(new ResponsePostDto(postId));
  }

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @DeleteMapping("/{postId}")
  @AuthRequired
  @SecurityRequirement(name = "Bearer Authentication")
  public ApiResult<String> deletePost(
          @PathVariable @Parameter(description = "게시물 ID") Long postId) {
    // postService.deletePost(postId);
    return ApiResult.success("게시물이 성공적으로 삭제되었습니다.");
  }
}
