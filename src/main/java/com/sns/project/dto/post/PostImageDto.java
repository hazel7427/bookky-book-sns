package com.sns.project.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostImageDto {
    @NotNull
    private Integer order;
    @NotNull
    private boolean isNew;
    // isNew가 true일 경우 imageFile이 있고, false일 경우 imageUrl이 있다.
    private String imageUrl;
    private MultipartFile imageFile;
} 