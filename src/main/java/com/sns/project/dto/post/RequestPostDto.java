package com.sns.project.dto.post;



import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestPostDto {
  @NotNull
  private String title;
  @NotNull
  private String content;
  @NotNull
  private List<PostImageDto> images;
}