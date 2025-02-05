package com.sns.project.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RequestPostUpdateDto {
    private String content;
    private List<PostImageDto> images;


} 