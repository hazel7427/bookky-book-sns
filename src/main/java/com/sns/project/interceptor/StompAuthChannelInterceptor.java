package com.sns.project.interceptor;

import com.sns.project.service.user.UserService;
import com.sns.project.service.user.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;


    /*
              connectHeaders: {
                    'Authorization': `Bearer ${Cookies.get('token')}`
                },
                헤더에서 인증 토큰을 가져옵니다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);


        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> protocols = accessor.getNativeHeader("Authorization");


            if (protocols != null && !protocols.isEmpty()) {
                String value = protocols.get(0);
                String token = value.substring(7);
                try {
                    Long userId = tokenService.validateToken(token);
                accessor.setUser(() -> String.valueOf(userId));
            } catch (Exception e) {
                    return null; // This will prevent the connection
                }
            }
            
        }
        return message;
    }
}