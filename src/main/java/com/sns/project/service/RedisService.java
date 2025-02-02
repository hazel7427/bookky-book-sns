package com.sns.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;


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



  /*
   * 리스트에 값 추가하기
   * 
   * @param key 리스트 키
   * @param value 추가할 값
   */
  public void pushToList(String key, Object value) {
    redisTemplate.opsForList().rightPush(key, value);
  }

  /*
   * 리스트에서 값 가져오기
   * 
   * @param key 리스트 키
   * @param clazz 반환할 클래스 타입
   * @return 리스트에서 가져온 값
   */
  public <T> T popFromList(String key, Class<T> clazz) {
    Object value = redisTemplate.opsForList().leftPop(key);
    return value != null ? clazz.cast(value) : null;
  }

  /*
   * 값 저장하기
   * 
   * @param key 키 
   * @param value 값
   * @param seconds 만료 시간(초)
   */
  public void setValueWithExpiration(String key, String value, long seconds) {
    redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
  }



  /*
   * 값 삭제하기
   * 
   * @param key 키
   */
  public void deleteValue(String key) {
    redisTemplate.delete(key);
  }

  /*
   * 값 가져오기
   * 
   * @param key 키
   * @param clazz 반환할 클래스 타입
   * @return 값
   */
  public <T> Optional<T> getValue(String key, Class<T> clazz) {
    Object value = redisTemplate.opsForValue().get(key);

    if (value == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(objectMapper.convertValue(value, clazz));
  }






}

