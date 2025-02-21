package com.sns.project.dto.following;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowersResponse {
    private List<FollowUserResponse> followers;
}