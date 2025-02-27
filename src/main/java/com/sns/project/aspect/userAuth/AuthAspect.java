package com.sns.project.aspect.userAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sns.project.service.user.TokenService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthAspect {

    private final TokenService tokenService;

    @Around("@annotation(com.sns.project.aspect.userAuth.AuthRequired)")
    public Object validateToken(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authHeader = requestAttributes.getRequest().getHeader("Authorization");
        String token = authHeader.substring(7);
        Long userId = tokenService.validateToken(token);
        log.info("userId: {}", userId);
        UserContext.setUserId(userId);
        
        try {
            return joinPoint.proceed();
        } finally {
            UserContext.clear();
        }
    }
} 