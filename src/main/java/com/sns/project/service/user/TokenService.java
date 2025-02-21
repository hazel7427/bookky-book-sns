package com.sns.project.service.user;

import com.sns.project.handler.exceptionHandler.exception.unauthorized.UnauthorizedException;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisService redisService;

    public Long extractAndValidateToken(ServletRequestAttributes requestAttributes) {
        if (requestAttributes == null) {
            throw new UnauthorizedException("Invalid request context");
        }

        String authHeader = requestAttributes.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);
        Optional<Long> userIdOpt = getUserId(token);
        if (userIdOpt.isEmpty()) {
            throw new UnauthorizedException(token+":Invalid or expired token");
        }

        return userIdOpt.get();
    }


    public boolean isValidToken(String token) {
        Optional<Long> userIdOpt = getUserId(token);
        return userIdOpt.isPresent();
    }

    private Optional<Long> getUserId(String token) {
        Optional<Long> userIdOpt = redisService.getValue(token, Long.class);
        return userIdOpt;
    }
}