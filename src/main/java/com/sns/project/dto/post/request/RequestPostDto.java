package com.sns.project.dto.post.request;



import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPostDto {
  @NotNull
  private String title;
  @NotNull
  private String content;
}