package com.sns.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import javax.swing.text.html.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // ✅ Serialization (JSON 변환)
    private <T> String serialize(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Redis 직렬화 오류: {}", e.getMessage());
            throw new RuntimeException("직렬화 실패");
        }
    }

    private <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Redis 역직렬화 오류: {}", e.getMessage());
            throw new RuntimeException("역직렬화 실패");
        }
    }

    // ✅ Value Operations (Key-Value)
    public Optional<Long> decrementValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
    
        if (value == null) {
            return Optional.empty();
        }
    
        if (value instanceof String) {
            try {
                Long numericValue = Long.parseLong((String) value);
                redisTemplate.opsForValue().set(key, String.valueOf(numericValue - 1));
                return Optional.of(numericValue - 1);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Stored value is not a valid number for key: " + key);
            }
        }
    
        throw new IllegalArgumentException("Value type is invalid for key: " + key);
    }
    

    public void setValueWithExpiration(String key, Object value, long seconds) {
        String jsonValue = serialize(value);
        redisTemplate.opsForValue().set(key, jsonValue, seconds, TimeUnit.SECONDS);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public <T> Optional<T> getValue(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(deserialize(value.toString(), clazz));
    }

    // ✅ Hash Operations
    public void putValueInHash(String redisHashKey, String fieldKey, Object value) {
        String jsonValue = serialize(value);
        redisTemplate.opsForHash().put(redisHashKey, fieldKey, jsonValue);
    }

    public <T> T getValueFromHash(String redisHashKey, String fieldKey, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(redisHashKey, fieldKey);
        if (value == null) {
            return null;
        }
        return deserialize(value.toString(), clazz);
    }

    // ✅ Queue Operations (FIFO)
    public void pushToQueue(String key, Object value) {
        String jsonValue = serialize(value);
        redisTemplate.opsForList().rightPush(key, jsonValue);
    }

    public <T> T popFromQueue(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForList().leftPop(key);
        if (value == null) {
            return null;
        }
        return deserialize(value.toString(), clazz);
    }

    public <T> T popFromQueueBlocking(String key, Class<T> clazz, long timeoutSeconds) {
        Object value = redisTemplate.opsForList().leftPop(key, Duration.ofSeconds(timeoutSeconds));
        if (value == null) {
            return null;
        }
        return deserialize(value.toString(), clazz);
    }

    // ✅ Set Operations
    public <T> void addToSet(String key, T value) {
        String jsonValue = serialize(value);
        redisTemplate.opsForSet().add(key, jsonValue);
    }

    public <T> void removeFromSet(String key, T value) {
        String jsonValue = serialize(value);
        redisTemplate.opsForSet().remove(key, jsonValue);
    }

    public <T> void setValueWithExpirationInSet(String key, T value, int expirationSeconds) {
        String jsonValue = serialize(value);
        redisTemplate.opsForSet().add(key, jsonValue);
        redisTemplate.expire(key, expirationSeconds, TimeUnit.SECONDS);
    }

    public <T> boolean isValueInSet(String key, T value) {
        String jsonValue = serialize(value);
        return redisTemplate.opsForSet().isMember(key, jsonValue);
    }

    public <T> Set<T> getValuesFromSet(String key, Class<T> clazz) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) {
            return null;
        }
        return members.stream()
                      .map(member -> deserialize(member.toString(), clazz))
                      .collect(Collectors.toSet());
    }

    public void setValue(String key, Object value) {
        String jsonValue = serialize(value);
        redisTemplate.opsForValue().set(key, jsonValue);
    }
}
