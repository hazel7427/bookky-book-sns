package com.sns.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sns.project.domain.post.PostImageInfo;

public interface PostImageInfoRepository extends JpaRepository<PostImageInfo, Long> {

}
