package com.sns.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

  @Autowired
  private final RedisTemplate<String, Object> redisTemplate;

  // 해시에 값 저장하기
  public void putValueInHash(String redisHashKey, String fieldKey, Object value) {
    redisTemplate.opsForHash().put(redisHashKey, fieldKey, value);
  }

  // 해시에서 값 가져오기
  public <T> T getValueFromHash(String redisHashKey, String fieldKey, Class<T> clazz) {
    Object value = redisTemplate.opsForHash().get(redisHashKey, fieldKey);

    if (value == null) {
      return null;
    }

    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }

    if (clazz == Long.class && value instanceof Integer) {
      return clazz.cast(((Integer) value).longValue());
    }

    if (clazz == Double.class && value instanceof Integer) {
      return clazz.cast(((Integer) value).doubleValue());
    }

    throw new IllegalArgumentException("Cannot convert value to " + clazz.getName() + ". Value: " + value);
  }


}

