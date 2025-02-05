package com.sns.project.aspect.userAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sns.project.service.RedisService;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.UnauthorizedException;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthAspect {

    private final RedisService redisService;

    @Around("@annotation(com.sns.project.aspect.userAuth.AuthRequired)")
    public Object validateToken(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("Invalid request context");
        }

        String authHeader = attributes.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);
        log.info("token :{}", token);

        Optional<Long> userIdOpt = redisService.getValue(token, Long.class);
        if (userIdOpt.isEmpty()) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        log.info("userId: {}", userIdOpt.get());
        UserContext.setUserId(userIdOpt.get());
        
        try {
            return joinPoint.proceed();
        } finally {
            UserContext.clear();
        }
    }
} 