package com.sns.project.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class RedisLuaService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisLuaService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Lua Script 실행 메서드
     */
    public Object executeLuaScript(String script, List<String> keys, List<String> args) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        
        return redisTemplate.execute(redisScript, keys, args.toArray());
    }
}
