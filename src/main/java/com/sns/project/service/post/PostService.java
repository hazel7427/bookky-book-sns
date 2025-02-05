package com.sns.project.service.post;

import com.sns.project.dto.post.RequestPostDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

  private final GCSService gcsService;

//  public void createPost(RequestPostDto requestPostDto) {
//    List<String> imageUrls = requestPostDto.getImageFiles().stream()
//        .map(gcsService::uploadFile)
//        .collect(Collectors.toList());
//  }
}
