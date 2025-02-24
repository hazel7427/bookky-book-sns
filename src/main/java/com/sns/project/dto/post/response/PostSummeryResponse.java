package com.sns.project.dto.post.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostSummeryResponse {
    private Long id;
    private String title;
    private String userName;
    private Long likeCount;
    private Long commentCount;
    private String createdAt;
}
