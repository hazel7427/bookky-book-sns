package com.sns.project.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

  @Autowired
  private StringRedisTemplate redisTemplate;

  // 값 저장하기
  public void saveToCache(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  // 값 조회하기
  public String getFromCache(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  // 해시에 값 저장하기
  public void putValueInHash(String redisHashKey, String fieldKey, Object value) {
    redisTemplate.opsForHash().put(redisHashKey, fieldKey, value);
  }

}

