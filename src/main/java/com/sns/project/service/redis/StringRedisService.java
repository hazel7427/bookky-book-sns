package com.sns.project.service.redis;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StringRedisService {
    private final RedisTemplate<String, String> stringRedisTemplate;

    // ===== Set Operations =====
    public void addToSet(String key, String value) {
        stringRedisTemplate.opsForSet().add(key, value);
    }

    public void removeFromSet(String key, String value) {
        stringRedisTemplate.opsForSet().remove(key, value);
    }

    public Set<String> getSetMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    public boolean isSetMember(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, value));
    }

    // ===== Hash Operations =====
    public void setHashValue(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Optional<String> getHashValue(String key, String hashKey) {
        return Optional.ofNullable((String) stringRedisTemplate.opsForHash().get(key, hashKey));
    }

    // ===== Sorted Set Operations =====
    public void addToZSet(String key, String value, double score) {
        stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<String> getZSetRange(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Optional<String> getHighestScoreFromZSet(String key) {
        Set<String> values = stringRedisTemplate.opsForZSet().range(key, -1, -1);
        return Objects.requireNonNull(values).isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
    }

    public ZSetOperations.TypedTuple<String> getHighestScoreWithScoreFromZSet(String key) {
        Set<ZSetOperations.TypedTuple<String>> values = stringRedisTemplate.opsForZSet().rangeWithScores(key, -1, -1);
        return Objects.requireNonNull(values).isEmpty() ? null : values.iterator().next();
    }

    // ===== Basic Operations =====
    public void setValue(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public Optional<String> getValue(String key) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    public Boolean setIfAbsent(String key, String value, Duration timeout) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout);
    }

    // ===== Utility Methods =====
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public Boolean expire(String key, Duration timeout) {
        return stringRedisTemplate.expire(key, timeout);
    }

    public void setValueWithExpirationInSet(String key, String value, int seconds) {
        stringRedisTemplate.opsForSet().add(key, value);
        stringRedisTemplate.expire(key, Duration.ofSeconds(seconds));
    }

    public List<String> popMultipleFromSet(String chatReadQueueKey, int i) {
        return stringRedisTemplate.opsForSet().pop(chatReadQueueKey, i);
    }


  public boolean exists(String key) {
    return stringRedisTemplate.hasKey(key);
  }

    public void deletePattern(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        stringRedisTemplate.delete(keys);
    }


} 